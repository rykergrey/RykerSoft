# Application release patterns

These examples were checked against local source and the RykerSoft catalog on 2026-09-30. They are discovery aids, not frozen release configuration. Read the current source, release scripts, registry, and prior distributed artifact before each release. Source folders can move; local checkouts can lag behind the published catalog. Check `git remote get-url origin`, branch, worktree status, and release history before selecting a checkout.

## Catalog identities and source hints

Folder hints below are relative to `/home/ryker/Work`; locate the repository by its remote on another machine. Platform letters mean Android, Windows, and Linux URLs are present in the inspected catalog, not that all platforms share a version.

| Catalog name | Published package identity | Local source hint | Platforms / build family |
| --- | --- | --- | --- |
| RykerSoft | `com.rykersoft.appmanager` | `RykerSoft` | A; native Gradle |
| Hyperscribe Mobile | `com.rykersoft.hyperscribemobile` | `Hyperscribe-Mobile` | A; native Gradle |
| Hyperscribe Desktop | `com.rykersoft.hyperscribedesktop` | `Hyperscribe-Desktop` | W/L; Python packaging |
| INFORMANT | `com.rykersoft.informant` | `INFORMANT` | A/W; Capacitor/Electron |
| Rush | `com.rykersoft.rush` | Not located in this workspace | A; inspect source before choosing tools |
| Synthing | `com.rykersoft.synthing` | `SynthingAndroid/Synthing` | A; native Gradle/C++ |
| SuperThink.ing | `com.rykersoft.superthinking` | `SuperThinking` | A/W/L; Capacitor/Electron |
| bettertracking | `com.rykersoft.bettertracking` | `bettertracking` | A/W/L; Capacitor/Electron |
| WordPlay | `com.rykersoft.wordplaying` | `WordPlaying` | A/W/L; Capacitor/Electron |
| Photocraft.ing | `com.rykersoft.photocrafting` | `Photocrafting` | A/W; Capacitor/Electron |
| FreeBall.ing | `com.rykersoft.freeballing` | `FreeBalling` | A/W/L; Godot |
| Storytime | `com.superstorycraft.ing` | `StoryTime` | A/W; Capacitor/Electron |
| Roadtrip Copilot | `com.rykersoft.roadtripper` | `Roadtripper` | A; Capacitor with separate mobile build |
| ComicCraft.ing | `com.rykersoft.comiccrafting` | `comiccrafting` | A/W/L; Capacitor/Electron |
| yoink. | `com.rykersoft.yoink` | `yoink` | W/L; Tauri |
| yoink. (Android) | `app.flux.download` | `yoink/android` | A; native WebView host |

The two yoink entries share a source repository and release family but have distinct package identities. Storytime and yoink Android intentionally retain their existing IDs. Never rename a published package to satisfy an old naming convention: package identity and signing identity determine whether existing installations can update without data loss.

## Build examples

Run these from the named source root unless stated otherwise. Confirm toolchain prerequisites and signing availability first. They illustrate evidenced build paths; publication remains a separate operation.

### Native Android: Hyperscribe Mobile

```sh
bash scripts/build_release.sh
```

This checked-in Linux script runs JVM tests and debug lint, builds the release APK and AAB, and invokes `scripts/verify_apk_signature.sh` for the release certificate. It reads ignored local signing custody files and supplies process-scoped environment variables; do not print those files or enable shell tracing. The optional `--skip-tests` argument exists but should only be used when the required checks already passed for the exact source being built.

Output is `app/build/outputs/apk/release/app-release.apk`, with the AAB under `app/build/outputs/bundle/release/`. The PowerShell counterpart is `scripts/build_release.ps1`. `app/build.gradle.kts` rejects a release task without complete signing configuration. Re-read the verifier and compare the previous public APK; do not copy its current certificate fingerprint into general instructions as a permanent constant.

### Native Android: Synthing

```sh
./gradlew testDebugUnitTest
./gradlew assembleRelease
```

