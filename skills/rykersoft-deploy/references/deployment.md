# RykerSoft distribution contract

Verified against hub commit `1f0e33d2ff3571a951cf8a5a9a81fc0b1f992d62` on 2026-09-30. Re-read the current source and live catalog for future releases; this is workflow knowledge, not a pinned release target.

## Repositories and activation

| Concern | Authoritative location |
| --- | --- |
| Hub source, catalog, screenshot galleries | `https://github.com/rykergrey/RykerSoft` |
| Default live catalog | `https://raw.githubusercontent.com/rykergrey/RykerSoft/main/registry.json` |
| Normal public artifact host | `https://github.com/rykergrey/RykerSoft-APKs/releases` |
| Normal remote app docs | `RykerSoft-APKs`, branch `main`, `docs/<computed-slug>/` |
| App source/build/signing | That app's own source repository |
| Hub identity and capability backend | Firebase project `rykersoft-abe84` |

The hub fetches the catalog, enriches its docs, and caches it in Room. Catalog changes on `main` reach existing hub installations on their next successful refresh. Custom registry URLs exist, so diagnose the actual configured URL when a device disagrees with the default catalog.

Use explicit versioned release asset URLs, not a repository-wide `latest` shortcut: one distribution repository contains many applications. The typical public tag is `<release-slug>-v<version>`; source tags commonly use `v<version>` but vary by project. Asset filenames also vary. Inspect existing releases before choosing names. The hub's README contains old release URL examples; the current registry is a better guide to the actual public host.

## Registry schema

Preserve the root object `{ "apps": [...] }`. A bare array also parses, but changing the established shape is unnecessary. Entries are keyed by unique `packageName`:

| Field | Meaning and publishing rule |
| --- | --- |
| `packageName`, `name` | Nonblank identity and display name. Android identity must match the manifest in the APK. Desktop entries use their established catalog identifier. |
| `latestVersionCode` | Integer; Android update detection is `installedCode < latestVersionCode`. Missing values parse permissively, so validate explicitly. |
| `latestVersionName` | Display version; match the release APK's actual manifest. |
| `apkUrl` | Direct anonymous APK asset URL for Android apps. |
| `exeUrl`, `linuxUrl` | Optional existing desktop binaries. At least one of these or `apkUrl` must be nonblank. Desktop-only entries are supported. |
| `icon` | Existing keyword, normally `android` or `desktop`; not a custom icon file URL. |
| `isGame` | Explicit Boolean controls Apps/Games placement; otherwise the parser guesses from names. |
| `changelog` | Short release highlights; also used when longer update history is unavailable. |
| `screenshots` | Prefer an array of direct public image URLs; empty arrays are supported. |
| `description`, `updatesHistory`, `specs` | Inline content. `description` is one or two plain paragraphs. `updatesHistory` is Markdown in the fixed shape `## <version> · <yyyy-mm-dd>` followed by `- ` items, newest first; its first heading must be `latestVersionName`. Nonblank values suppress remote doc loading. |
| `id` | Stable slug for the product. Entries that share an `id` are one product on different platforms (yoink has two). It names the icon file and the website address. |
| `tagline` | One sentence for lists and cards. |
| `features` | Array of four to seven short lines. |
| `pro` | One or two sentences on what Pro adds and what stays free. Empty string when the whole app is free. |
| `iconUrl` | `https://raw.githubusercontent.com/rykergrey/RykerSoft/main/icons/<id>.png` (512 px PNG committed under `icons/`). |
| `releasedAt` | Date of the newest published file, `yyyy-mm-dd`. Drives the "Latest release" sort. |
| `linuxDebUrl`, `linuxPacmanUrl` | Extra Linux packages, with matching `linuxDebSha256` / `linuxPacmanSha256`. |
| `webUrl` | Address of the app's browser edition, `https://<id>.rykersoft.rykergrey.com`. Set only for apps listed in `web/apps/apps.json` and deployed with `web/apps/deploy.mjs`. The Android hub ignores it; the website links to it. The hub's own entry points at the website. |
| `userGuide` | Not used. Guides are not shown until they are rewritten; do not add this field. |
| `apkSha256`, `exeSha256`, `linuxSha256`, other platform fields | Present in some entries; preserve them and recompute hashes for artifacts you replace. The current Android parser does not consume/enforce hashes or `linuxDebUrl`/`linuxPacmanUrl`. Do not claim client-side hash enforcement. |

The parser skips invalid entries, and `ManagedAppDao.syncRemoteApps` removes cached apps absent from the parsed list. Validate the full catalog and compare its identities to the previous live version; do not publish a one-app fragment or accidentally invalidate other rows.

