# Technical specifications

## Package
- **App name:** INFORMANT
- **Android package ID:** `com.rykersoft.informant`
- **Current version:** 1.0.4 (versionCode 5)

## Platforms
| Platform | Stack | Storage |
|---|---|---|
| Android | Capacitor 8 + WebView | IndexedDB (`clientDb`) |
| Desktop | Electron / local Node server | Projects JSON + SQLite `informant.db` |
| Web (dev) | Vite + React 19 | Same as platform above |

## Requirements
- **Android:** minSdk from Capacitor project defaults; release builds need `android/keystore.properties`
- **Desktop:** Node.js for `npm run dev` / portable builds
- **Optional APIs:** Gemini, YouTube Data API, YouTube Transcript API, Webshare proxy

## Architecture notes
- Project-centric navigation: `ProjectHome` → `Workspace` (no React Router)
- Single global YouTube player (`PlayerManager`) repositions over the active card target
- Player modes: `collapsed`, `expanded`, `pip`
- Video artifacts (notes, bookmarks, transcripts, comments, chat, analyses) keyed by `videoId`

## Distribution
- RykerSoft hub installs the signed APK from this repo’s GitHub Releases
- Hub gallery screenshots live in `rykergrey/RykerSoft` → `screenshots/informant/`