Source authority: `SynthingAndroid/Synthing/docs/DEVELOPMENT.md` and `app/build.gradle.kts`. The project uses a native Oboe/C++ engine and declares its NDK/CMake requirements. Release builds use R8 and resource shrinking, so validate the optimized artifact after changes to native bindings or serialization.

**Signing trap:** when `keystore.properties` is absent, this project's release configuration falls back to the debug key. A successful `assembleRelease` does not establish a distributable production signature. Verify the resulting APK against the prior public artifact and resolve signing custody before publication.

### Capacitor: WordPlay and Storytime

```sh
npm run build:android
cd android
./gradlew assembleRelease
```

Both projects define `build:android` as a Vite production build followed by Capacitor synchronization. Their Gradle release signing uses the ignored Android-root `keystore.properties`; missing signing configuration can produce an unsigned release. Verify the APK, package/version, and signer explicitly.

Keep `package.json` version and Android `versionName` aligned, and increase Android `versionCode` beyond the activated distribution. Confirm packaged web assets match the final production `dist`, especially when rebuilding several platforms. WordPlay's `release/*-package-verification.json` and `*-release-record.json` show this evidence pattern.

The PowerShell `deploy-android.ps1`, `android:build:apk`, and `release:all` commands in these projects build/install **debug** Android APKs. Their names do not make them hub publication commands. Storytime's `docs/in-app-updates.md` requires the matching complete public release before registry activation and enforces installed package, increasing version code, and exact signing-certificate match.

### Capacitor: ComicCraft.ing and Roadtrip Copilot

ComicCraft.ing provides a dedicated Linux release command:

```sh
npm run android:release
```

`comiccrafting/scripts/build-release.sh` performs TypeScript checks, tests, web build/sync, Gradle release assembly, signature verification, and SHA-256 generation. It stages `release/<version>/ComicCraft.ing-<version>.apk`. Check its JDK and Android build-tools paths for the current machine. `scripts/verify-release.py` scans the artifact for unintended credentials and private files; it reads local configuration, so inspect its prerequisites without displaying secret values.

Roadtrip Copilot's mobile UI is separate from its general web build. `npm run build:android` calls `build:mobile` and Capacitor sync. Its Windows `npm run build:android:release` invokes `scripts/build-android-release.ps1`, which requires production signing environment variables, builds the APK, and checks the signer against `.rykersoft/deploy.json`. Do not substitute its general `npm run build` for `build:mobile`.

### Other build families and platform cautions

- INFORMANT uses `build:capacitor`, not its ordinary web build, for the Capacitor-specific output. Inspect `package.json`, `vite.config.*`, and `AGENTS.md` before syncing Android.
- SuperThinking's `android:build:apk` and `android:build:aab` invoke `scripts/bump-android-version.js` before building; repeated invocation can bump versions again. They also use the Windows wrapper. Its desktop source lives under `electron/` with separate dependencies.
- FreeBalling is a Godot project (`project.godot`), with a native Android auth plugin under `native/android/freeballing_auth`. Inspect current export presets and release tooling without dumping embedded signing values. Do not apply a Capacitor or Flutter command to it.
- Hyperscribe Desktop contains `build_exe.py`, `build_linux.py`, and PyInstaller spec files. Inspect these for the chosen target; no Android build is implied by a desktop release.
- No Flutter source was found in the inspected app folders. If a future app has `pubspec.yaml`, follow its actual Flutter release/signing configuration rather than these Gradle examples.

### yoink: Tauri desktop and native Android WebView

```sh
npm run build
cd android
./gradlew testDebugUnitTest lintDebug assembleRelease
```

The Android Gradle project copies the root `dist` into generated web assets and fails if `dist/index.html` is absent. It is not Capacitor. The inspected Gradle file does not configure release signing; its release APK needs the established release-signing path before distribution. `docs/verification.md` records production signer continuity and distinguishes public release APKs from development-signed device installations.

