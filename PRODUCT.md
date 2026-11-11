# RykerSoft Hub

RykerSoft is one hobbyist's collection of apps and games, built first for his own use. The hub is how anyone else
gets them.

## Who it is for

- Ryker and his family, who use the apps daily and get Pro features switched on per account.
- Anyone else, who can install every app for free and use its free features with no account.

## What the hub does

- **Android app**: lists every app, shows what is installed and what has an update, downloads and verifies each APK,
  hands it to Android's installer, and holds the RykerSoft account that carries Pro access into the other apps.
- **Website**: the same catalog for desktop. It offers the Windows and Linux files directly and a QR code for
  getting an APK onto a phone. It has no accounts and no tracking.

## What it is not

- Not a store. Nothing is sold. Any feature that does not rely on an API key is free; Pro covers the features that
  do, and is granted by hand, per account, per app.
- Not a documentation site. User guides are out until they have been rewritten.

## One source of truth

`registry.json` on `main` is the catalog for both surfaces. See `skills/rykersoft-deploy/references/deployment.md`
for its fields.

## Standing facts

- Technical apps for power users: dense, truthful, keyboard and thumb friendly. See `DESIGN.md`.
- The physical phone is the judge for Android work. Release builds are signed locally.
