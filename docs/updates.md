# Release Updates & History

## v1.3.0 (Version Code 18) - August 8th, 2026
- Make Google the standard RykerSoft account sign-in through Android Credential Manager
- Remove new email/password account creation while retaining an explicit legacy sign-in, password-reset, and Google-link migration path
- Preserve each migrated account's Firebase UID, owned data, and existing entitlements when Google is linked
- Add accessible show/hide controls to the remaining legacy password, unlock-code, and private-token fields
- Move unlock-code validation into an atomic Firestore request whose rules grant only packages listed by the server-only code record
- Deny client reads of unlock-code documents and client writes to entitlement documents in Firestore rules
- Register the release signing certificate SHA-1 and SHA-256 fingerprints and enable Google in Firebase project `rykersoft-abe84`

## v1.2.10 (Version Code 17) - August 8th, 2026
- Add a compact share icon beside every app card's version number
- Copy the selected app's exact registry APK URL to the Android clipboard
- Confirm successful copies with an app-specific snackbar message
- Add Compose interaction coverage for the new share control

## v1.2.9 (Version Code 16) - August 7th, 2026
- Validate downloaded APK package IDs, signing certificates, and version codes before opening Android's installer
- Explain signing-key conflicts separately from Island, Secure Folder, and Work Profile conflicts
- Document the one-time uninstall required for devices that still have the debug-signed v1.1.0 release
- Canonicalize the Android namespace to `com.rykersoft.appmanager` without changing the installed application ID
- Harden the manual release workflow to require encrypted signing secrets and verify the expected certificate before publication
- Add a public RykerSoft screenshot gallery and refresh all hub documentation

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
