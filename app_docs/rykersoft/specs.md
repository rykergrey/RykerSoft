# Technical Specifications

## Platform & Requirements
- **Target OS**: Android 7.0+ (API Level 24+)
- **Architecture**: Jetpack Compose, Kotlin, Room DB, Coroutines & Flow
- **Package ID**: `com.rykersoft.appmanager`
- **Version**: 1.2.5 (versionCode 12)
- **Permissions**: `INTERNET`, `REQUEST_INSTALL_PACKAGES`, `POST_NOTIFICATIONS`

## Architecture Highlights
- Single-activity architecture powered by Jetpack Compose
- Modern Room SQLite database storing synced application registries
- Direct OkHttp stream fetcher with download progress reporting
- Installs via `PackageInstaller` sessions + `InstallConfirmationActivity` host for Play Protect / user confirmation (avoids Compose Dialog burying the prompt)
- Post-install callback brings the hub forward and focuses the User Guide tab
- Detail dialog default tab: Updates (outdated) / Description (not installed) / User Guide (up to date)
- Neo-brutalist custom design system: semantic color tokens (CTA yellow / success green / danger crimson / link cyan / brand magenta), hard 2D offset shadows, and a dual-font stack (monospace display + Inter body via Google Fonts)
