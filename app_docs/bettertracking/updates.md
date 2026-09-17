# Release notes

## v1.7.0
- Add mobile tap glows, independent light trails to the tray, and arrival pulses
- Show persistent tray selection checks across multiple library items, including combos
- Long-press selects and expands without duplicating items; cancel holds safely during scrolling
- Add visual feedback to navigation and library controls, with reduced-motion support
- Remove vibration from library, tray navigation, and number-pad interactions

## v1.6.0
- Show parsed library items as compact nutrition draft cards with serving size, nutrient values, category, tags and icon
- Keep unknown values and partial-label notices visible; expand ingredients, additional nutrients and lengthy assistant notes as needed
- Suggest organization using existing categories/tags and supported icons, preserving current selections during photo follow-ups
- Browse every tag in a scrollable multi-select picker, with typed filtering, keyboard navigation and new-tag entry
- Preserve existing category and tag capitalization on save
- Keep the beginning of new results visible within the chat without moving the surrounding editor

## v1.5.1
- Reject malformed assistant custom attributes before they can crash the library item editor
- Show rejected-update notices in chat while keeping the existing draft available
- Recover from an editor rendering failure without losing the assistant conversation or attached photos, with an option to restore the previous draft
- Keep automatic scrolling inside the conversation and provide a reload screen for unexpected app rendering errors
- Added React integration tests and verified cropped photo submission, multi-step tool results, recovery, and saving in mobile and desktop browsers

## v1.5.0
- Fixed the repeated Gemini HTTP 400 error by preserving tool declarations and instructions on every chat request
- Rebuilt photo cropping with original-image resolution, touch controls, rotation, whole-photo attachment, camera capture, and up to four photos per message
- Added structured food and supplement label reading that distinguishes facts panels from marketing and checks units and serving columns
- Preserved partial ingredients, manufacturer details, supplement actives, unknown nutrients and photo evidence across draft follow-ups
- Added explicit requests for missing details, typed corrections, and retry with the same photos and message
- Updated signed Android, Windows portable, Linux AppImage and Debian packages

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