Example structure only; derive all values from the real artifact:

```json
{
  "packageName": "com.example.actualapp",
  "name": "Example",
  "latestVersionCode": 42,
  "latestVersionName": "2.3.0",
  "apkUrl": "https://github.com/rykergrey/RykerSoft-APKs/releases/download/example-v2.3.0/app-release.apk",
  "icon": "android",
  "changelog": "Release highlights grounded in shipped changes",
  "screenshots": [],
  "isGame": false
}
```

Do not impose `com.rykersoft.*` on an existing app. Storytime uses `com.superstorycraft.ing`; yoink Android uses `app.flux.download`. Changing either would break update continuity and identity-based integration.

## Documentation and galleries

`AppRepository.enrichAppWithRemoteDocs` selects `apkUrl`, else `exeUrl`, else `linuxUrl`, then extracts its GitHub owner/repository. From that repository's **main** branch it fetches:

- `docs/<computed-slug>/description.md`
- `docs/<computed-slug>/updates.md`
- `docs/<computed-slug>/specs.md`
- `docs/<computed-slug>/user_guide.md`

The fallback is root `docs/<file>`, then `README.md` for description or `CHANGELOG.md`/short changelog for updates. A missing app document can therefore show the distribution repository's generic README instead of throwing an obvious error. Verify exact paths and content. Non-GitHub artifact hosts are not supported by this repository inference; use appropriate inline fields or make a scoped hub change if introducing one.

Current slug logic (`AppRepository.appSlug`, duplicated in `AppViewModel.appSlug`):

- Special cases for RykerSoft, INFORMANT, Rush, Synthing, SuperThink.ing, and bettertracking map to `rykersoft`, `informant`, `rush`, `synthing`, `superthinking`, and `bettertracking` respectively.
- Package IDs containing `hyperscribemobile` / `hyperscribedesktop` map to `hyperscribe-mobile` / `hyperscribe-desktop`.
- Otherwise lowercase the **display name** and remove everything outside `[a-z0-9]`.

Consequences: `WordPlay` → `wordplay` (not `wordplaying`); `Roadtrip Copilot` → `roadtripcopilot` (not `roadtripper`); `yoink. (Android)` → `yoinkandroid`. Keep documentation reachable at the path current clients compute, even if the release tag and screenshot folder use another spelling. If changing slug handling in code, update both implementations and account for older installed hubs.

Create concise description, chronological updates, requirements/specs, and a usable guide from the actual app. Preserve older update history. Pro apps also follow the documentation requirements in [capabilities.md](capabilities.md). Publish linked supporting files for GitHub/web readers, but do not promise those links work in the hub: the current detail-view Markdown handler handles in-document anchors, ignores HTTP(S) clicks, and treats other link targets as anchors rather than navigating to another document. Keep essential instructions in the four loaded documents.

Screenshots usually live in `RykerSoft/screenshots/<release-slug>/` and are linked explicitly by the registry. Existing guidance prefers three or more useful images, but the client accepts fewer and several real entries have none. Empty galleries currently trigger generic placeholder images in the UI. Preserve existing good galleries; update screenshots when the release changes the experience. Do not fabricate screenshots to satisfy a count. Filename/versioned or commit-pinned paths can avoid stale images.

Root `app_docs/` and `docs/` are not automatically packaged into the APK. Offline fallback files are under `app/src/main/assets/app_docs/<slug>/`. Update the relevant copies intentionally when changing bundled hub docs. Publishing remote docs or another app's catalog entry alone does not require a hub rebuild.

## Release procedure

Choose the applicable scope before following the sequence below:

| Request | Work needed |
| --- | --- |
| Existing app binary update | Verify source/build/signing, publish the requested platforms, update relevant docs, verify existing capability requirements, then activate the focused catalog change. |
| New app onboarding | Establish package/signer, publish binaries and all four docs, register the appropriate capability (including a free/no-provider declaration), add one complete catalog entry, then verify installation. |
| Hub implementation release | Run the hub's relevant checks, build/sign its APK, and publish it through the same process. |
| Release notes, description, screenshot, or other metadata-only correction | Edit the requested public docs/images or inline catalog fields and verify them. Preserve version codes/names and artifact URLs. Skip builds, tags, release creation, signer checks, and capability writes unless directly needed by the correction. |

