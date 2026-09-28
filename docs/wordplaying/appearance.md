# Appearance system

Open the palette button on Home, or **Appearance** in Scramble's match menu. The studio previews coordinated palettes, tile finishes, interface styles, and motion separately. Every piece is listed in horizontally scrollable collections. Locked pieces show a neutral category silhouette, lock, name, and play-or-win requirement, but never their actual colors, texture, effect, or live preview. Applying an earned combination is explicit; dismissing a preview leaves the current look alone. Each category shows its unlocked percentage, including its starter, with exact counts available to assistive technology.

## Visual contract

The visual direction is 80s/90s arcade, vector-grid sci-fi, and synthwave: luminous saturated accents, near-black screens, sharp edges, and controlled neon glow. Arcade is the default orange/cyan palette. Each palette supplies a coordinated background, panel, raised surface, border, secondary text, action fill with contrasting label ink, new-letter accent, distinct waiting-player planning accent, and contributing-word outline. Finishes alter neutral tile faces without changing bonus meanings. These constraints make every offered combination compatible; arbitrary per-element color picking is intentionally unavailable.

The eight palettes are Arcade (orange/cyan), Gridline (electric cyan/orange), Hotline (hot pink/yellow), Outrun (ultraviolet/cyan/pink), Phosphor (laser green/blue), Voltage (yellow/violet), Overdrive (cobalt/lime), and Plasma (orchid/mint). The first five preserve the existing saved IDs `ember`, `honey`, `copper`, `dusk`, and `tide`; they are retuned and renamed, not additional unlock purchases. Existing thresholds and progress remain intact.

Six tile finishes offer Graphite, Vector Glass, Scanline, Pixel Matrix, Wireframe, and Obsidian. Fine grids and CRT scanlines are stationary, never flickering. Five interface styles independently customize action buttons, match-card borders, and control frames: Arcade (solid neon), Vector (dark outlined controls), Double Line (terminal borders), Cut Corner (clipped sci-fi action buttons), and Grid (subtle wire grids). Dark styles switch labels to luminous accent text; solid styles use dark ink. The cut-corner focus indicator sits inside the control so it is not clipped. Old three-axis saves receive Arcade interface styling automatically.

Continue, Create Match, Start Match, and Submit share the same primary-action treatment. Selected game cards, links, outlines, and subtle fills follow the selected accent. The wordplay.ing logo intentionally retains its orange-red brand mark. Warning, error, success, and bonus colors remain semantic, not palette decorations. Secondary match details use brighter theme-aware neutrals. The studio previews the full palette locally, including matching Continue/Create Match samples; previewing never equips or unlocks a look.

| Meaning | Default treatment |
| --- | --- |
| Background / empty cell / tile | Near-black / dark charcoal / lighter graphite |
| Active selection and newly committed letters | Luminous orange by default; follows palette |
| Planning while waiting | Ice cyan by default; dashed ghosts, never white selection |
| All tiles in scored words | Cool outline; existing letters stay off-white |
| Stack | Small layered mark/count opposite the letter's point value |
| DL / TL | Electric teal / violet |
| DW / TW | Yellow / pink |

Bonus hues remain stable across palettes. Empty bonus squares show only their color, while the legend above the board shows four matching DL, TL, DW, and TW swatches without expanded text. The swatches use the exact same fill and border treatment as their board squares. The turn ticker sits above the letter tray. Full bonus names remain in accessible square and swatch labels. Lasting Bonuses keep an outer premium outline under a tile; word contribution uses a separate inner outline. Hidden bonuses remain invisible until discovered. Dashed draft outlines and stack marks distinguish other board states.

Gentle is the restrained default animation. Halo and Glint give the player's Scramble letters distinct entrances as soon as a tray letter is placed on the board, including moved drafts and Falling drops. They also set different letter transition tempos without changing scoring or reveal timing. Reduced-motion preferences disable these effects. Opponent entry animations remain limited to actual new placements; contributing tiles receive an outline instead of falling into the board again. Bungle's neutral tiles use the same finish and palette, selected letters and trace paths use the chosen accent, and each selection plays one finite, visibly distinct settle, halo, or glint on the glyph layer. Gameplay bonus, swap, territory, and reroll signals retain their meanings. The score badge keeps a stable UI typeface; Qu's smaller u no longer inherits an oversized fixed line height.

