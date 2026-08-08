# RykerSoft Technical Specifications

## Platform and Release

- **Platform:** Native Android application written in Kotlin
- **UI:** Jetpack Compose with Material 3
- **Package ID / namespace:** `com.rykersoft.appmanager`
- **Version:** 1.2.9 (`versionCode` 16)
- **Minimum Android:** Android 7.0 / API 24
- **Target Android:** API 36
- **APK architectures:** `arm64-v8a`, `armeabi-v7a`, `x86`, and `x86_64`
- **Release signing certificate SHA-256:** `f1b2d0a742f03a714a84c42fa503dfd88ad6260938488b18e3cf865cd0ae21d6`

## Permissions and Android APIs

- `INTERNET` for registry, documentation, screenshots, and APK downloads
- `QUERY_ALL_PACKAGES` for installed-version discovery across the managed catalog
- `REQUEST_INSTALL_PACKAGES` for user-approved sideload installation
- `POST_NOTIFICATIONS` for update availability notifications
- Android `PackageInstaller` session API for installation and status callbacks
- `LauncherApps` and `UserManager` for detecting visible copies in related profiles
- `FileProvider` for the legacy installer fallback

## Architecture

- Single-activity Compose application with lifecycle-aware state flows
- Room database for the synchronized managed-app registry
- Repository layer for registry synchronization and dynamic Markdown documentation
- OkHttp streaming downloads with progress reporting and authenticated private-release support
- Pre-install package, signing-certificate, and downgrade validation
- PackageInstaller status receiver plus a dedicated confirmation activity
- Firebase Authentication and Firestore for RykerSoft account entitlements
- WorkManager-compatible background update scheduling

## Distribution and Security

- App Manager APKs are published on the public `rykergrey/RykerSoft` release page.
- Other RykerSoft APKs and documentation are distributed through the private `rykergrey/RykerSoft-APKs` repository.
- Gallery screenshots are public assets under `rykergrey/RykerSoft/screenshots/`.
- The manual release workflow restores signing material from encrypted GitHub Actions secrets when they are configured, then verifies the expected non-debug certificate, package ID, and version before publication.
- Android requires an uninstall before installing v1.1.1 or newer over the debug-signed v1.1.0 release; uninstalling clears local app data.
