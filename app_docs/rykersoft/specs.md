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
- Neumorphic custom design system with dynamic HSL dark mode palettes
