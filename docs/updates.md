# Release Updates & History

## v1.4.2
Version Code 23 - August 23rd, 2026
- Declared Hyperscribe Mobile as an optional RykerSoft Pro Access application
- Added package-scoped Gemini, OpenAI, Groq, and ElevenLabs credential fields for trusted family use
- Kept Hyperscribe Desktop as a separate non-Pro, bring-your-own-key catalog application
- Preserved all existing provider keys and user entitlements through merge-only capability updates

## v1.4.1
Version Code 22 - August 23rd, 2026
- Added real Windows catalog browsing through the Desktop platform filter
- Added EXE-only application records with independent Windows download and share actions
- Kept Android APK install, update, validation, and launch behavior unchanged
- Added separate Hyperscribe Mobile and Hyperscribe Desktop catalog support
- Added merge-only non-Pro capability manifests for both Hyperscribe products
- Added a Room migration that preserves the Windows download URL locally

## v1.4.0
Version Code 21 - August 20th, 2026
- Added an administrator-only User Management section for managing RykerSoft accounts directly in the application
- Added searchable account listings and per-application controls for granting or revoking Pro access
- Added secure, masked provider credential controls for Pro-enabled applications that declare required API services
- Added administrator notifications when new RykerSoft account profiles are created
- Restricted administrative controls to the verified `heavensounds@gmail.com` account
- Improved entitlement security by using exact application package grants without exposing provider credentials to unauthorized users

## v1.3.2 (Version Code 20) - August 8th, 2026
- Remove the embedded family GitHub token and the Settings token override so credentials are never shipped in the Android client or attached to registry, documentation, or APK requests
- Download current RykerSoft binaries and documentation from anonymously accessible public distribution URLs while keeping private application source repositories private
- Retire the reusable family unlock-code flow and preserve existing per-application pro grants at `users/{uid}/entitlements/apps`
- Make Firebase entitlements administrator-managed: signed-in users can read their own grants but cannot create, change, or delete them
- Replace the unlock-code entry dialog with account-specific Pro Access information and keep all non-pro features available without an entitlement
- Purge any legacy GitHub token override saved by an earlier App Manager release
- Replace credential-dependent release automation with secretless GitHub Actions validation; signed production releases remain locally built and certificate-verified

## v1.3.1 (Version Code 19) - August 8th, 2026
- Keep an expanded application card open when a download or Android installer session begins
- Preserve the exact Updates, Description, or User Guide tab selected by the user throughout install and update progress
- Add the APK-link share control beside Install, Update, and Launch actions inside expanded app details
- Remove the unnecessary `C++ / Engine` and `Kotlin / App` programming labels from catalog cards
- Show informational Windows badges and a Windows-version property for apps with separate desktop editions
- Recognize verified registry Windows assets for INFORMANT, WordPlay.ing, and FreeBall.ing, plus the known SuperThink.ing desktop edition
- Keep Windows availability display-only in the Android hub; no Windows download or launch action is provided

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
