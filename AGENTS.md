# Working on RykerSoft

This repository is the native Android RykerSoft Application Manager and its live app catalog. It is also the reference for publishing other applications to the hub.

## Deployment knowledge

For a request to deploy, publish, onboard, or update an app in RykerSoft, use [the RykerSoft deployment skill](skills/rykersoft-deploy/SKILL.md). Its references explain the [distribution contract](skills/rykersoft-deploy/references/deployment.md), [app-specific build patterns](skills/rykersoft-deploy/references/app-release-patterns.md), and [account/capability integration](skills/rykersoft-deploy/references/capabilities.md).

The skill's version-controlled source is here; a discoverable copy is installed at `~/.codex/skills/rykersoft-deploy`. Keep both copies in sync when deliberately updating the workflow. Recheck current code and live release metadata rather than copying old version numbers.

## Repository map

- `registry.json`: live catalog fetched from this repository's `main`; update only the intended entries in the complete list.
- `app/src/main/java/com/rykersoft/appmanager/`: Kotlin/Compose app, Room catalog cache, downloader/installer, Firebase identity and administration.
- `app/build.gradle.kts`: hub package/version/build/signing configuration.
- `screenshots/`: public app galleries referenced by the catalog.
- `icons/`: one 512 px icon per catalog `id`, referenced by `iconUrl`; sources for hub-drawn icons are in `icons/src/`.
- `web/site/`: the website (React, Tailwind, Base UI). It builds `catalog.json` from `registry.json`; see `web/README.md`.
- `web/apps/`: browser editions of the web-built apps, served from the VPS at `<id>.rykersoft.rykergrey.com`; see `web/apps/README.md`.
- `DESIGN.md`, `PRODUCT.md`: the hub's design rules and purpose, for both the Android app and the website.
- `app/src/main/assets/app_docs/`: packaged offline documentation; root `app_docs/` and `docs/` are separate copies, not automatically bundled.
- `firestore.rules`, `firestore-tests/`: hub backend access rules and emulator tests.
- `.rykersoft/app-capabilities.json`: local capability declaration, not runtime registration.
- `rykergrey/RykerSoft-APKs`: separate public GitHub distribution repository for most release assets and remote `docs/<computed-slug>/` documents.

## Development checks and boundaries

Use the current Gradle wrapper. Java 21 matches the CI/Robolectric requirement. For app code changes, the existing CI check is:

```bash
./gradlew testDebugUnitTest assembleDebug --no-daemon --no-configuration-cache
```

Run checks relevant to the change. Documentation-only work needs reference/format checks; catalog-only changes need full JSON and artifact/doc URL verification, not an unnecessary hub rebuild. Firestore access changes require emulator tests and review of the actual deployed contract before a scoped deployment.

Production APKs are signed locally. CI is read-only and builds debug validation artifacts. Preserve the published package ID and signing certificate. Never commit ignored local Firebase/signing configuration or use debug signing as a release substitute.

Publish verified artifacts and documents before catalog activation. Preserve unrelated app entries, existing user data, UIDs/grants, and untouched platform URLs. A catalog change does not require account migrations or a Firebase rules deployment.

Older README release URLs and some Pro integration prose are historical. The deployment references document known discrepancies, including computed documentation slugs and Hyperscribe code/rules inconsistencies. Verify current code and deployed state before resolving them; do not silently expand an ordinary app release into unrelated repairs.