For example, a Storytime release-notes correction normally updates public `docs/storytime/updates.md` (and its maintained source counterpart); change `changelog` only if the requested short summary needs it. It does not require rewriting the other three documents, releasing an APK, or touching Pro configuration. Check for a nonblank inline `updatesHistory` that could override the document.

1. **Establish a fresh baseline.** Inspect source and hub remotes, branches, `git status`, current source release history, live registry entry, and public tag/assets. Locate the intended source revision; a local app checkout can be behind its published version. Use a separate clean checkout/worktree if existing work would be mixed. Do not reset someone else's work or assume the oldest `RykerSoft-APKs` local clone is current.
2. **Set the release versions.** For an Android binary update, pick a code strictly above the published APK/catalog baseline (and any known installed higher build). Align app-specific manifests/configuration, release notes, and build inputs. A documentation-only correction needs no artificial version bump. Keep unchanged desktop pointers valid if only Android is requested.
3. **Build and verify.** Follow the target repository's instructions and existing scripts. Native Android normally uses its Gradle wrapper. Capacitor projects must build the web app and sync/copy it into Android before Gradle. Other stacks have different commands. Run relevant tests. Build with the established local signing credentials and keep them out of output, commits, CI, and artifacts.
4. **Inspect the actual APK.** Use Android SDK `aapt dump badging` or `apkanalyzer manifest application-id/version-code/version-name`, `apksigner verify --verbose --print-certs`, and `sha256sum`. Compare signing certificate SHA-256 with a trusted previous release or the project's established fingerprint. Confirm package, version, supported SDK, and that no debug credentials or costly provider/admin tokens were baked into the release. A successful Gradle release task is insufficient: some projects fall back to debug signing if local config is absent.
5. **Prepare docs and backend requirements.** Stage the four documents at the computed public slug, review gallery URLs and release notes, and inspect `.rykersoft/app-capabilities.json` where present. Complete only backend/capability changes actually required for this release; follow [capabilities.md](capabilities.md) for Pro/provider integration. A normal catalog update is not a reason to redeploy hub rules or alter grants.
6. **Publish artifacts and docs first.** Tag the verified source revision according to that project's convention; upload the reviewed binaries to a new public release. Publish documents to the distribution repository's `main`, and publish any new screenshot files before activation. Do not turn private source history public to expose downloads. If the public tag already exists, inspect its assets/hashes and resume only if they match the intended immutable release; use a new release for changed bytes.
7. **Verify anonymously.** Download the public asset without GitHub credentials, follow redirects, verify its full hash and APK identity/signature again, and fetch each exact Markdown/image URL. Confirm content, not just HTTP success; an HTML error/login page is not an APK. Authenticated `gh` commands are useful for publishing but cannot establish anonymous readability.
8. **Activate the focused catalog change.** Refresh the hub branch immediately before editing. Preserve other packages and all fields/URLs outside the requested platforms. Validate the full JSON, unique package IDs, positive integer codes, versions, explicit game status, direct HTTPS links, and artifact parity. Review the diff. Commit the focused change and push it to `main` within the deployment request. If the push is rejected, fetch and reapply on the new head; never force-push.
9. **Verify the public result.** Re-fetch the default raw registry (cache-busting query if needed) until it reflects the intended commit, with bounded retries. Confirm every changed URL and the exact document slugs. Refresh the hub and exercise update/install and docs on a suitable device when available. Report any device/backend check that was not performed.

If a required check fails, do not activate the new catalog entry. An already uploaded, unreferenced release can remain while the blocker is resolved; report its state rather than claiming a completed deployment. Do not loop by uploading the same failing artifact. If credentials, intended source, or signing continuity cannot be established, finish independent preparation and ask for the missing input.

Illustrative commands after resolving the actual paths and values (not a one-size build script):

```bash
# In the source repository; commands assume the Android SDK tools are on PATH.
aapt dump badging "$apk_path"
apksigner verify --verbose --print-certs "$apk_path"
sha256sum "$apk_path"

# Public release after build/signature verification and release authorization.
# If already published, inspect/resume; do not overwrite release bytes.
gh release create "$public_tag" "$apk_path" \
  --repo rykergrey/RykerSoft-APKs --title "$release_title" \
  --notes-file "$notes_path" --latest=false

# Anonymous verification. -q disables user curl config that could add auth.
curl -q --fail --location --proto '=https' --proto-redir '=https' \
  "$public_apk_url" --output "$downloaded_apk_path"
sha256sum "$downloaded_apk_path"
apksigner verify --verbose --print-certs "$downloaded_apk_path"

# In the hub checkout, before committing the catalog.
python -m json.tool registry.json > /dev/null
git diff --check
git diff -- registry.json
```

