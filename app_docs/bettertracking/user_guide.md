# bettertracking User Guide

Holistic tracking for food, supplements, exercise, measurements, lifestyle, and notes — with adaptive calorie targets, AI logging, and coaching.

## Table of Contents

- [1. Getting started](#1-getting-started)
- [2. The Journal](#2-the-journal)
- [3. Library & Composer](#3-library--composer)
- [4. Staging tray & Quick Log](#4-staging-tray--quick-log)
- [5. Adaptive targets & Energy Bank](#5-adaptive-targets--energy-bank)
- [6. AI chat & Health Coach](#6-ai-chat--health-coach)
- [7. Alerts & reminders](#7-alerts--reminders)
- [PRO Features](#pro-features)
- [9. Profile, keys & preferences](#9-profile-keys--preferences)
- [10. Export & import](#10-export--import)

## 1. Getting started

1. Create an account or sign in (email/password). Your library, logs, and profile sync through the cloud and keep working offline.
2. Fill in **Profile → Goals & biometrics** (weight goal, gender, height, date of birth, wake/bed times) so calorie targets can be calculated.
3. New accounts come seeded with builtin items (Weight, Water, Sleep, bodyweight exercises, and more) so you can log immediately.
4. To use PRO features, ask the RykerSoft administrator to enable bettertracking for your account and sign in under **Profile → API Keys** (see PRO Features).

## 2. The Journal

- Switch between **day, week, and month** views with the date navigator.
- Filter by domain (Food / Supplement / Exercise / Measurement / Lifestyle / Note), search, and filter by tags.
- Sort by time, calories, burn, macros, or name; group by time, category, or item.
- Day dashboards summarize nutrition, supplements, exercise, measurements, lifestyle, and notes.
- Week/month views add trend charts (calories, macros, burn, weight) with totals, averages, and min/max.
- Tap a log to edit its quantity, unit, date, or custom values. Multi-select for batch delete/update or export.
- A yesterday catch-up banner appears when food logging looked thin, which keeps Energy Bank math honest.

## 3. Library & Composer

- The Library holds reusable items across all six domains: simple **ingredients** and **combos** that compose child items with quantities.
- Items carry macros, optional micros (fiber, vitamins, minerals, etc.), units, serving size, tags, categories, icons, and custom fields.
- Build exercise **routines** with rest periods, and attach structured **reminders** to any item.
- **AI Architect**: describe an item in plain language (optionally with photos) and let the model draft the entry; nutrition AI can estimate macros and exercise burn from your biometrics.
- Tap behavior is configurable: stage the item to the tray or open its details.
- Your Library search, scroll position, and open groups are kept when you leave for the staging tray and return.

## 4. Staging tray & Quick Log

- Stage several items with quantities, units, and custom values before committing them as logs.
- Pick the log date/time and watch staged vs remaining nutrition against your day targets.
- Use voice or text quick-add inside the tray, create a combo from the current tray, or open chat with the staged items as context.
- When you add from the library with a phrase like “three whole eggs,” the quantity uses that item’s library unit (pcs, g, srv, etc.), not adjectives from the phrase.
- **Quick Log**: type free text and/or attach a photo → AI estimates the macros. Refine with feedback before saving; one-off hidden items can be created automatically.

## 5. Adaptive targets & Energy Bank

- Daily calorie targets start from Mifflin–St Jeor BMR using your biometrics.
- After about 14 days of weight history, targets shift from calibrating to **adaptive**, driven by your actual weight trend.
- Logged exercise burn is trusted at 0.8× to stay conservative. You can override calories, macros, and micros manually.
- The optional **Energy Bank** carries surplus/deficit across days with configurable window length, recovery days, and pacing.
- In-app **Help** explains the engine, the burn trust factor, and the BMR floor in detail.

## 6. AI chat & Health Coach

- Chat streams replies with markdown and item cards; conversations persist to your account.
- The assistant can use tools: navigate the app, query your library and logs, create or update items, adjust your profile, set reminders, and propose batch updates.
- Attach day logs or a saved analysis by calendar date as chat context.
- **Health Coach Analysis** builds a detailed day/week/month prompt locally from your profile, targets, notes, and logs. Choose Perplexity (default), ChatGPT, Google Gemini, or copy it into any chatbot. Long prompts are copied for manual paste when direct prefill is unreliable; previously saved reports remain readable.
- Voice input works in chat (Groq or OpenAI Whisper).

## 7. Alerts & reminders

- Attach schedules to library items: once, daily, weekdays, weekly, biweekly, or monthly.
- The Alerts view shows Active (today) and Scheduled reminders; log, edit, duplicate, or delete from there.
- Profile wake/sleep alarms and per-domain notification toggles control local notifications.
- On Android, notifications reschedule automatically after a reboot.

## PRO Features

Items marked *—Quick Log, AI Architect, chat, and voice transcription—require Pro access. Coach Analysis builds a prompt locally and uses the user's selected external chatbot account. All tracking, journaling, library, and reminder features work without Pro access.

1. In the **RykerSoft App Manager**, sign in to your RykerSoft account with Google.
2. Open bettertracking's page and use **PRO ACCESS INFO** to confirm the account the administrator should authorize.
3. After the administrator enables `com.rykersoft.bettertracking` for that account's Firebase UID, go to **Profile → API Keys** and sign in with the **same** RykerSoft account.
4. Provider access syncs automatically. Use **Refresh keys** if PRO features do not appear right away.

Notes:
- Pro access is the package entitlement on your Firebase UID; no reusable family code is required.
- This RykerSoft sign-in is separate from your bettertracking account (which syncs your data).
- Keys you enter manually under Profile → API Keys take priority over synced keys.

## 9. Profile, keys & preferences

- **Goals & biometrics**: weight goal, body-composition goal (lose / maintain / gain), dietary preferences, and coach context.
- **Custom targets**: manual calorie/macro/micro overrides and an adaptive-target preview.
- **Energy Bank settings**: enable/disable, window, recovery, pacing.
- **API Keys**: RykerSoft AI unlock sign-in, plus optional manual Gemini / Groq / OpenAI keys and model picks.
- **Transcription provider**: Groq (default) or OpenAI.
- **Library tap** preference: stage to tray or open details.

## 10. Export & import

- Multi-select journal logs → export **CSV**, **Markdown**, or copy to clipboard.
- Export library items (including combo dependencies) as a JSON file; import that JSON on another device or account.
