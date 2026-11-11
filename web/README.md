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
- entries that share an `id` merged into one product.

If GitHub cannot be reached, sizes and dates carry over from the previous `catalog.json`. Set `GITHUB_TOKEN` to lift
the anonymous rate limit.

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
