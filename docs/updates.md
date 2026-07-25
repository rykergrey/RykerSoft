# Release Updates & History

## v1.2.3 (Version Code 10) - July 25th, 2026
- Installs now use Android PackageInstaller sessions instead of ACTION_VIEW, so Play Protect stays on-screen and tappable
- App detail dialog closes while confirmation runs (Compose dialogs were burying Play Protect and leaving installs stuck forever)
- After success, App Manager returns to the foreground on that app’s User Guide tab
- Stuck/abandoned install sessions are cleared before each new install

## v1.2.2 (Version Code 9) - July 24th, 2026
- App Manager no longer backgrounds itself when launching the system package installer — you stay in the hub during install
- After a successful install or update, the app detail view reopens on the User Guide tab
- Opening an app detail view now picks a smarter default tab: Updates if an update is available, Description if not installed, User Guide if installed and up to date

## v1.2.1 (Version Code 8) - July 24th, 2026
- Photocraft.ing added as an unlockable AI app
- UNLOCK AI FEATURES applies to Photocraft.ing alongside SuperThinking, bettertracking, and INFORMANT

## v1.2.0 (Version Code 7) - July 24th, 2026
- Complete visual design overhaul with a strict semantic color system: yellow = primary actions and active tabs, green = installed/success, crimson = errors and destructive actions, cyan = links and interactive focus, magenta reserved for brand accents
- Error toasts now show a red border and alert icon instead of a green checkmark
- Dual-font typography: retro monospace stays on headers, badges, and stats; descriptions, user guides, and release notes switch to the Inter sans-serif for comfortable reading
- App Manager update banner redesigned — description text wraps across the full card width instead of truncating, with a full-width UPDATE NOW button
- Cleaner app detail view: flat nested panels with softer borders, one unified section-label style, and yellow active tabs
- Markdown polish: cyan underlined hyperlinks, inline code chips, neon accent-bar headings; inline bold no longer borrows the CTA yellow
- Fixed dark-on-dark icons and labels (settings gear, sign-out button)

## v1.1.4 (Version Code 6) - July 24th, 2026
- Family access token now ships baked into the app — downloads work out of the box, no manual token entry needed
- Settings → GitHub Token overrides the built-in token (useful if the token is ever rotated); clearing it returns to the built-in one

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