Metadata rollback means restoring a prior known-good catalog entry or correcting URLs, without disturbing other apps. It does not make Android downgrade installed versions. A code fix for devices that already installed a bad APK normally needs another release with a higher version code and the same signing identity; uninstalling is not an acceptable shortcut to signature/upgrade problems.

## Releasing or developing the hub itself

The hub is native Kotlin/Compose with Room, OkHttp, Firebase Auth/Firestore, and the Android package installer. Its package is `com.rykersoft.appmanager`. Code and version live in `app/build.gradle.kts`; use the current wrapper/toolchain rather than copying dependency versions from this document.

- Java 21 is used by `.github/workflows/android-validation.yml` because the current Robolectric API 36 sandbox requires it, even though application bytecode targets Java 11.
- CI runs `./gradlew testDebugUnitTest assembleDebug --no-daemon --no-configuration-cache` with read-only repository permissions. It does not publish signed releases.
- A local hub release uses `./gradlew testDebugUnitTest assembleRelease --no-daemon --no-configuration-cache`, with ignored local `keystore.properties` and required Firebase build configuration. APK output is normally `app/build/outputs/apk/release/app-release.apk`; verify rather than assuming signing occurred.
- Inspect `.env.example`, `local.properties` requirements, and `.gitignore`. `.env`, `keystore.properties`, keystores, and `app/google-services.json` are local configuration; do not commit them. A fresh checkout is not automatically ready for a production build.
- For relevant Firestore changes, install the locked `firestore-tests` dependencies, use the Firebase emulator, and run its Node tests. These tests clear emulator state; never point test commands at production. Deploying rules is a separate, scoped backend change, not an automatic APK-release step.
- Update the hub's own catalog row and public documents through the same artifact-first process. A hub release may also require synchronized bundled docs, Pro catalog constants, or schema logic depending on the implementation change.

## Implementation evidence

Paths relative to the hub repository:

- `app/src/main/java/com/rykersoft/appmanager/network/RegistryFetcher.kt`: parsing and anonymous HTTP fetching.
- `app/src/main/java/com/rykersoft/appmanager/data/AppRepository.kt`: source-repository inference, slug, remote docs, refresh.
- `app/src/main/java/com/rykersoft/appmanager/data/ManagedAppDao.kt`: full-list synchronization and deletion of absent packages.
- `app/src/main/java/com/rykersoft/appmanager/ui/AppViewModel.kt`: default URL, installed-version checks, bundled docs and duplicated slug logic.
- `app/src/main/java/com/rykersoft/appmanager/util/ApkManager.kt`: downloading/installing APKs and package checks.
- `app/src/main/java/com/rykersoft/appmanager/entitlements/`: account, capability, and Pro behavior.
- `app/src/test/java/com/rykersoft/appmanager/network/RegistryFetcherDesktopTest.kt`: desktop-only catalog behavior.


## User-facing wording (decided 2026-11-10)

`changelog` and `updatesHistory` are written for the person using the app, not as a development record.

- Say what is new or different on screen: features, layout changes, new platforms.
- Leave out signing, package ids, version codes, test coverage, entitlement internals, build tooling and fixes for mistakes that never mattered to a user.
- A fix worth mentioning is phrased as what now works, in one line.
- Free and Pro: a feature is Pro when it could cost a lot of money if many people used it. That covers anything on a paid API key (AI, transcription, paid lookups) and anything that stores user data on the RykerSoft cloud account (Hyperscribe Sync). Everything else is free. The `pro` field names only those features and says what stays free.
- `changelog` is the current version's items joined with ` | `. Keep it identical to the first block of `updatesHistory`.
- On every release also set `releasedAt`, and update `tagline`, `description` and `features` if the release changed what the app is.

`RegistryContentFieldsTest` parses the repository's `registry.json` and fails when an entry lacks these fields or its icon file.

The website (`web/site`) builds its own `catalog.json` from `registry.json` plus GitHub release sizes and dates; see `web/README.md`.

## Web editions

Some apps also run in the browser from the VPS; `web/apps/apps.json` lists them and `web/apps/README.md` explains the build, deploy and sign-in setup. A web edition is built from a pinned commit, so releasing a new version of such an app leaves its web edition behind until `apps.json` points at the new release and `build.mjs` and `deploy.mjs` have run. The website shows a behind web edition in the warning colour. Treat the web edition as one more platform of the release: update it when the request covers it, and say so when it was left behind. Keep the app's `webUrl` unchanged; its address must never move, because browser-saved data stays with the address.
