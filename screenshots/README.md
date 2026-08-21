# Hub screenshots (public)

Public gallery images for the RykerSoft Application Manager.

App source repos may stay **private**. Hub screenshots live here so the manager can load them without auth.

## Layout

```
screenshots/
  <app-slug>/
    01-….jpg|png|webp
    02-….jpg|png|webp
    …
```

| App slug | Registry `packageName` |
|----------|------------------------|
| `informant` | `com.informant.app` |
| `rush` | `com.rykersoft.rush` |
| `synthing` | `com.rykersoft.synthing` |
| `superthinking` | `com.rykersoft.superthinking` |
| `bettertracking` | `com.rykersoft.bettertracking` |
| `roadtripper` | `com.rykersoft.roadtripper` |

## Naming

- Sequential: `01-label.ext`, `02-label.ext`, …
- Formats: `.png`, `.jpg`, `.jpeg`, `.webp`
- Minimum **3** images per app for hub cards

## Registry URLs

```
https://raw.githubusercontent.com/rykergrey/RykerSoft/main/screenshots/<app-slug>/<filename>
```

Update that app’s `screenshots` array in `registry.json` on `main` after adding or replacing images.

## Migrating from an app repo

Copy image files from the app’s own `screenshots/` folder into `screenshots/<app-slug>/` here (same filenames when possible), then point `registry.json` at the RykerSoft raw URLs above. Keep source-repo `screenshots/` for local/docs if you want; the hub only needs this public tree.
