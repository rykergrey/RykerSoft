# RykerSoft User Guide

RykerSoft is the hub for installing, updating, and reading docs for the RykerSoft app collection.

## Table of Contents

- [1. Overview](#1-overview)
- [2. App Detail Tabs](#2-app-detail-tabs)
- [3. Checking & Installing Updates](#3-checking--installing-updates)
- [4. After an Install](#4-after-an-install)
- [5. RykerSoft Account & Pro Unlock](#5-rykersoft-account--pro-unlock)
- [6. Customizing Settings](#6-customizing-settings)
- [7. Troubleshooting](#7-troubleshooting)

## 1. Overview
RykerSoft serves as the unified dashboard for all applications developed under the RykerSoft software collection. It manages version tracking, downloads, documentation, and instant installations.

Colors carry meaning throughout the app: **yellow** marks primary actions and the active tab, **green** means installed or success, **red** means an error or a destructive action, and **cyan** marks links and interactive highlights.

## 2. App Detail Tabs
Tap any application card to open its detail view. The hub opens a useful tab automatically:

- **Update available** → **Updates** tab (release notes for the new version)
- **Not installed** → **Description** tab (what the app does)
- **Installed and up to date** → **User Guide** tab

You can still switch tabs manually anytime:

- **Updates**: Reverse-chronological release notes
- **Description**: Feature overview
- **User Guide**: Interactive guide with a clickable Table of Contents

## 3. Checking & Installing Updates
1. Tap the sync (refresh) button in the top control bar to pull the latest registry.
2. Each app card shows a status sticker: **NEW RELEASE** (not installed), **UPDATE READY** (yellow — a newer version is available), or **INSTALLED** (green — up to date).
3. Tap the yellow **INSTALL** / **UPDATE** button to download the latest APK. App Manager stays open and shows a waiting banner.
4. Accept the system install prompt and Play Protect when they appear on top — you do **not** need to disable Play Protect. (The app detail sheet closes during this so those prompts stay tappable.)
5. When install finishes, that app’s **User Guide** tab opens automatically.
6. If a wait gets stuck, tap **CANCEL INSTALL** on the banner (or tap Install again). Installed, up-to-date apps show a green **OPEN** / **PLAY** / **LAUNCH** button.

App downloads and documentation come from the private **RykerSoft-APKs** distribution repo. The required access token is built into the app, so downloads work out of the box. If the built-in token is ever rotated, paste the replacement into **Settings → GitHub Token** (that field overrides the built-in one).

## 4. After an Install
When install finishes successfully:

1. App Manager returns to the foreground automatically.
2. That app’s detail view opens on the **User Guide** tab.

If an install was cancelled or blocked, you’ll see an error toast and can try again. Retrying also clears any stuck prior install session.

## 5. RykerSoft Account & Pro Unlock
Some apps (SuperThink.ing, bettertracking, INFORMANT, Photocraft.ing) include optional pro features. Those features use keys delivered from your RykerSoft Firebase project after unlock.

1. Open **Settings** and create or sign in to a **RykerSoft account** (email/password). This is separate from each app’s own login.
2. Open a pro-capable app’s detail page. If you see **PRO LOCKED**, tap **UNLOCK PRO FEATURES** in the bottom-left of the detail card.
3. Enter the family unlock code provided by the admin.
4. Install or open the app, then sign into the **same RykerSoft account** inside that app’s settings (RykerSoft pro unlock section) so keys can sync.
5. Core app features work even when locked. Pro features stay off until unlock + hub sign-in succeed.

Admin setup (one-time): see `firebase/SEED.md` for Firestore rules, unlock code hashes, and `config/providerKeys`.

## 6. Customizing Settings
Access Settings via the top gear / title controls to:
- Sign in to your RykerSoft account for pro unlocks
- Set the RykerSoft access token (GitHub PAT scoped to the RykerSoft-APKs repo) for APK/docs downloads
- Configure periodic background update notification checks
- Change title font presets (Arcade 3D, Cyber Neon, etc.)

## 7. Troubleshooting
- **Install Permission Denied**: Ensure Android allows RykerSoft to install unknown applications in system settings.
- **Network Error / APK 404**: The access token is invalid — usually a stale token pasted in Settings (clear the field to use the built-in one), or the built-in token was rotated (paste the new one from the admin).
- **“Already installed” / install conflict but the hub says Not Installed**: A copy still exists in **Island**, **Secure Folder**, or a **Work profile**. Open that profile, uninstall the app there, then try again from App Manager. The main home screen can look clean while Island still has it.
- **Pro unlock fails**: Confirm Firebase `.env` keys are set, Email/Password Auth is enabled, and the unlock code hash exists under `unlockCodes/{hash}`.
- **App still locked after unlock**: Sign into the same RykerSoft account inside the app and tap Refresh keys.
- **Install prompts not appearing**: Tap **CANCEL INSTALL** on the yellow banner, then try again. Keep App Manager in the foreground while confirming.
