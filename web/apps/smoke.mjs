#!/usr/bin/env node
// Loads each built app in headless Chromium, at desktop and phone size, and reports what breaks.
//
//   node web/apps/smoke.mjs                  serve web/apps/out/<id> locally, one port (one origin) per app
//   node web/apps/smoke.mjs --live           load https://<id>.<domain>/ instead
//   node web/apps/smoke.mjs --live wordplay  only these ids
//
// Needs Playwright: set PLAYWRIGHT_DIR to its package folder if it is not resolvable from here.
// Screenshots go to SMOKE_SHOTS (default: the system temp folder). Exit code 1 if any app fails to render.
import fs from 'node:fs';
import http from 'node:http';
import os from 'node:os';
import path from 'node:path';
import { createRequire } from 'node:module';
import { fileURLToPath } from 'node:url';

const here = path.dirname(fileURLToPath(import.meta.url));
const manifest = JSON.parse(fs.readFileSync(path.join(here, 'apps.json'), 'utf8'));
const args = process.argv.slice(2);
const live = args.includes('--live');
const ids = args.filter((a) => !a.startsWith('--'));
const apps = manifest.apps.filter((a) => ids.length === 0 || ids.includes(a.id));
const shots = process.env.SMOKE_SHOTS || fs.mkdtempSync(path.join(os.tmpdir(), 'rykersoft-smoke-'));
fs.mkdirSync(shots, { recursive: true });

const require = createRequire(process.env.PLAYWRIGHT_DIR ? `${process.env.PLAYWRIGHT_DIR}/` : import.meta.url);
const { chromium } = require(process.env.PLAYWRIGHT_DIR || 'playwright');

const TYPES = { '.html': 'text/html', '.js': 'text/javascript', '.mjs': 'text/javascript', '.css': 'text/css', '.json': 'application/json', '.png': 'image/png', '.jpg': 'image/jpeg', '.svg': 'image/svg+xml', '.wasm': 'application/wasm', '.webp': 'image/webp', '.woff2': 'font/woff2', '.woff': 'font/woff', '.ico': 'image/x-icon', '.mp3': 'audio/mpeg', '.webmanifest': 'application/manifest+json' };

function serve(root) {
  return new Promise((resolve) => {
    const server = http.createServer((req, res) => {
      let p = decodeURIComponent(new URL(req.url, 'http://x').pathname);
      if (p.endsWith('/')) p += 'index.html';
      const file = path.join(root, p);
      if (!file.startsWith(root) || !fs.existsSync(file) || fs.statSync(file).isDirectory()) {
        res.writeHead(404);
        return res.end();
      }
      res.writeHead(200, { 'content-type': TYPES[path.extname(file)] || 'application/octet-stream' });
      fs.createReadStream(file).pipe(res);
    });
    server.listen(0, '127.0.0.1', () => resolve(server));
  });
}

const browser = await chromium.launch();
let broken = 0;
for (const app of apps) {
  let server = null;
  let url = `https://${app.id}.${manifest.domain}/`;
  if (!live) {
    const root = path.join(here, 'out', app.id);
    if (!fs.existsSync(root)) {
      console.log(`${app.id.padEnd(16)} not built`);
      broken += 1;
      continue;
    }
    server = await serve(root);
    url = `http://localhost:${server.address().port}/`;
  }
  const notes = new Set();
  let elements = 0;
  for (const [name, viewport] of [['desktop', { width: 1280, height: 800 }], ['phone', { width: 390, height: 844 }]]) {
    const context = await browser.newContext({ viewport, hasTouch: name === 'phone', isMobile: name === 'phone' });
    const page = await context.newPage();
    page.on('pageerror', (e) => notes.add(`script error: ${String(e).slice(0, 200)}`));
    page.on('response', (r) => {
      if (r.status() >= 400) notes.add(`${r.status()} ${r.url().slice(0, 140)}`);
    });
    page.on('console', (m) => {
      if (m.type() === 'error' && !/Failed to load resource/.test(m.text())) notes.add(`console: ${m.text().slice(0, 200)}`);
    });
    await page.goto(url, { waitUntil: 'load', timeout: 45000 }).catch((e) => notes.add(`load: ${e.message.slice(0, 160)}`));
    await page.waitForTimeout(5000);
    elements = Math.max(elements, await page.evaluate(() => document.querySelectorAll('body *').length).catch(() => 0));
    await page.screenshot({ path: path.join(shots, `${app.id}-${name}.png`) }).catch(() => {});
    await context.close();
  }
  server?.close();
  const rendered = elements >= 15;
  if (!rendered) broken += 1;
  console.log(`${app.id.padEnd(16)} ${rendered ? 'renders' : 'BLANK  '} ${String(elements).padStart(5)} elements  ${url}`);
  for (const note of notes) console.log(`${' '.repeat(17)}${note}`);
}
await browser.close();
console.log(`\nScreenshots: ${shots}`);
process.exit(broken ? 1 : 0);
