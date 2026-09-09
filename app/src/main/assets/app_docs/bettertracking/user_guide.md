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

Choose **Android** or **Desktop** from the platform selector. Windows-capable entries display a **WINDOWS** badge. For a Windows-only product, tap **DOWNLOAD** or **GET WINDOWS APP** to open its verified public EXE release in the browser; Android does not attempt to install the EXE.

To share a download, tap the share icon on the home card or inside the expanded detail card. RykerSoft copies that entry's exact public APK or Windows release URL to the Android clipboard and confirms the copy at the bottom of the screen. Paste the link into a message or another sharing destination; no family GitHub credential is required.

## 3. Install & Update Apps

1. Tap the sync button to fetch the newest registry.
2. Tap **INSTALL** for a new app or **UPDATE** for an installed app.
3. RykerSoft downloads the APK and verifies its package name, signing certificate, and version code.
4. If Android has not granted install permission to RykerSoft, enable **Allow from this source** when Settings opens, then return to the app.
5. Approve Android's package installer and Play Protect prompts. Keep RykerSoft in the foreground until confirmation completes.
6. A yellow waiting banner remains visible while Android is processing the session. Use **CANCEL INSTALL** if a session becomes stuck.

The expanded application card remains open throughout download and installation. You can continue reading the selected Updates, Description, or User Guide tab while Android handles the install.

Unknown-source permission authorizes RykerSoft to request an install. It does not bypass Android's package-signature, version, device-compatibility, policy, or storage checks.

## 4. After an Install or Update

After success, RykerSoft returns to the foreground, refreshes installed versions, and keeps your current location.

- The expanded application card remains open if it was already open.
- The selected **Updates**, **Description**, or **User Guide** tab remains selected.
- Starting from the home catalog leaves you on that application card.
- An installed, current app displays **OPEN**, **PLAY**, or **LAUNCH**.

If Android rejects the operation, RykerSoft presents the most specific available explanation and leaves the app ready to retry.

## 5. Settings & RykerSoft Account

Open the gear control to:

- Sign in to RykerSoft with Google through Android's account chooser.
- Change the registry URL for development or recovery.
- Enable or disable periodic update notifications.
- Select a title-font preset.
- Add a custom app entry or load sample catalog entries.

The RykerSoft account is separate from any product-specific account an individual app may use.

New RykerSoft accounts use Google and do not require another password. If you already have a password-based RykerSoft account, choose **MIGRATE AN EXISTING PASSWORD ACCOUNT**, verify the old account, then choose **LINK GOOGLE & PRESERVE ACCOUNT**. Linking keeps the original Firebase UID, data ownership, and entitlements. The migration panel also provides password reset; it cannot create new password accounts.

The legacy password field starts hidden. Use its accessible eye button to reveal or hide the value without clearing the field.

## PRO Features

The App Manager itself is free. A magenta `*` marks optional features in connected apps that require RykerSoft Pro access.

* Request access — Sign in with Google and open a pro-capable app's detail page. **PRO ACCESS INFO** shows the account the administrator must authorize. The administrator grants only the requested package to that account's Firebase UID.
* Activate inside the app — Install or open the app, then sign in with the same Google account inside that app so its entitlement and provider configuration can synchronize. Existing grants remain attached to the preserved UID.

Ordinary unstarred catalog, documentation, update, download, and installation features remain available without Pro access.

When the verified administrator account `heavensounds@gmail.com` is signed in, Store Settings includes user and application management. New hub profiles generate an Android notification after the account directory has been initialized. The administrator can search users, grant or revoke each deployed Pro app independently, see which provider fields are missing, and enter or rotate those values through masked controls. Apps that declare no external APIs appear with no credential fields.

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
- **Download or documentation fails:** Confirm the device has internet access, then sync the registry again. Official RykerSoft distribution links are public and do not require a GitHub token.
- **Pro access is not active:** Confirm you are signed in to the account the administrator authorized, then reopen or refresh the target app. If needed, send the administrator the signed-in email so they can locate the authoritative Firebase UID.
- **Google sign-in does not show an account:** Confirm Google Play services is available and that the device has a Google account, then retry the persistent **SIGN IN WITH GOOGLE** button.
- **Google says the email belongs to a legacy account:** Open the migration panel, sign in with the existing password, and link Google from the signed-in account screen. Do not create or merge accounts manually.
- **Legacy password is forgotten:** Enter the legacy email in the migration panel and choose **RESET PASSWORD**.
- **Google is already linked elsewhere:** No automatic merge occurs. Contact support so ownership can be verified without choosing a data winner or changing a UID.
- **App remains locked:** Sign in to the same RykerSoft account inside the target app and refresh its keys.

## 7. Privacy and Security

RykerSoft reads package metadata to determine Android installation status and versions. It downloads registry data, documentation, screenshots, and APK assets from RykerSoft-controlled GitHub repositories, and opens verified Windows release links in the browser. Android always presents the final APK installation confirmation.

Release APKs are cryptographically signed. RykerSoft validates downloaded package identity and signing compatibility locally before starting an installation. Pro authorization is stored as package-specific booleans under the account's Firebase UID; email addresses are used only by a trusted administrator to locate that UID and are never authorization proof by themselves.