Tap the preview board or its Bungle/Scramble buttons to switch games. Both use real tile components and shared live CSS. The short demonstration runs once per change or Replay, with no looping idle animation; reduced motion shows the completed state immediately. Scramble shows placed versus contributing letters, a planning ghost, a stack, points, and an occupied bonus. Bungle shows Qu, letter values, and a traced word. Larger detail samples expose palette roles, tile texture, interface framing, and the current game's letter motion; the selected category is highlighted. The preview never changes a match or earns progress.

## Rewards

The catalog has **22 pieces: 4 starters and 18 earnable upgrades**. Eight palettes × six finishes × five interface styles × three motion choices give 720 curated combinations. Every piece can be earned through completed matches; online wins are an optional faster route. Starter pieces are the Arcade palette, Graphite tiles, Arcade interface, and Gentle motion.

| Piece | Completed matches | Or online wins |
| --- | ---: | ---: |
| Gridline palette | 3 | 1 |
| Vector Glass tiles | 5 | 2 |
| Vector interface | 7 | 2 |
| Halo motion | 10 | 3 |
| Hotline palette | 12 | 4 |
| Scanline tiles | 15 | 5 |
| Double Line interface | 18 | 6 |
| Pixel Matrix tiles | 22 | 7 |
| Outrun palette | 25 | 8 |
| Glint motion | 30 | 10 |
| Cut Corner interface | 35 | 11 |
| Wireframe tiles | 42 | 13 |
| Phosphor palette | 50 | 15 |
| Grid interface | 60 | 18 |
| Voltage palette | 70 | 20 |
| Obsidian tiles | 80 | 24 |
| Overdrive palette | 95 | 28 |
| Plasma palette | 125 | 38 |

Local completion requires actual play and a finished whole match, not a draft, handoff, or intermediate round. Stable receipts prevent repeat saves from earning again. Trusted Bungle competitive totals, Bungle live records, and Scramble ruleset statistics are counted separately to avoid overlap. Totals retain their high-water mark so delayed snapshots cannot revoke rewards.

This first implementation stores selections and local receipts on the device, separated by registered profile or guest. A local match credits one profile; guest receipts do not transfer on sign-in. Account-backed online statistics rebuild online eligibility on other devices, but selected looks and locally earned progress do not sync between devices. These are optional cosmetic rewards, not a server-authoritative economy.

## Extending the collection

Add a stable ID, label, description, and play/win thresholds to `services/appearanceCatalog.ts`. Palettes must supply all semantic roles, finishes must preserve letter and legend-initial contrast even at their brightest sheen, interface styles must define matching readable label/fill pairs, and effects must implement matching live and preview styles with reduced-motion support. Never recycle IDs. Catalog entries automatically appear in the studio and unlock evaluation; no separate settings page is needed. Use `wp-primary` for new primary actions and the shared accent/soft/outline roles for selections; do not introduce fixed orange literals outside intentional brand art. The Tailwind orange and zinc families bridge older UI into these roles.

Run `npm run verify:appearance`, `npm run verify:builder-ui`, and `npm run verify:builder-score-effects` after changing this system. The appearance suite checks every palette/finish, both action-gradient endpoints and midpoint, secondary text against all three surfaces, stable semantic colors, unlocks, percentages, locked silhouettes, and both game previews. Check a narrow mobile viewport and keyboard interaction as well as the live board. `scripts/appearance-preview.html` offers a development-only fixture using the real match cards, game selector, and tile previews without match/account writes or progress grants. `scripts/typography-preview.html` compares six lettering candidates using shared Scramble faces and real Bungle tiles; see [Typography review](typography-review.md) before promoting a candidate into the catalog.

Expand through distinct design axes rather than near-duplicate hues: curated lettering, tile-edge profiles (bevel, inset, clipped), and event-only motion (a single settle, edge trace, or short light sweep). Each addition needs its own small-board, annotation-clearance, contrast, reduced-motion, and cross-game review. Preserve hitboxes, selection timing, bonus meanings, and score readability. Avoid continuous shimmer, blinking grids, particle showers, random rotation during input, and reward effects that conceal information. Prototype these before setting new thresholds; the current 18-upgrade progression is unchanged.

Lasting Bonuses changes scoring and requires protocol 3 support. Release its backend together with the client; earlier protocol 1/2 matches remain supported. Source changes do not deploy the backend automatically.
