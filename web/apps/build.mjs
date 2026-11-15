#!/usr/bin/env node
// Builds the web edition of each app in apps.json from its pinned release source.
//
//   node web/apps/build.mjs                 every app
//   node web/apps/build.mjs wordplay rush   only these ids
//
// Source is exported from the pinned commit into a scratch folder, so uncommitted work in the
// app's checkout never reaches the public build. Output: web/apps/out/<id>/.
import { execFileSync, spawnSync } from 'node:child_process';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const here = path.dirname(fileURLToPath(import.meta.url));
const manifest = JSON.parse(fs.readFileSync(path.join(here, 'apps.json'), 'utf8'));
const WORK = process.env.RYKERSOFT_WORK || path.resolve(here, '../../..');
const CACHE = process.env.RYKERSOFT_WEB_CACHE || path.join(os.homedir(), '.cache/rykersoft-web');
const OUT = path.join(here, 'out');
const COPY_EXCLUDES = ['node_modules', 'dist', 'android', 'release', 'build', 'desktop', 'electron', '.git', '.env', '.env.*'];

const wanted = process.argv.slice(2);
const apps = manifest.apps.filter((a) => wanted.length === 0 || wanted.includes(a.id));
const unknown = wanted.filter((id) => !manifest.apps.some((a) => a.id === id));
if (unknown.length) fail(`Not in apps.json: ${unknown.join(', ')}`);

function fail(message) {
  console.error(message);
  process.exit(1);
}

function run(cmd, args, opts = {}) {
  const r = spawnSync(cmd, args, { stdio: ['ignore', 'pipe', 'pipe'], encoding: 'utf8', maxBuffer: 64 << 20, ...opts });
  if (r.status !== 0) throw new Error(`${cmd} ${args.join(' ')}\n${(r.stdout || '').slice(-1500)}\n${(r.stderr || '').slice(-1500)}`);
  return r.stdout;
}

function exportSource(app, stage) {
  const source = path.join(WORK, app.source);
  if (!fs.existsSync(source)) throw new Error(`Source folder not found: ${source}`);
  fs.rmSync(stage, { recursive: true, force: true });
  fs.mkdirSync(stage, { recursive: true });
  if (app.ref === null) {
    run('rsync', ['-a', ...COPY_EXCLUDES.flatMap((e) => ['--exclude', e]), `${source}/`, `${stage}/`]);
    return null;
  }
  const commit = execFileSync('git', ['-C', source, 'rev-parse', '--verify', `${app.ref}^{commit}`], { encoding: 'utf8' }).trim();
  const tar = path.join(CACHE, `${app.id}.tar`);
  run('git', ['-C', source, 'archive', '--format=tar', '-o', tar, commit]);
  run('tar', ['-xf', tar, '-C', stage]);
  fs.rmSync(tar);
  return commit;
}

function build(app) {
  const stage = path.join(CACHE, 'src', app.id);
  const commit = exportSource(app, stage);
  // Web-only fixes the release does not have yet live in patches/<id>/*.patch, relative to the
  // source root. A patch that no longer applies fails the build, so it cannot silently drop out.
  const patchDir = path.join(here, 'patches', app.id);
  const patches = fs.existsSync(patchDir) ? fs.readdirSync(patchDir).filter((f) => f.endsWith('.patch')).sort() : [];
  for (const patch of patches) run('patch', ['-p1', '--forward', '--fuzz=0', '-i', path.join(patchDir, patch)], { cwd: stage });
  const dir = path.join(stage, app.dir || '.');
  // Only apps.json may configure a public build: no local env file takes part.
  for (const f of fs.readdirSync(dir)) if (/^\.env(\.(?!example$).+)?$/.test(f)) fs.rmSync(path.join(dir, f));
  const env = { ...process.env, ...(manifest.env || {}), ...(app.env || {}) };
  if (app.install !== false) run('npm', ['ci', '--no-audit', '--no-fund'], { cwd: dir, env });
  if (app.build !== null) run('sh', ['-c', app.build || 'npm run build'], { cwd: dir, env });

  const built = path.join(dir, app.out || 'dist');
  if (!fs.existsSync(path.join(built, 'index.html'))) throw new Error(`No index.html in ${built}`);
  let version = app.version;
  if (!version) version = JSON.parse(fs.readFileSync(path.join(dir, 'package.json'), 'utf8')).version;

  const dest = path.join(OUT, app.id);
  fs.rmSync(dest, { recursive: true, force: true });
  fs.mkdirSync(OUT, { recursive: true });
  fs.cpSync(built, dest, { recursive: true });
  // What is deployed, readable at https://<id>.<domain>/web-edition.json.
  fs.writeFileSync(path.join(dest, 'web-edition.json'), `${JSON.stringify({ id: app.id, version, commit, patches, builtAt: new Date().toISOString() }, null, 2)}\n`);
  const size = run('du', ['-sh', dest]).split('\t')[0];
  return { version, commit, size, patches };
}

let failed = 0;
for (const app of apps) {
  process.stdout.write(`${app.id.padEnd(16)} `);
  try {
    const { version, commit, size, patches } = build(app);
    console.log(`${version.padEnd(9)} ${(commit || 'folder copy').slice(0, 9).padEnd(11)} ${size.padEnd(6)}${patches.length ? ` ${patches.length} patch${patches.length > 1 ? 'es' : ''}` : ''}`);
  } catch (error) {
    failed += 1;
    console.log('FAILED');
    console.error(String(error.message || error));
  }
}
process.exit(failed ? 1 : 0);
