# RykerSoft User Guide

RykerSoft is the Android hub for installing, updating, launching, and learning about the RykerSoft app collection.

## Table of Contents

- [1. Overview](#1-overview)
- [2. Browse Apps & Documentation](#2-browse-apps--documentation)
- [3. Install & Update Apps](#3-install--update-apps)
- [4. After an Install or Update](#4-after-an-install-or-update)
- [5. Settings & RykerSoft Account](#5-settings--rykersoft-account)
- [PRO Features](#pro-features)
- [6. Troubleshooting](#6-troubleshooting)
- [7. Privacy and Security](#7-privacy-and-security)

## 1. Overview

The home dashboard synchronizes the RykerSoft registry and compares every catalog entry with packages visible to Android. Status labels show whether an app is a new release, installed, or ready for an update.

Colors carry consistent meaning: yellow marks primary actions and active tabs, green means installed or successful, red identifies errors or destructive actions, cyan marks links and interactive focus, and magenta marks pro capabilities.

## 2. Browse Apps & Documentation

Use the platform, status, sorting, and search controls above the catalog to find an app. Tap a card to open its detail view.

The detail view chooses a useful starting tab:

- **Update available** — Opens **Updates** so you can review the new release notes.
- **Not installed** — Opens **Description** so you can understand the app before installing.
- **Installed and current** — Opens **User Guide**.

Each detail view can include an image gallery, full description, reverse-chronological updates, technical specifications, and a clickable user-guide table of contents.

To share an APK download, tap the share icon beside an app's version number. RykerSoft copies that app's exact registry APK URL to the Android clipboard and confirms the copy at the bottom of the screen. Paste the link into a message or another sharing destination. Access to private distribution links still requires an authorized RykerSoft family token or GitHub account.

## 3. Install & Update Apps

1. Tap the sync button to fetch the newest registry.
2. Tap **INSTALL** for a new app or **UPDATE** for an installed app.
3. RykerSoft downloads the APK and verifies its package name, signing certificate, and version code.
4. If Android has not granted install permission to RykerSoft, enable **Allow from this source** when Settings opens, then return to the app.
5. Approve Android's package installer and Play Protect prompts. Keep RykerSoft in the foreground until confirmation completes.
6. A yellow waiting banner remains visible while Android is processing the session. Use **CANCEL INSTALL** if a session becomes stuck.

Unknown-source permission authorizes RykerSoft to request an install. It does not bypass Android's package-signature, version, device-compatibility, policy, or storage checks.

## 4. After an Install or Update

After success, RykerSoft returns to the foreground and refreshes installed versions.

- An **update** reopens the app's **Updates** tab.
- A **fresh install** opens the app's **User Guide**.
- An installed, current app displays **OPEN**, **PLAY**, or **LAUNCH**.

If Android rejects the operation, RykerSoft presents the most specific available explanation and leaves the app ready to retry.

## 5. Settings & RykerSoft Account

Open the gear control to:

- Sign in to RykerSoft with Google through Android's account chooser.
- Override the built-in distribution token if the family token is rotated.
- Change the registry URL for development or recovery.
- Enable or disable periodic update notifications.
- Select a title-font preset.
- Add a custom app entry or load sample catalog entries.

The RykerSoft account is separate from any product-specific account an individual app may use.

New RykerSoft accounts use Google and do not require another password. If you already have a password-based RykerSoft account, choose **MIGRATE AN EXISTING PASSWORD ACCOUNT**, verify the old account, then choose **LINK GOOGLE & PRESERVE ACCOUNT**. Linking keeps the original Firebase UID, data ownership, and entitlements. The migration panel also provides password reset; it cannot create new password accounts.

Every remaining secret field starts hidden. Use its accessible eye button to reveal or hide the value without clearing the field.

## PRO Features

The App Manager itself is free. A magenta `*` marks optional features in connected apps that require a RykerSoft pro unlock.

* Unlock eligible apps — Sign in with Google, open a pro-capable app's detail page, choose **UNLOCK PRO FEATURES**, and enter the provided unlock code. Firestore validates the atomic request against the server-only code record and grants only listed packages.
* Activate inside the app — Install or open the app, then sign in with the same Google account inside that app so its entitlement and provider configuration can synchronize.

Ordinary unstarred catalog, documentation, update, download, and installation features remain available without a pro unlock.

## 6. Troubleshooting

- **“App not installed” after previously using v1.1.0:** v1.1.0 was signed with an Android debug key, while v1.1.1 and newer use the RykerSoft release key. Android cannot update across those keys. Uninstall RykerSoft from every profile, then install the current release. Uninstalling clears RykerSoft's local data.
- **Signing-key conflict for another app:** The installed copy and downloaded release do not share a signing certificate. Uninstall every copy only if you accept losing that app's local data, then install again.
- **Hub says Not Installed but Android reports a conflict:** Check Island, Secure Folder, Work Profile, secondary users, and archived apps. Remove the hidden copy from its profile before retrying.
- **Install permission required:** Open Android Settings for RykerSoft and enable **Allow from this source**.
- **APK is invalid or has the wrong package:** Sync the registry again. If the error persists, the published asset or registry URL needs correction.
- **Older version blocked:** Install a release with an equal or greater Android version code, or uninstall the newer copy first.
- **Incompatible device:** RykerSoft requires Android 7.0 or newer and the target app may have additional requirements.
- **Not enough storage:** Free internal storage and retry the download and installation.
- **Play Protect prompt is hidden or stalled:** Return to RykerSoft, tap **CANCEL INSTALL**, and retry with the app left in the foreground.
- **Download or documentation authorization fails:** Clear a stale token override to use the built-in family token, or enter the replacement token supplied by the administrator.
- **Pro unlock fails:** Confirm you are signed in, the unlock code is active, and the app is included in that code's allowed packages.
- **Google sign-in does not show an account:** Confirm Google Play services is available and that the device has a Google account, then retry the persistent **SIGN IN WITH GOOGLE** button.
- **Google says the email belongs to a legacy account:** Open the migration panel, sign in with the existing password, and link Google from the signed-in account screen. Do not create or merge accounts manually.
- **Legacy password is forgotten:** Enter the legacy email in the migration panel and choose **RESET PASSWORD**.
- **Google is already linked elsewhere:** No automatic merge occurs. Contact support so ownership can be verified without choosing a data winner or changing a UID.
- **App remains locked:** Sign in to the same RykerSoft account inside the target app and refresh its keys.

## 7. Privacy and Security

RykerSoft reads package metadata to determine installation status and versions. It downloads registry data, documentation, screenshots, and APK assets from RykerSoft-controlled GitHub repositories. Android always presents the final installation confirmation.

Release APKs are cryptographically signed. RykerSoft validates downloaded package identity and signing compatibility locally before starting an installation. Treat unlock codes and private-distribution tokens as credentials and do not share them publicly.
