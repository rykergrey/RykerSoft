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
- [8. RykerSoft AI unlock](#8-rykersoft-ai-unlock)
- [9. Profile, keys & preferences](#9-profile-keys--preferences)
- [10. Export & import](#10-export--import)

## 1. Getting started

1. Create an account or sign in (email/password). Your library, logs, and profile sync through the cloud and keep working offline.
2. Fill in **Profile → Goals & biometrics** (weight goal, gender, height, date of birth, wake/bed times) so calorie targets can be calculated.
3. New accounts come seeded with builtin items (Weight, Water, Sleep, bodyweight exercises, and more) so you can log immediately.
4. To use AI features, unlock bettertracking in the RykerSoft App Manager and sign in under **Profile → API Keys** (see section 8).

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
- **Rolling Calorie Balance** (formerly Energy Bank) opens with a daily check that works from the first usable food log. It compares recent logged days plus this day’s projected intake against those same days’ goals. The configurable 3–14 day window is a lookback, not a weekly allowance or deadline. Unlogged future days never provide extra calories. The previous completed days, averages, and per-day breakdown remain available in expandable history.
- Example: 3,000 kcal on Monday against a 2,000 kcal goal, followed by six 2,000 kcal days, totals **15,000 / 14,000 kcal: 1,000 above goal**. Six days 100 kcal below goal would instead leave 400 kcal above goal. Above a weight-loss goal does not necessarily mean above estimated maintenance; the card explains both.
- **Explore your options** compares keeping the current goal, balancing today, spreading the difference over a chosen number of days (including this day), or entering a custom finishing total. A partial history can be explored immediately and is labeled. For example, yesterday at 3,000 kcal against a 2,000 kcal goal means +1,000 kcal today; finishing today at 1,800 would leave +800. A smaller +300 overage could be offset by a 1,700 kcal day. These are arithmetic previews: suggestions respect the existing calorie floor and already logged intake. A preview never changes the saved goal. Multi-day previews also show an estimated intake for the following days; they assume the baseline stays unchanged and do not treat older days leaving the window as offsets.
- The tracker is visible unless explicitly disabled. Daily goals stay unchanged by default. In Profile, opt into adjustments to adaptive goals and choose a recovery period and daily limit. Adjustments use balance ÷ recovery days, capped by the selected limit and existing BMR floor. Fixed calorie overrides remain fixed. Adjustments are recalculated daily, with no promised clearance date.
- Missing food days, unknown calories, unresolved items, and days with excluded entries are omitted from both intake and goal totals. Supplement-only and exercise-only days are not food-log coverage. Partial windows are labeled and support what-if previews, but do not trigger automatic goal adjustments. Even a logged day may have missing meals: review the logs before interpreting a shortfall or using the suggested pace.
- Historical goals are reconstructed with current profile settings and each day’s available weight and exercise records. Without an earlier weight record, the supplied current weight is the fallback. Past profile versions are not stored. Exercise is counted once in adaptive goals and does not increase fixed goals. An old day leaving the rolling window changes the balance; it does not undo calories already consumed.
- The **Macros & Nutrients** card can switch between Today and the rolling average. Each bar keeps a visible white goal boundary; rolling mode adds a cyan selected-day marker, cumulative variance, usable-day coverage, and a count of days outside the goal. Protein minimums, nutrient maximums, ranges, and neutral aims are evaluated differently, so adequate protein is not labeled an overage and a low carb/fat aim is not presented as a deficiency. Missing or unresolved nutrition stays out of the average rather than becoming a false zero.
- In-app **Help** explains the engine, the burn trust factor, and the BMR floor in detail.

For background on sustainable changes and weight management, see [NIDDK’s eating and physical activity guide](https://www.niddk.nih.gov/health-information/weight-management/adult-overweight-obesity/eating-physical-activity). The rolling balance describes logged intake; it does not predict an exact change in body weight.

## 6. AI chat & Health Coach

- Chat streams replies with markdown and item cards; conversations persist to your account.
- The assistant can use tools: navigate the app, query your library and logs, create or update items, adjust your profile, set reminders, and propose batch updates.
- Attach day logs or a saved analysis by calendar date as chat context.
- **Health Coach Analysis** builds a detailed day/week/month coaching prompt locally from your profile, targets, notes, and logs. Choose Perplexity (the default), ChatGPT, Google Gemini, or copy the prompt into any other chatbot. Long prompts are copied for manual paste when a prefilled URL would be unreliable. Existing saved reports remain available to read or download.
- Voice input works in chat (Groq or OpenAI Whisper).

## 7. Alerts & reminders

- Attach schedules to library items: once, daily, weekdays, weekly, biweekly, or monthly.
- The Alerts view shows Active (today) and Scheduled reminders; log, edit, duplicate, or delete from there.
- Profile wake/sleep alarms and per-domain notification toggles control local notifications.
- On Android, notifications reschedule automatically after a reboot.

## 8. RykerSoft AI unlock

In-app AI features (Quick Log, AI Architect, chat, and cloud voice transcription) require an unlock. Coach Analysis itself does not use a BetterTracking API key; the selected external chatbot uses the user's own account or subscription. All tracking, journaling, library, and reminder features work without an unlock.

1. In the **RykerSoft App Manager**, create or sign in to your RykerSoft account.
2. Open bettertracking's page in the App Manager, tap **UNLOCK AI FEATURES**, and enter your family unlock code.
3. In bettertracking, go to **Profile → API Keys** and sign in with the **same** RykerSoft account.
4. AI keys sync automatically. Use **Refresh keys** if AI features don't light up right away.

Notes:
- The unlock code is only entered in the App Manager, never inside bettertracking.
- This RykerSoft sign-in is separate from your bettertracking account (which syncs your data).
- Keys you enter manually under Profile → API Keys take priority over synced keys.

## 9. Profile, keys & preferences

- **Goals & biometrics**: weight goal, body-composition goal (lose / maintain / gain), dietary preferences, and coach context.
- **Custom targets**: manual calorie/macro/micro overrides and an adaptive-target preview.
- **Rolling Calorie Balance settings**: show/hide, window, recovery days, daily limit, and opt-in goal adjustment.
- **API Keys**: RykerSoft AI unlock sign-in, plus optional manual Gemini / Groq / OpenAI keys and model picks.
- **Transcription provider**: Groq (default) or OpenAI.
- **Library tap** preference: stage to tray or open details.

## 10. Export & import

- Multi-select journal logs → export **CSV**, **Markdown**, or copy to clipboard.
- Export library items (including combo dependencies) as a JSON file; import that JSON on another device or account.

## Nutrition assistant and reports (1.2.0)

Use **Reports** in the journal header or **Food finder** in the library to compare foods without AI. Choose a preset, date range and nutrient limits. You can compare recorded amounts, saved servings, per 100 calories or known weights; save favorites, stage a food or export CSV. Missing nutrition appears as unknown.

In chat, ask “Help me meet my protein goal using my library” or “What foods do I eat most?” Chat retrieves the relevant totals and records as needed. It offers ideas outside your library when useful. Use **Web research: Auto / Off / On** to control external evidence searches.

Attach a label or describe a new food, supplement, exercise or other item. Review the draft serving basis, nutrition and evidence before applying it. Proposed changes are not saved until you apply them. Images are retained on the device where they were attached; extracted drafts and source links sync with chat.

Profile now supports explicit nutrient aims, minimums, maximums, ranges and excluded foods. Library favorites are different from foods you consume frequently.

For Linux, mark the AppImage executable before launching, or install the Debian package on a compatible Debian/Ubuntu system. Download links are in the app description.
