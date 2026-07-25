# Release Updates & History

## v1.1.3 (Version Code 5) - July 24th, 2026
- App APKs and docs now distribute from the private RykerSoft-APKs repo — one shared access token covers every app
- Per-app documentation is fetched from `docs/<app>/` in the distribution repo (with fallback to each app repo's layout)
- Users paste a single RykerSoft access token in Settings; app source repos stay private

## v1.1.2 (Version Code 4) - July 24th, 2026
- RykerSoft Firebase account sign-in in Settings for AI entitlements
- Per-app AI unlock with family unlock codes (SuperThinking, bettertracking, INFORMANT)
- Hub UI shows AI LOCKED / AI UNLOCKED and unlock flow

## v1.1.1 (Version Code 3) - July 24th, 2026
- Switched release builds to a dedicated RykerSoft upload keystore (non-debug signing)
- Required one-time uninstall if upgrading from a debug-signed install

## v1.1.0 (Version Code 2) - July 24th, 2026
- Redesigned Application Detail View into a 4-tab tabbed interface (Updates, Description, Specs, User Guide)
- Modularized per-app repository documentation (`app_docs/<slug>/`) for independent updates
- Integrated interactive User Guide Table of Contents with formatted markdown links and auto-scrolling
- Added temporary yellow background highlight fade-out animation on targeted headings
- Standardized package registration to `com.rykersoft.appmanager`

## v1.0.0 (Version Code 1) - July 2026
- Added RykerSoft Application Manager package registration to registry.json
- Implemented in-app self-updating and automatic app manager update alerts
- Added custom title font presets (Arcade 3D, Cyber Neon, Retro CRT, Matrix Terminal)
- Built-in system package installer integration and unknown sources prompt handler

## v0.9.5-beta (Version Code 1) - June 2026
- Initial beta distribution of the RykerSoft application hub
- Room SQLite local database persistence for managed application metadata
- OkHttp cache-busting network fetcher with custom GitHub token authentication support
