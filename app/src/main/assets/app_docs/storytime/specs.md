# Storytime Technical Specifications

## Application

- Package: `com.superstorycraft.ing`
- Release: `1.0.0` (`versionCode 1`)
- UI: React 19, TypeScript, Vite, and Tailwind CSS
- Android: Capacitor 8 with native Java bridges
- Windows: Electron 40, NSIS installer, and portable x86-64 executable
- Data: localStorage plus IndexedDB; stories and generated media remain local unless the user explicitly syncs

## Standard and Pro Boundary

- Standard functions do not call external AI providers.
- Provider-backed writing, artwork, creative assistance, cloud speech, and Story Party hosting are marked with `*` as Pro features.
- A personal provider key can be used as an optional local override.
- RykerSoft-managed keys are runtime-only and never enter localStorage, IndexedDB, story files, Drive backups, logs, source, or packaged artifacts.

## Firebase Boundaries

- App-owned Firebase project: `superstorycrafting`
  - Google authentication for online rooms
  - Ephemeral room, membership, response, and public-state documents
  - Public room names chosen by the player and separated from email/profile identity
- RykerSoft hub project: `rykersoft-abe84`
  - Named Firebase app: `rykersoft-hub`
  - Entitlement: `users/{hubUid}/entitlements/apps["com.superstorycraft.ing"] == true`
  - Provider record: `providerKeys/com.superstorycraft.ing`
  - Capability record: `appCapabilities/com.superstorycraft.ing`

Firebase UIDs are project-scoped. Storytime never treats app-project and hub-project UIDs as equal and never authorizes access from an email string supplied by the client.

## Provider Model

- Model: trusted-family runtime credential delivery
- Required default field: `gemini`
- Optional fields: `openai`, `kimi`, `elevenLabs`
- Entitled clients perform one exact-document read; list queries and every client provider write are denied.
- Credentials are cleared before every entitlement refresh, on sign-out, and when entitlement/provider access fails.
- Revocation prevents future retrieval. If a trusted device is lost, rotate the package-specific provider credentials.

## Authentication

- Android app-data sign-in uses the Capacitor Firebase Authentication native Google flow.
- Android hub sign-in uses a dedicated Credential Manager bridge with the hub web client ID, then imports the short-lived Google credential into the named hub Firebase Auth instance.
- Windows app-data and hub sign-in use a temporary loopback Firebase helper opened in the system browser. The helper uses in-memory persistence and returns only short-lived credentials through narrow IPC.
- Web development uses Firebase Google popup authentication.
- Google Drive authorization is separate from Firebase authentication.

## Security and Privacy

- Electron context isolation, sandboxing, navigation restrictions, and a narrow preload bridge remain enabled.
- No OAuth client secret, service-account credential, provider key, entitlement-writing authority, or reusable family code is packaged.
- Online-room public data excludes email, Firebase UID presentation, tokens, provider keys, and entitlement state.
- Personal keys are masked by default with accessible show/hide controls and may be removed independently.
- Android backups are disabled at the application manifest level.

## Release Targets

- Signed non-debug Android APK, package `com.superstorycraft.ing`
- Windows NSIS installer and portable x86-64 executable
- Public artifacts and documentation in `rykergrey/RykerSoft-APKs`
- Registry activation in `rykergrey/RykerSoft` occurs only after artifacts, capability state, and screenshots are verified
