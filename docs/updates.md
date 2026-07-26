# Release Updates & History

## v1.2.8 (Version Code 15) - July 26th, 2026
- Tapping **UPDATE** on a collapsed app card opens the detail view on the **Updates** tab so you can read the changelog while the APK downloads
- After an update finishes, you stay on the **Updates** tab (fresh installs still open the User Guide)
- Fixed the install-waiting banner **CANCEL INSTALL** button shadow stretching across the full banner width

## v1.2.7 (Version Code 14) - July 25th, 2026
- Removed the Remove button from the app detail footer
- Unlock control moved to the bottom-left of the detail card as **Unlock Pro Features**
- User-facing copy shifted from “AI” to **pro features** (badges, settings, unlock dialog, docs)

## v1.2.6 (Version Code 13) - July 25th, 2026
- Install / update no longer sends you to the home screen — App Manager stays open
- System install + Play Protect prompts launch in the hub’s task; an in-app banner shows while waiting
- After success, the hub opens that app’s User Guide (Cancel Install clears a stuck wait)

## v1.2.5 (Version Code 12) - July 25th, 2026
- Fixed Play Protect disappearing after tapping Install: the hub no longer re-yields / restarts UI on the second confirmation prompt
- Clearer install-conflict guidance when an app still exists in Island, Secure Folder, or a Work profile (main profile can show Not Installed)
- Pre-checks for other-profile copies before downloading, so users get actionable instructions instead of a cryptic conflict

## v1.2.4 (Version Code 11) - July 25th, 2026
- Fixed Play Protect vanishing immediately: a confirmation-host lifecycle bug was bringing App Manager back on top and abandoning the install session
- During Play Protect / install confirmation the hub briefly backgrounds again (the reliable fix), then returns automatically on success to the User Guide
- Retrying Install clears stuck sessions instead of permanently showing “install already in progress”

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
