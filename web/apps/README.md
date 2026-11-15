# Web editions

Apps that are built from web code also run in the browser, each at its own address:
`https://<id>.rykersoft.rykergrey.com`. The catalog links to them through `webUrl`, and the website shows a lit
globe mark and an **Open in browser** button.

Each app has its own address on purpose. Browsers keep saved data, sign-in and microphone or camera permission
per address, so one app can never read or overwrite another's. Do not move an app to a different address once people
use it: what they saved in the browser stays behind at the old one.

## What is here

| File | Job |
| --- | --- |
| `apps.json` | The list of web editions: catalog `id`, package, source folder, pinned commit, build settings |
| `build.mjs` | Builds each app from its pinned commit into `out/<id>/`, and its server, if it has one, into `out-services/<id>/server.mjs` |
| `patches/<id>/*.patch` | Web-only fixes a release does not have yet, applied to the source before building |
| `smoke.mjs` | Loads each app in headless Chromium at desktop and phone size and reports what breaks |
| `deploy.mjs` | Uploads `out/` to the VPS, installs each app's server as a service, and writes the web server config and certificate |

## Release a web edition

Run these from the repository root, on the machine that holds the app sources (folders beside this repository, or
set `RYKERSOFT_WORK`).

```bash
# 1. Point apps.json at the released commit: set "ref" to the release tag or commit.
node web/apps/build.mjs storytime                     # build one app; no ids builds all
PLAYWRIGHT_DIR=<playwright package> node web/apps/smoke.mjs storytime
RYKERSOFT_VPS=root@<vps> node web/apps/deploy.mjs storytime
node web/apps/smoke.mjs --live storytime              # the same check against the live address
```

`deploy.mjs` reads `RYKERSOFT_VPS_KEY` for the SSH key file when the default key is not the right one.

The website reads each edition's version from `https://<id>.rykersoft.rykergrey.com/web-edition.json` and marks a
web edition that is behind the catalog version, the same way it marks an old download. So after an app release,
rebuild and deploy its web edition too.

## Add an app

1. Add an entry to `apps.json`. `id` is the catalog id and becomes the address.
2. `node web/apps/build.mjs <id>`, then `smoke.mjs <id>`. Open the build locally and try sign-in: `localhost` is
   an authorized sign-in domain in every project.
3. `RYKERSOFT_VPS=root@<vps> node web/apps/deploy.mjs <id> --cert`. `--cert` extends the certificate to the new
   address. The wildcard DNS record `*.rykersoft` already points every address at the VPS.
4. Add `"webUrl": "https://<id>.rykersoft.rykergrey.com"` to the app's entry in `registry.json` and push.

## Rules the builds follow

- **Pinned source.** A build exports the pinned commit into a scratch folder (`~/.cache/rykersoft-web`). Work in
  progress in the app's checkout never reaches the public build. ComicCraft.ing is the exception: its source folder
  is not a git repository, so the folder is copied as it stands.
