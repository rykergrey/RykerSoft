# RykerSoft Application Manager

Current release: **v1.3.2** (`versionCode` 20), package `com.rykersoft.appmanager`, for Android 7.0/API 24 and newer.

Personal Android app hub and application manager for RykerSoft applications. Easily check for updates, view changelogs, download, and install latest versions of RykerSoft apps — including self-updating RykerSoft itself!

## Features
- **App Hub & Registry**: Synchronizes with `registry.json` for live update tracking.
- **In-App Self-Updating**: Automatically alerts when a new version of RykerSoft is available with one-click in-app update.
- **Rich App Cards & Detail Views**: View changelogs, markdown descriptions, version diffs (`v1.0.0 → v1.0.2`), and screenshots.
- **Package Installer Integration**: Seamlessly downloads APKs and invokes system installer.
- **Shareable APK Links**: Copy an app's exact APK download URL from the share icon beside its version.
- **Google RykerSoft Account**: Sign in through Android Credential Manager, with a safe link-and-recovery path for legacy password accounts.
- **UID-Based Pro Access**: App-specific Firestore entitlements are managed by an administrator and read only by the matching signed-in Firebase user; free features remain available without a grant.

---

## Run & Install Locally

### Prerequisites
- Android Studio or Android SDK command-line tools
- Device or emulator connected via ADB (or wireless debugging)

### Local Release Build & Install
Run the included PowerShell script to build the release APK and install it directly on a connected device:
```powershell
.\install-release.ps1
```

---

## Deploying Releases & Release Candidates

Releases are built, signed, and verified locally so the solo maintainer's Android signing key never needs to enter GitHub Actions. The read-only Actions workflow runs unit tests and produces only a debug validation APK without release credentials.

### 1. Build and Verify Locally

Run the tests and release build from this repository root, then verify the package ID, version, and expected signing certificate before publishing:

```powershell
.\gradlew.bat testDebugUnitTest assembleRelease
```

The release signing key stays on the maintainer's machine. GitHub Actions is limited to read-only validation and debug artifacts; it does not receive release signing credentials.

### 2. Tag and Publish

After the locally signed APK passes verification, publish it to the public release location and push the matching tag:

- **Release Candidate Tag**:
  ```bash
  git tag v1.0.1-rc1
  git push origin v1.0.1-rc1
  ```
- **Official Release Tag**:
  ```bash
  git tag v1.0.1
  git push origin v1.0.1
  ```

Every APK, document, screenshot, or optional Windows artifact referenced by `registry.json` must be available over anonymous HTTPS. Source repositories may remain private, but App Manager never carries a GitHub personal access token or other private-download credential.

> Devices that still have debug-signed v1.1.0 must uninstall that copy before installing v1.1.1 or newer. Android cannot update an app across unrelated signing keys, and uninstalling clears local app data.

### 3. Update `registry.json`
To make the new version available to all RykerSoft users for automatic in-app update notifications, update `registry.json` in the root repository:

```json
{
  "packageName": "com.rykersoft.appmanager",
  "name": "RykerSoft",
  "description": "Personal Android app hub and application manager...",
  "latestVersionCode": 2,
  "latestVersionName": "1.0.1",
  "apkUrl": "https://github.com/rykergrey/RykerSoft/releases/download/v1.0.1/app-release.apk",
  "icon": "android",
  "changelog": "- Summary of changes in v1.0.1",
  "isGame": false
}
```

Provide an `exeUrl` only when a verified Windows release artifact exists. The Android hub
shows Windows availability as informational metadata and intentionally does not download or launch Windows builds.

Once committed and pushed to `main`, all installed RykerSoft app instances will detect the update and display the **App Manager Update Available** alert banner on open.

When migrating an older private distribution, publish and verify a clean public artifact location first, release the tokenless manager while the legacy token still works, update every known installation/profile, and only then revoke and delete the old token. Do not expose an unreviewed private release history simply by changing repository visibility.

## Pro Access Administration

Pro access uses exact package fields at `users/{uid}/entitlements/apps`. Client applications can read only the signed-in user's document and cannot write grants. The Firebase Console is the current administration interface; a future Windows/.NET admin application must be a thin authenticated client of callable Functions using the Admin SDK, never a holder of service-account credentials.

See [`firebase/SEED.md`](firebase/SEED.md) for the exact grant/revoke procedure, existing-user preservation rules, provider-key migration, and optional secure one-time invitation design.

