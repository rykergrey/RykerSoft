# RykerSoft Hub design

One design covers both hub surfaces: the Android app (`app/`) and the website (`web/site/`). The visual direction
below was approved from mockups on 2026-11-10.

## The world: Hard-Edge Neon

A workshop bench at night. Tinted blue-black graph paper, parts laid out in bordered trays, one loud device.

- **Canvas**: graph paper. Background `#0f1015` with a 32 px grid in `#15161d`.
- **Edges**: 2 px borders on anything that holds content or can be pressed.
- **The loud device**: a hard, unblurred black shadow down and to the right (3 px on controls, 5 px on cards and
  dialogs). Nothing else glows, blurs or animates for decoration.
- **Shapes**: three radii for everything: 0 (rows, cards, groups), 2 px (controls, tags), 4 px (icon tiles, menus,
  dialogs).
- **Type**: Chakra Petch for names, IBM Plex Sans for reading, JetBrains Mono for data, labels and buttons. All
  three are bundled (Android `res/font`, web `@fontsource`); nothing loads from a font service. The licences are in
  `app/src/main/assets/licenses/`.
- **Dark only.** The world is the dark sheet; there is no light theme.

## Colour roles

Each accent means one thing. If a new need does not fit a role, it stays neutral.

| Role | Value | Means |
| --- | --- | --- |
| action | `#ffd600` on ink `#14130a` | The primary action and the active path: Update, the selected filter or tab, the file for your system |
| brand | `#ff2a85` | Identity only: the logo's offset and the mark before a section name |
| ok | `#00e676` | Free, installed and current, Pro switched on, a published checksum |
| link | `#00e5ff` | Links and keyboard focus |
| game | `#b08cff` | The Games kind |
| warn | `#ffaa33` | A file that is behind the current version; an install waiting on the user |
| danger | `#ff3366` | Destructive actions and errors |

Neutrals: bg `#0f1015`, surface `#191a23`, raised `#222431`, border `#3c3f52`, soft border `#2a2c3a`, text `#f2f3f7`,
dim `#a9abbb`, faint `#7c7f93`.

## Type scale

| Role | Android | Web |
| --- | --- | --- |
| display (an app's name on its page) | 24 sp | `text-display` |
| title (a name in a list) | 17 sp | `text-title` 18 px |
| body | 14 sp | `text-body` 14.5 px |
| small body | 13 sp | `text-sm` 13 px |
| data, label, button | 12 sp mono | `text-label` 12 px, `text-meta` 11 px |

Nothing the user must read is under 12 sp on Android or 11 px on the web.

## Where the tokens live

- Android: `app/src/main/java/com/rykersoft/appmanager/ui/theme/HubTokens.kt` (`Hub.C`, `Hub.T`, `Hub.S`, `Hub.D`).
  The kit is `ui/kit/HubKit.kt`. Screens hold no raw colour, size or shape.
- Web: `web/site/src/styles.css` (`@theme`). The stock Tailwind palette is removed, so only role classes exist. The
  kit is `web/site/src/components/kit.tsx`; menus come from Base UI.

## Named rules

- **The Real Content Rule.** A card, row or page shows only what the catalog says. No stock description, no
  placeholder screenshot, no invented changelog. An app with nothing published shows an honest empty line.
- **The Release Date Rule.** "Latest release" sorts by the catalog's `releasedAt` date. Version numbers are never
  compared across apps.
- **The Behind Rule.** Every file knows its own version from its release tag. A file older than the app's current
  version is marked in `warn` wherever it is offered.
- **The System Rule.** The system filter has four choices: All (the default), Android, Windows, Linux. The website
  detects the visitor's system and offers that file first, but never hides the others.
- **The One Yellow Rule.** In a row or bar there is at most one `action` button. On the Android catalog only Update
  is yellow; Install and Open are plain, so yellow always means "this needs you".
- **The Thumb Rule (Android).** State reads at the top (logo, status line, kind filter). Verbs sit in a dock at the
  bottom: search, system, sort, settings; on an app page, the install action.
- **The Applied Now Rule.** Settings is a page of row groups. Each change applies when it is made. There is no Save
  and no Cancel.
- **The Red Last Rule.** Resetting the catalog is the last group on the settings page, in red, and asks first.
- **The Free Rule.** A feature is Pro when it could cost Ryker a lot of money if many people used it: anything on a
  paid API key (AI, transcription, paid lookups) and anything that stores user data on his cloud account (Hyperscribe
  Sync). Everything else is free. Every Pro note, tag and explanation in both surfaces follows this definition (Ryker,
  2026-11-10).
- **No guides for now.** User guides are not shown in either surface until they have been rewritten. The files under
  `app_docs/` and the distribution repository's `docs/` are kept for that rewrite.

## App icons

Every catalog entry has a 512 px icon at `icons/<id>.png`. Icons drawn for the hub (sources in `icons/src/`) use a
flat colour field, a bold geometric mark and the hard offset shadow. Apps that ship their own icon use it unchanged.

## Verified

- Website: built and captured in headless Chromium at 1440 px and 400 px wide (2026-11-10).
- Android: 32 JVM tests pass; the three screens were rendered with Robolectric at Pixel 8 size, including font scale
  1.3. Not yet checked on a physical phone.
