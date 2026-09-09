# Release notes

## v1.4.1
- Moved rolling calorie progress into a Today / rolling-average switch on the calorie widget
- Replaced the always-visible full-width planner with a compact period summary showing average intake, combined balance, coverage, and the selected day
- Moved what-if plans, maintenance context, and daily history into an on-demand Plans & details modal
- Kept partial-window warnings prominent and avoided treating incomplete shortfalls as success

## v1.4.0
- Added a Today / rolling-average switch to the Macros & Nutrients widget so period trends are useful before the window is complete
- Added fixed goal, minimum, limit, and range lines, plus a cyan selected-day marker and bright overage segments
- Shows average variance, cumulative period variance, usable-day coverage, and the count of individual days outside each goal
- Treats nutrient goals by meaning: protein minimums reward adequate intake, maximums expose overages, ranges keep both boundaries, and neutral aims avoid false deficiency warnings
- Keeps incomplete, unknown, unresolved, and supplement-only days from creating misleading nutrient averages

## v1.3.0
- Redesigned Rolling Calorie Balance that surfaces overages from the first usable day instead of waiting for a full week
- Daily what-if planning for keeping the current goal, balancing sooner, spreading a difference across days, or entering a custom intake
- Clear separation between calorie-goal variance and estimated maintenance, with partial-log and incomplete-data warnings
- Per-day rolling history, reconstructed historical goals, current-day projections, safe calorie floors, and opt-in automatic adjustments
- Improved assistant alerts, nutrition reporting periods, reminder reliability, and library data handling
- Android, Windows x64 portable, Linux x64 AppImage, and Debian release builds

## v1.2.0
- Personalized nutrition chat retrieves current intake, goals, preferences and library foods on demand
- Optional Auto / Off / On web research with cited sources
- Nutrition reports without AI: nutrient filters, food rankings, consumption patterns, charts and CSV export
- Text and image library drafts with evidence labels and reliable reviewed changes
- Explicit nutrient minimums, maximums, ranges, favorites and food exclusions
- Bounded chat context, improved historical nutrition calculations and safer serving conversions
- Windows x64 portable and Linux x64 AppImage / Debian builds

## v1.1.5
- Replace app-funded day/week/month Health Coach generation with portable prompts for the user's own chatbot
- Default to Perplexity, with ChatGPT, Google Gemini, and copy-to-any-chatbot options
- Remove automatic background monthly AI reports while preserving access to previously saved reports
- Include chronological logs, notes, nutrients, custom values, recipe portions, targets, and adaptive context in each coaching prompt

## v1.1.4
- Restore **Continue with Google** in Profile settings and remove obsolete password/create-account controls
- Bundle the canonical RykerSoft Hub Firebase configuration so release builds no longer depend on an unpublished local environment file
- Use Android Credential Manager with the Hub web client ID, preserving the existing release signing identity and Pro entitlements

## v1.1.3
- Restore a complete, source-backed release after an unreleased direct-device build advanced the Android version
- Preserve the trusted Android signer and provide a monotonic update path without removing local app data
- Rebuild the current stable application bundle and synchronize RykerSoft hub metadata

## v1.1.1
- Add-from-library (staging tray and Library magic search) now uses each item’s library-defined unit (pcs, g, srv, etc.) instead of adjectives from the phrase (e.g. “three whole eggs” → 3 pcs)
- Library tab keeps search text, scroll position, and expanded groups when you switch to the staging tray and back

## v1.1.0
- RykerSoft AI unlock: sign in with your RykerSoft account under **Profile → API Keys** to sync Gemini and Groq keys after unlocking bettertracking in the RykerSoft App Manager
- AI features (Quick Log, AI Architect, chat, Coach Analysis, transcription) are now unlock-gated; manually entered keys still work and take priority
- All tracking, journal, library, and reminder features remain fully available without the unlock

## v1.0.2
- Release-signed APK for the RykerSoft hub
- Display name changed from bettertrack.ing to bettertracking

## v1.0.1
- First RykerSoft hub release (`com.rykersoft.bettertracking`)
