Holistic personal tracking for food, supplements, exercise, measurements, lifestyle, and notes — with an adaptive calorie engine, AI logging and coaching, reminders, cloud sync, and offline support.

bettertracking is a full journal + library system, not a simple calorie counter. Track what you eat and do, build a personal library of ingredients and combos, stage batches into your journal, and let targets adapt from your weight trend and activity.

## Features

- **Six domains** in one place: Food, Supplement, Exercise, Measurement, Lifestyle, and Note
- **Journal** with day/week/month views, domain filters, tag search, trend charts, and multi-select export (CSV / Markdown / clipboard)
- **Personal library** of reusable ingredients and combos with macros, micros, custom fields, exercise routines, and attached reminders
- **Staging tray** for multi-item logging with quantities, units, voice quick-add (uses each library item’s defined unit), and day-target awareness
- **Library panel** remembers search, scroll, and expanded groups across tray switches
* **Quick Log**: free text and/or a photo → AI-estimated macros, refine with feedback
- **Adaptive nutrition engine**: Mifflin–St Jeor BMR, weight-trend adaptation after ~14 days, conservative 0.8× trust on exercise burn, manual overrides
- **Optional Energy Bank** that carries surplus/deficit across days with lookback, recovery, and pacing controls
- **AI assistant & coaching**: tool-using chat (navigate, query logs, create/update items, set reminders), plus local day/week/month Health Coach prompts that open in the user's selected chatbot (Perplexity by default)
* **AI Architect**: describe an item (optionally with images) and let the model draft the library entry
* **Speech-to-text** via Groq Whisper (default) or OpenAI Whisper, plus on-device voice capture on Android
- **Alerts & reminders**: per-item schedules (daily/weekly/monthly/etc.), wake/sleep alarms, Capacitor local notifications with reboot rescheduling
- **Firebase Auth + Firestore** account sync with IndexedDB offline persistence
- **Library JSON export/import** including combo dependencies

## PRO Features

Items marked * require an administrator-granted entitlement for `com.rykersoft.bettertracking`, followed by sign-in to the same RykerSoft account inside the app under **Profile → API Keys → RykerSoft AI unlock**. Health Coach prompts use the user's selected external chatbot and do not consume BetterTracking API credits. All tracking, journaling, library, and reminder features work without Pro access. You can also supply your own Gemini/Groq/OpenAI keys manually.

## Platforms

- **Android** — Capacitor APK via the RykerSoft hub (`com.rykersoft.bettertracking`)
- **Web** — Vite PWA with service worker
- **Windows** — Electron portable build
