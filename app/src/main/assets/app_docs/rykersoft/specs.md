# Technical Specifications

## Platform & Requirements
- **Target OS**: Android 7.0+ (API Level 24+)
- **Architecture**: Jetpack Compose, Kotlin, Room DB, Coroutines & Flow
- **Package ID**: `com.rykersoft.appmanager`
- **Version**: 1.2.2 (versionCode 9)
- **Permissions**: `INTERNET`, `REQUEST_INSTALL_PACKAGES`, `POST_NOTIFICATIONS`

## Architecture Highlights
- Single-activity architecture powered by Jetpack Compose
- Modern Room SQLite database storing synced application registries
- Direct OkHttp stream fetcher with download progress reporting; install via system `ACTION_VIEW` Package Installer (App Manager remains in the foreground task)
- Post-install resume detects the newly installed/updated package and focuses its User Guide tab
- Detail dialog default tab: Updates (outdated) / Description (not installed) / User Guide (up to date)
- Neo-brutalist custom design system: semantic color tokens (CTA yellow / success green / danger crimson / link cyan / brand magenta), hard 2D offset shadows, and a dual-font stack (monospace display + Inter body via Google Fonts)
