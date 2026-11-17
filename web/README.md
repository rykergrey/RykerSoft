# RykerSoft website

The desktop and phone-browser way into the catalog. Static files: React, Tailwind and Base UI, built with Vite.
Design rules are in `../DESIGN.md`.

## Run it

```bash
cd web/site
pnpm install
pnpm dev        # builds catalog.json, then serves http://localhost:5173
pnpm build      # type-checks and writes dist/
```

Add `?as=windows`, `?as=linux` or `?as=android` to see the site as another system would. `?os=linux` opens with the
system filter set.

## Where the data comes from

`scripts/build-catalog.mjs` reads `registry.json` and writes `public/catalog.json`, `public/icons/` and
`public/shots/`. The registry stays the only source of truth; the script adds what it does not carry:

- each file's size and release date, from the GitHub release list of `rykergrey/RykerSoft-APKs`;
- each file's version, from the release tag in its URL;
- each web edition's version, from `web-edition.json` at its `webUrl` (see `apps/README.md`);
- entries that share an `id` merged into one product.

If GitHub cannot be reached, sizes and dates carry over from the previous `catalog.json`. Set `GITHUB_TOKEN` to lift
the anonymous rate limit.

## Web editions

Apps built from web code also run in the browser at `https://<id>.rykersoft.rykergrey.com`. Building and deploying
them is separate from this site: see `apps/README.md`. The site only links to them, through `webUrl` in the registry.

## Account and administration (`#/account`)

The Account button opens sign-in (Google, or an older password account with Google linking), the same Firebase
project and Firestore records the Android hub uses. A signed-in user sees their own Pro access. The administrator
(the verified email named in `firestore.rules`, signed in with Google in this session) also gets:

- **Pro access**: every account against every published Pro app, one switch per cell. Each switch writes
  `users/<uid>/entitlements/apps` and shows what the server stored.
- **Provider keys**: one form per app, with fields from `appCapabilities/<package>`. Saved keys are never read back
  into the page; a blank field keeps the saved key.

Firebase's public web config is in `src/lib/hub.ts`; `firestore.rules` does the enforcing. The SDK is loaded only
when the account page opens. Google sign-in works only on domains listed under Firebase Authentication → Settings
→ Authorized domains; `rykersoft.rykergrey.com` and `localhost` are listed (added 2026-11-11).

## Hosting on a VPS

The site is `dist/` behind any static web server. Hash routing (`#/app/<id>`) means no rewrite rules.

To keep the live site current without rebuilding, refresh only the catalog on a timer:

```bash
REGISTRY_URL=https://raw.githubusercontent.com/rykergrey/RykerSoft/main/registry.json \
CATALOG_OUT=/var/www/rykersoft \
node scripts/build-catalog.mjs
```

With `REGISTRY_URL` set, the script reads the published registry; icons and this repo's screenshots are copied from
the checkout it runs in, so pull the repo first when those change.

## Live deployment (set up 2026-11-10)

Address: `rykersoft.rykergrey.com`, on the Hostinger VPS.

| Piece | Where |
| --- | --- |
| Site files | `/var/www/rykersoft` (owner `rykersoft:www-data`) |
| Web server entry | `/etc/nginx/sites-available/rykersoft` |
| Repo checkout used for the catalog | `/opt/rykersoft/repo` (public clone, pulled by the timer) |
| Catalog refresh | `rykersoft-catalog.timer`, every 15 minutes: `git pull`, then `build-catalog.mjs` into `/var/www/rykersoft` |
| Web editions | `/var/www/rykersoft-apps/<id>` behind `/etc/nginx/sites-available/rykersoft-apps` (see `apps/README.md`) |

A catalog change pushed to `main` reaches the site within about 15 minutes with no deploy. A change to the site's
own code needs a new build uploaded:

```bash
cd web/site && pnpm build
rsync -az --delete --exclude catalog.json --exclude icons --exclude shots dist/ root@<vps>:/var/www/rykersoft/
```
