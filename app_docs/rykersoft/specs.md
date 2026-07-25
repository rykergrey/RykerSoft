# Technical Specifications

## Platform & Requirements
- **Target OS**: Android 7.0+ (API Level 24+)
- **Architecture**: Jetpack Compose, Kotlin, Room DB, Coroutines & Flow
- **Package ID**: `com.rykersoft.appmanager`
- **Permissions**: `INTERNET`, `REQUEST_INSTALL_PACKAGES`, `POST_NOTIFICATIONS`

## Architecture Highlights
- Single-activity architecture powered by Jetpack Compose
- Modern Room SQLite database storing synced application registries
- Direct OkHttp stream fetcher with background download progress reporting
- Neo-brutalist custom design system: semantic color tokens (CTA yellow / success green / danger crimson / link cyan / brand magenta), hard 2D offset shadows, and a dual-font stack (monospace display + Inter body via Google Fonts)
