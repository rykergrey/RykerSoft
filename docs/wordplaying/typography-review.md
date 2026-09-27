# Tile lettering review · September 27, 2026

Development comparison: run `npm run dev` and open `/scripts/typography-preview.html`. This is a review sheet, not a new settings screen or a reward grant. It imports `BuilderTileFace` and Bungle's actual `Tile`, with the live tile styles. Candidate font weights and optical scales are explicit, not randomized. All six families were already requested by the app's existing Google Fonts stylesheet; no new font dependency was added to the shipped app.

## What was checked

- Scramble at 22, 36, and 52 CSS pixels: W with stack count 3, Q worth 10, an assigned blank A worth 0, and M on a persistent TW square with stack count 5.
- Bungle at 44 and 60 pixels: compound Qu, a selected W, narrow I, and point-value badges.
- Wide/narrow glyph sample: `MWQ O0 I1 Z2`.
- Browser inspection at desktop and 320-pixel phone width. Font loading completed before review.
- Conservative bounding-box intersection checks between glyphs and all badges. These are a useful guard, not a substitute for visual inspection or testing the full alphabet on Android.

The study exposed existing cramped annotation spacing. Shared Scramble faces now reserve extra lower-corner room when stacked. Bungle letters scale to their tile width, value badges have a separate typeface and reserved upper area, and Qu's u uses a proportional line height. These improvements apply to live games and previews, not just the study.

## Candidates and findings

| Working reward name | Typeface / weight / optical scale | Findings |
| --- | --- | --- |
| Classic | Abel / 400 / 1.00 | Reference letterform. Open, narrow, familiar; no measured badge collisions in the revised sample. The live app uses heavier synthetic weights. |
| Vector Type | Orbitron / 700 / 0.82 | Strongest angular sci-fi direction. Works well at medium/large sizes; its wide M still intersects the tiny TW label on a stacked 22px tile. Hold until small-size metrics are resolved. |
| Signal | Teko / 600 / 1.08 | Condensed scoreboard character; visibly distinct from Abel without consuming corner space. No measured collisions. Strong candidate for a first reward, pending full-alphabet/device review. |
| Cabinet | Audiowide / 400 / 0.82 | Softer, wide arcade lettering. Attractive at larger sizes, but the same stacked 22px TW conflict as Vector Type. Hold; do not squeeze it down until it loses its character. |
| Soft Keys | Rubik / 700 / 0.91 | Chunky console-key feel and clear small letters. No measured collisions. Strong usability-oriented candidate, pending full-alphabet/device review. |
| Pixel ROM | Press Start 2P / 400 / 0.64 | Fits the sample geometry only at a much smaller scale; texture/detail becomes fussy on dense boards. Exclude from the first tile-font rewards. Better suited to an occasional heading or collectible title. |

No candidate was added to earned rewards. **22 total options / 18 earnable upgrades remain unchanged.**

## Release gate for a lettering reward

Review the full alphabet, all point values, assigned blanks, Qu, maximum supported stack counts, bonus labels, zoom levels, rotation modifiers, both game modes, selected/unselected states, every finish, and reduced motion. Keep points and stack counts in the stable UI face. Confirm native-device rendering, font licensing/bundling and offline fallback before release; an online Google Fonts load alone is not an offline guarantee.

Prefer a small first batch of genuinely distinct faces. Do not add fonts just to inflate the reward count. Tile-edge profiles and one-shot placement/selection motions can provide more meaningful later upgrades without multiplying arbitrary color variations.
