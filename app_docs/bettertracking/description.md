Holistic personal tracking for food, supplements, exercise, measurements, lifestyle, and notes — with an adaptive calorie engine, AI logging and coaching, reminders, cloud sync, and offline support.

bettertracking is a full journal + library system, not a simple calorie counter. Track what you eat and do, build a personal library of ingredients and combos, stage batches into your journal, and let targets adapt from your weight trend and activity.

## New in 1.4.0

- Today and rolling-average views for macros and nutrients, using the same configurable lookback as the calorie balance
- Clear goal, minimum, maximum, and range lines with a selected-day marker and visible overage segments
- Period averages, cumulative variance, daily overage counts, and data coverage in one glanceable widget
- Goal-aware guidance that recognizes protein adequacy without treating it as an overage and keeps nutrient trends separate from calorie credit

## Features

- **Six domains** in one place: Food, Supplement, Exercise, Measurement, Lifestyle, and Note
- **Journal** with day/week/month views, domain filters, tag search, trend charts, and multi-select export (CSV / Markdown / clipboard)
- **Personal library** of reusable ingredients and combos with macros, micros, custom fields, exercise routines, and attached reminders
- **Staging tray** for multi-item logging with quantities, units, voice quick-add (uses each library item’s defined unit), and day-target awareness
- **Library panel** remembers search, scroll, and expanded groups across tray switches
- **Quick Log**: free text and/or a photo → AI-estimated macros, refine with feedback
- **Adaptive nutrition engine**: Mifflin–St Jeor BMR, weight-trend adaptation after ~14 days, conservative 0.8× trust on exercise burn, manual overrides
- **Rolling Calorie Balance** that compares recent intake with combined goals, explains logging coverage, and supports opt-in adjustments
- **Rolling nutrient progress** with daily and period views, goal-type-aware boundaries, cumulative variance, and individual overage-day counts
- **AI assistant & coaching**: tool-using chat (navigate, query logs, create/update items, set reminders), plus local day/week/month Health Coach prompts that open in the user's selected chatbot (Perplexity by default)
- **AI Architect**: describe an item (optionally with images) and let the model draft the library entry
- **Speech-to-text** via Groq Whisper (default) or OpenAI Whisper, plus on-device voice capture on Android
- **Alerts & reminders**: per-item schedules (daily/weekly/monthly/etc.), wake/sleep alarms, Capacitor local notifications with reboot rescheduling
- **Firebase Auth + Firestore** account sync with IndexedDB offline persistence
- **Library JSON export/import** including combo dependencies

## AI unlock

In-app AI features (Quick Log, AI Architect, chat, and voice transcription) are enabled through the RykerSoft App Manager: have bettertracking access granted to your RykerSoft account, then sign in with that account inside the app under **Profile → API Keys → RykerSoft AI unlock**. Health Coach prompts do not use BetterTracking API credits; the external chatbot uses the user's own account. All tracking, journaling, library, and reminder features work without the unlock. You can also supply your own Gemini/Groq/OpenAI keys manually.

## Platforms

- **Android** — Capacitor APK via the RykerSoft hub (`com.rykersoft.bettertracking`)
- **Web** — Vite PWA with service worker
- **Windows** — Electron portable build

- **Linux** — x64 AppImage and Debian package

## Download version 1.4.0

- [Android APK](https://github.com/rykergrey/RykerSoft-APKs/releases/download/bettertracking-v1.4.0/app-release.apk)
- [Windows x64 portable](https://github.com/rykergrey/RykerSoft-APKs/releases/download/bettertracking-v1.4.0/bettertracking-v1.4.0-win-x64.exe)
- [Linux x64 AppImage](https://github.com/rykergrey/RykerSoft-APKs/releases/download/bettertracking-v1.4.0/bettertracking-v1.4.0-linux-x86_64.AppImage)
- [Linux x64 Debian package](https://github.com/rykergrey/RykerSoft-APKs/releases/download/bettertracking-v1.4.0/bettertracking-v1.4.0-linux-amd64.deb)
