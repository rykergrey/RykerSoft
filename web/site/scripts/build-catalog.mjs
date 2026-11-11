// Builds public/catalog.json for the site from the repo's registry.json.
//
// registry.json stays the single source of truth. This script only derives what the registry
// does not carry: each file's size and release date (from GitHub's release list) and each
// file's version (from the release tag in its URL). Entries that share an "id" are one product.
//
// Run:  node scripts/build-catalog.mjs            (reads ../../registry.json)
//       REGISTRY_URL=https://raw.githubusercontent.com/rykergrey/RykerSoft/main/registry.json node scripts/build-catalog.mjs
// Set GITHUB_TOKEN to lift GitHub's anonymous rate limit. If GitHub cannot be reached, sizes and
// dates are carried over from the previous catalog.json so a failed refresh never loses data.
import { readFile, writeFile, mkdir, copyFile, readdir } from 'node:fs/promises';
import { existsSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import path from 'node:path';

const here = path.dirname(fileURLToPath(import.meta.url));
const site = path.resolve(here, '..');
const repo = path.resolve(site, '../..');
const out = path.resolve(process.env.CATALOG_OUT || path.join(site, 'public'));
const RELEASE_REPO = 'rykergrey/RykerSoft-APKs';

async function loadRegistry() {
  if (process.env.REGISTRY_URL) {
    const r = await fetch(process.env.REGISTRY_URL, { headers: { 'Cache-Control': 'no-cache' } });
    if (!r.ok) throw new Error(`registry: HTTP ${r.status}`);
    return r.json();
  }
  return JSON.parse(await readFile(path.join(repo, 'registry.json'), 'utf8'));
}

async function loadReleases() {
  const headers = { Accept: 'application/vnd.github+json', 'User-Agent': 'catalog-builder' };
  if (process.env.GITHUB_TOKEN) headers.Authorization = `Bearer ${process.env.GITHUB_TOKEN}`;
  const map = new Map();
  for (let page = 1; page <= 20; page++) {
    const r = await fetch(`https://api.github.com/repos/${RELEASE_REPO}/releases?per_page=100&page=${page}`, { headers });
    if (!r.ok) throw new Error(`GitHub releases: HTTP ${r.status}`);
    const list = await r.json();
    for (const rel of list) {
      map.set(rel.tag_name, {
        date: (rel.published_at || rel.created_at || '').slice(0, 10),
        assets: Object.fromEntries(rel.assets.map((a) => [a.name, a.size])),
      });
    }
    if (list.length < 100) break;
  }
  return map;
}

const FIELDS = [
  ['apkUrl', 'apkSha256', 'android', () => 'APK'],
  ['exeUrl', 'exeSha256', 'windows', (f) => (/\.zip$/i.test(f) ? 'ZIP' : /\.msi$/i.test(f) ? 'MSI' : 'EXE')],
  ['linuxUrl', 'linuxSha256', 'linux', (f) => (/appimage/i.test(f) ? 'AppImage' : /\.tar\.(gz|xz|zst)$/i.test(f) ? 'tar.gz' : 'Linux')],
  ['linuxDebUrl', 'linuxDebSha256', 'linux', () => '.deb'],
  ['linuxPacmanUrl', 'linuxPacmanSha256', 'linux', () => 'Arch package'],
];

function parseHistory(md) {
  const releases = [];
  for (const line of String(md || '').split('\n')) {
    const h = line.match(/^##\s+v?([^\s·]+)(?:\s*·\s*(\d{4}-\d{2}-\d{2}))?/);
    if (h) releases.push({ version: h[1], date: h[2] || '', notes: [] });
    else if (/^\s*[-*]\s+/.test(line) && releases.length) releases.at(-1).notes.push(line.replace(/^\s*[-*]\s+/, '').trim());
  }
  return releases;
}

const registry = await loadRegistry();
const entries = Array.isArray(registry) ? registry : registry.apps;

let previous = new Map();
try {
  const old = JSON.parse(await readFile(path.join(out, 'catalog.json'), 'utf8'));
  for (const a of old.apps) for (const b of a.builds) previous.set(b.url, b);
} catch { /* first run */ }

let releases = null;
try {
  releases = await loadReleases();
} catch (e) {
  console.warn(`! ${e.message}. Keeping sizes and dates from the previous catalog.json.`);
}

await mkdir(path.join(out, 'icons'), { recursive: true });
const iconDir = path.join(repo, 'icons');
const icons = new Set();
if (existsSync(iconDir)) {
  for (const f of await readdir(iconDir)) {
    if (!f.endsWith('.png')) continue;
    await copyFile(path.join(iconDir, f), path.join(out, 'icons', f));
    icons.add(f.replace(/\.png$/, ''));
  }
}

// Screenshots kept in this repo are served by the site itself; anything else stays a remote link.
await mkdir(path.join(out, 'shots'), { recursive: true });
async function localShot(url) {
  const m = url.match(/^https:\/\/raw\.githubusercontent\.com\/rykergrey\/RykerSoft\/[^/]+\/(screenshots\/.+)$/);
  if (!m) return url;
  const rel = decodeURIComponent(m[1]);
  const src = path.join(repo, rel);
  if (!existsSync(src)) return url;
  const name = rel.replace(/^screenshots\//, '').replace(/\//g, '--');
  await copyFile(src, path.join(out, 'shots', name));
  return `shots/${encodeURIComponent(name)}`;
}

const slug = (s) => s.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '');
const apps = new Map();
for (const e of entries) {
  const id = e.id || slug(e.name);
  const builds = [];
  for (const [urlKey, shaKey, os, format] of FIELDS) {
    const url = (e[urlKey] || '').trim();
    if (!url) continue;
    const m = url.match(/\/download\/([^/]+)\/([^/]+)$/);
    const tag = m ? decodeURIComponent(m[1]) : '';
    const file = m ? decodeURIComponent(m[2]) : url.split('/').pop();
    const rel = releases?.get(tag);
    const old = previous.get(url);
    builds.push({
      os,
      format: format(file),
      url,
      file,
      version: (tag.match(/-v(\d[\w.]*)$/) || [])[1] || e.latestVersionName,
      size: rel?.assets[file] ?? old?.size ?? null,
      date: rel?.date || old?.date || '',
      sha256: e[shaKey] || '',
    });
  }
  const shots = Array.isArray(e.screenshots) ? e.screenshots : String(e.screenshots || '').split(',');
  const item = {
    id,
    name: e.name,
    kind: e.isGame ? 'game' : 'app',
    version: e.latestVersionName,
    date: e.releasedAt || '',
    tagline: e.tagline || '',
    description: e.description || '',
    features: e.features || [],
    pro: e.pro || '',
    icon: icons.has(id) ? `icons/${id}.png` : '',
    shots: await Promise.all(shots.map((s) => s.trim()).filter(Boolean).map(localShot)),
    packages: [e.packageName],
    web: (e.webUrl || '').trim(),
    builds,
    releases: parseHistory(e.updatesHistory),
  };
  const first = apps.get(id);
  if (!first) { apps.set(id, item); continue; }
  // A second entry with the same id is another platform of the same product.
  if (item.name.length < first.name.length) first.name = item.name;
  first.packages.push(...item.packages.filter((p) => !first.packages.includes(p)));
  first.builds.push(...item.builds);
  if (!first.web) first.web = item.web;
  first.shots.push(...item.shots.filter((s) => !first.shots.includes(s)));
  if (item.date > first.date) first.date = item.date;
}

const order = { android: 0, windows: 1, linux: 2 };
const list = [...apps.values()];
for (const a of list) {
  a.builds.sort((x, y) => order[x.os] - order[y.os]);
  if (!a.date) a.date = a.builds.map((b) => b.date).sort().at(-1) || '';
}

await writeFile(path.join(out, 'catalog.json'), JSON.stringify({ generatedAt: new Date().toISOString(), apps: list }));
const n = (os) => list.filter((a) => a.builds.some((b) => b.os === os)).length;
const unsized = list.flatMap((a) => a.builds).filter((b) => b.size == null).length;
console.log(`catalog.json: ${list.length} apps (${n('android')} Android, ${n('windows')} Windows, ${n('linux')} Linux), ${icons.size} icons` + (unsized ? `, ${unsized} files without a size` : ''));