Desktop builds use Tauri. `.github/workflows/desktop-release.yml` produces Windows EXE/MSI and Linux AppImage/DEB artifacts and publishes version-tag releases. `README.md` requires tag alignment with `package.json`, `src-tauri/Cargo.toml`, and `src-tauri/tauri.conf.json`. Confirm the workflow ran successfully for the exact release commit. Do not replace a successful CI artifact with a failed local packaging attempt.

## Publication and verification patterns

1. Read the current registry entry and previous public release before selecting a new version. Local source may be behind the catalog; do not release a lower code merely because it exceeds the checkout's old value.
2. Preserve each installed package ID and its signing continuity. Use `apksigner verify --print-certs` plus Android package metadata inspection (`aapt dump badging` or equivalent). Compare with the previous distributed APK. Never uninstall or clear app data to bypass a mismatch.
3. Preserve existing release naming. Common source tags are `v<version>`; public distribution tags are `<slug>-v<version>` in `rykergrey/RykerSoft-APKs`. Asset filenames vary: `app-release.apk`, versioned WordPlay/yoink names, `Roadtrip-Copilot-v<version>.apk`, and `ComicCraft.ing-<version>.apk` are all evidenced.
4. Publish only reviewed binaries as GitHub Release assets; do not commit APKs, signing material, or private source history into the public mirror. Keep source repositories private when they already are private. A source release and a public mirror release can coexist.
5. Author release documentation in the app source `docs/`. Mirror reviewed `description.md`, `updates.md`, `specs.md`, and `user_guide.md`, plus linked supporting documents, into the catalog-selected public docs folder. Existing folders include `wordplaying`, `hyperscribe-mobile`, `synthing`, and `storytime`, but current WordPlay clients compute `wordplay`, so publishing only `wordplaying` is insufficient. Roadtrip Copilot similarly computes `roadtripcopilot`. yoink desktop and Android use separate `yoink` and `yoinkandroid` docs folders. Resolve paths using the hub's current slug mapping as detailed in [deployment.md](deployment.md).
6. Validate the user guide's clickable `## Table of Contents` and anchors. Use publicly accessible screenshot URLs; the hub has no private-repository download credential.
7. Check anonymous download access and verify downloaded asset SHA-256 against the local verified artifact. Capture package/version, signature evidence, source commit, public asset URL/digest, docs commit, and validation results without recording credentials.
8. Activate the registry only after artifacts and documentation are available. Keep checksum fields with their matching URLs. Preserve untouched platform URLs/checksums: Storytime demonstrates that Android and Windows can intentionally point to different release versions. Desktop-only entries and empty galleries already exist; a universal APK or three-new-screenshots requirement would misrepresent current behavior.

## Evidence locations

- Hub: `RykerSoft/registry.json`; public distribution policy: `RykerSoft-APKs/README.md`.
- Native: `Hyperscribe-Mobile/scripts/build_release.{sh,ps1}`, `scripts/verify_apk_signature.sh`, `app/build.gradle.kts`; `SynthingAndroid/Synthing/app/build.gradle.kts`, `docs/DEVELOPMENT.md`.
- Capacitor: each app's `package.json` and `android/app/build.gradle`; `StoryTime/docs/in-app-updates.md`; `WordPlaying/release/*-package-verification.json` and `*-release-record.json`.
- Dedicated release scripts: `comiccrafting/scripts/build-release.sh`, `scripts/verify-release.py`; `Roadtripper/scripts/build-android-release.ps1`.
- Other families: `FreeBalling/project.godot`; `Hyperscribe-Desktop/build_{exe,linux}.py`; `yoink/android/app/build.gradle.kts`, `.github/workflows/desktop-release.yml`, `docs/verification.md`.
- Historical guidance: `SynthingAndroid/Synthing/.cursor/rules/rykersoft-hub-deploy.mdc`. Useful for mirror/docs flow, but its blanket package renaming, APK-only eligibility, and fixed gallery-count assumptions are superseded by current source and catalog evidence.