- **No local env files.** `.env` and `.env.*` files are never read. Anything a public build needs is in
  `apps.json`: the top-level `env` (the hub's public sign-in settings) or the app's own `env`. This keeps a private
  key in someone's `.env` out of a public bundle.
- **Patches fail loudly.** A patch that no longer applies stops the build. When an app release includes the fix,
  delete the patch.
- **Nothing cached without a check.** The server sends `Cache-Control: no-cache` for every file, so browsers
  revalidate and a new deploy shows up on the next load.

## Apps with their own server

An app can bring a server for things a page cannot do alone, such as multiplayer rooms. In `apps.json`:

```json
"service": { "entry": "server/host.ts", "port": 4312, "env": { "NAME": "public value" } },
"proxy": [
  { "path": "/rooms", "upstream": "http://127.0.0.1:4312/rooms", "websocket": true },
  { "path": "/api/", "upstream": "http://127.0.0.1:4312/api/" }
]
```

- `build.mjs` bundles `entry` and its dependencies into one file, so the VPS needs only Node.
- `deploy.mjs` installs it as the systemd unit `rykersoft-app-<id>`: its own user (`rykersoft-apps`), loopback only,
  a read-only system, no home folders, 300 MB of memory at most, one writable folder at
  `/var/lib/rykersoft-apps/<id>`. It gets `PORT`, `HOST=127.0.0.1`, `TRUST_PROXY=1`, `DATA_DIR` and the `env` values.
- Pick a port no other entry uses. Nothing but nginx reaches it; the firewall leaves only 80, 443 and SSH open.
- Values in `env` are public (they are in this repository). A secret goes in `/etc/rykersoft-apps/<id>.env` on the
  VPS. No current service needs one.
- A server behind the proxy sees every caller as `127.0.0.1`. One that limits requests per address must read the
  `X-Real-IP` header when `TRUST_PROXY=1`, as Earbound's patch and Close Call's host do.

| Service | Port | What it holds |
| --- | --- | --- |
| `rykersoft-app-earbound` | 4311 | Party rooms, in memory. Nothing stored |
| `rykersoft-app-close-call` | 4312 | Online match rooms, in memory; song lookup; YouTube link details; Pro judging. No database, no YouTube key |
| `mechameme.service` (not managed here) | 4186 | MechaMeme Community and free captions |

Check one with `curl https://<id>.rykersoft.rykergrey.com/api/health`. A restart ends the matches in progress.

## Sign-in

Sign-in runs through Firebase in the browser, with a Google popup. A domain must be authorized in each Firebase
project an app signs in to (Firebase console, Authentication, Settings, Authorized domains). Authorizing
`rykersoft.rykergrey.com` covers every app address under it. It is authorized in:

- `rykersoft-abe84` (the hub account and Pro access, used by every app with Pro)
- `superthinking-1e5f4` (SuperThink.ing accounts and vault sync)
- `superstorycrafting` (Storytime online rooms)
- `wordplaying-5eec3` (WordPlay accounts and online play)

Pro works as on Android: after sign-in the app reads the account's grant and the protected provider keys from
Firestore, and the existing rules decide. Nothing about Pro is stored on the VPS.

## On the VPS

| Piece | Where |
| --- | --- |
| App files | `/var/www/rykersoft-apps/<id>/` (owner `rykersoft:www-data`) |
| Web server entry | `/etc/nginx/sites-available/rykersoft-apps` and `/etc/nginx/snippets/rykersoft-app-static.conf`, both written by `deploy.mjs`; do not edit them on the server |
| Certificate | `rykersoft-apps` (one certificate listing every app address; certbot renews it and reloads nginx) |
| Check port | `127.0.0.1:8431` serves each app over plain HTTP by `Host` header, for checks from the VPS itself |

MechaMeme's address also passes `/api/` to the existing `mechameme.service`, so the browser talks to one address
and needs no cross-site permission.

## What runs on the web, and what does not (2026-11-15)

| App | Web edition | Notes |
| --- | --- | --- |
| SuperThink.ing | yes | Patched: the Google button failed in a browser (`patches/superthinking`) |
| BetterTracking | yes | |
| WordPlay | yes | |
| Photocraft.ing | yes | |
| Storytime | yes | Patched: Drive backup authorizes through Storytime's Firebase project (`patches/storytime`), the same Google project as Android, so backups are shared |
| ComicCraft.ing | yes | Built from a folder copy; the source is not in git |
| MechaMeme | yes | Patched the same way as SuperThink.ing (`patches/mechameme`) |
| The Guile Family | yes | The offline games. Its sign-in and AI drafts exist only in the Android app |
| Field Recorder | yes | Patched (`patches/field-recorder`): with no key and no Pro it uses the browser's own speech recognition (Chrome, Edge, Safari), and any microphone the browser offers |
| Earbound | yes | Its party-room server runs on the VPS (`patches/earbound`), so guests join with the link or code on any connection. The host's device plays the sounds, as in the app: it is a same-room party game. Pass the phone works offline |
| Close Call | yes | Built from `main`: one-device play as in the app, plus online rooms on the VPS. Pasted YouTube links in place of search, no campaign database. Name That Song takes typed answers where the browser has no speech recognition or microphone |
| INFORMANT | not yet | Needs its Node server per user, and the 1.4.13 source is not committed |
| FreeBall.ing | not yet | Godot game; needs a web export preset, the web export template and a sign-in bridge |
| Umm, Rush | not assessed | Source not found on this machine |
| yoink. | no | Downloads and edits video with native tools; a public server doing that is not something to host |
| Hyperscribe Mobile, Synthing, RykerFM1 | no | Native Android code (Kotlin, C++, USB) |
| Hyperscribe Desktop | no | Python desktop app |
| RykerSoft Hub | n/a | The website is its web form |
