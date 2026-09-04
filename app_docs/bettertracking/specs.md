# Technical specifications

## Package
- **App name:** bettertracking
- **Android package ID:** `com.rykersoft.bettertracking`
- **Current version:** 1.1.5 (versionCode 9)

## Platforms
| Platform | Stack | Storage |
|---|---|---|
| Android | Capacitor 7 + WebView | Firestore + IndexedDB offline cache |
| Web | Vite + React (PWA, service worker) | Firestore + IndexedDB offline cache |
| Windows | Electron portable | Same web bundle |

## Android
- **minSdk:** 23, **targetSdk / compileSdk:** 35
- Release builds signed via `android/keystore.properties` (gitignored)
- Capacitor Local Notifications with reboot rescheduling permissions

## Accounts & sync
- Email/password sign-in via the app's own Firebase Auth project
- Per-user Firestore data: library, logs, profile, chat conversations, analyses
- Realtime listeners with IndexedDB offline persistence

## AI providers
- **Gemini** — Quick Log, AI Architect, nutrition/burn estimates, and tool-using chat. Health Coach Analysis builds a local portable prompt for the user's external chatbot instead of calling Gemini.
- **Groq Whisper** (default) / **OpenAI Whisper** — speech-to-text
- Keys come from the RykerSoft hub Firebase (`rykersoft-abe84`) after unlock + hub sign-in (Profile → API Keys), or can be entered manually

## Architecture notes
- `App.tsx` — shell, navigation, Firebase subscriptions, overlays
- `services/` — Gemini/AI, adaptive nutrition engine, transcription, notifications, library search
- `services/rykersoftHub.ts` — second Firebase app (`rykersoft-hub`) for entitlements + provider keys
- Adaptive targets: Mifflin–St Jeor BMR, ~14-day calibration, 0.8× exercise-burn trust factor, optional Energy Bank

## Distribution
- RykerSoft hub installs the signed APK from this repo's GitHub Releases
- Hub gallery screenshots live in `rykergrey/RykerSoft` → `screenshots/bettertracking/`
