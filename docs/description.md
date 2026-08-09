# RykerSoft Application Manager

RykerSoft is the Android hub for the RykerSoft app collection. It keeps one catalog of available apps, compares installed versions with current releases, downloads signed APKs, guides Android's installer flow, and presents each app's documentation and screenshots.

## Key Features

- **Unified app catalog** — Browse RykerSoft apps and games from one dashboard with installed, update-ready, and new-release states.
- **Version-aware updates** — Compare Android version codes and open the newest release notes before updating.
- **Safer APK validation** — Check the downloaded package name, signing certificate, and version code before starting Android's installer.
- **Actionable install errors** — Distinguish signing-key conflicts, another-profile copies, corrupt APKs, insufficient storage, and incompatible devices.
- **Reliable installer flow** — Use `PackageInstaller` sessions and a dedicated confirmation host so Play Protect and system prompts remain visible.
- **Dynamic documentation** — Read descriptions, complete update history, specifications, user guides, and public screenshot galleries in the app.
- **In-place install reading** — Keep the open app detail card and its selected documentation tab visible while downloads and Android installation continue.
- **Platform availability** — See an informational Windows badge when a separate Windows edition exists, without exposing Windows downloads in the Android hub.
- **Automatic update checks** — Refresh on demand and optionally receive background notifications when newer releases are available.
- **Google RykerSoft account** — Sign in through Android Credential Manager to receive account-specific pro access without creating another password.
- **Safe legacy migration** — Verify an existing password account, link Google to that same Firebase UID, and retain password reset during migration.
- **Administrator-managed access** — Preserve per-application pro access on each Firebase UID while denying entitlement changes from untrusted clients.
- **Neo-brutalist interface** — Use a high-contrast cyber palette, clear status colors, hard shadows, and readable tabbed detail views.
- **Shareable APK links** — Copy an app's exact APK download URL from either the home card or its expanded detail actions.

## PRO Features

The App Manager itself remains free. A magenta `*` identifies optional capabilities inside connected apps that require RykerSoft Pro access.

* Connected-app pro features — Ask the RykerSoft administrator to enable an eligible app for your account, then sign in with the same Google account inside that app.
* Provider-backed tools — Supported apps can receive their entitled provider configuration after account sign-in and authorization.
