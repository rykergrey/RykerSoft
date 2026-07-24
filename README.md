<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# RykerSoft Application Manager

Personal Android app hub and application manager for RykerSoft applications. Easily check for updates, view changelogs, download, and install latest versions of RykerSoft apps — including self-updating RykerSoft itself!

## Features
- **App Hub & Registry**: Synchronizes with `registry.json` for live update tracking.
- **In-App Self-Updating**: Automatically alerts when a new version of RykerSoft is available with one-click in-app update.
- **Rich App Cards & Detail Views**: View changelogs, markdown descriptions, version diffs (`v1.0.0 → v1.0.2`), and screenshots.
- **Package Installer Integration**: Seamlessly downloads APKs and invokes system installer.

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

Releases are managed using GitHub Tags and GitHub Actions (`.github/workflows/release.yml`).

### 1. Tagging & Pushing a Release
To publish a new release candidate or official release version:

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

The GitHub Actions workflow will automatically:
1. Compile the release APK (`./gradlew assembleRelease`).
2. Create a GitHub Release under the corresponding tag (marking `-rc` tags as Pre-release).
3. Attach `app-release.apk` as a release asset.

### 2. Updating `registry.json`
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

Once committed and pushed to `main`, all installed RykerSoft app instances will detect the update and display the **App Manager Update Available** alert banner on open.

