---
name: rykersoft-deploy
description: Publish or update an application in RykerSoft, Ryker Grey's Android app hub; onboard catalog entries, verify signed releases, publish public artifacts and documentation, or release the hub itself. Use for requests to deploy an app to RykerSoft or maintain its distribution workflow, including cataloged Windows/Linux editions. Ordinary app development without a hub release does not require this workflow.
---

# Deploy an application to RykerSoft

RykerSoft is an Android application manager whose live catalog is `registry.json` on `rykergrey/RykerSoft`'s `main`. Most binaries and remote app docs are in the separate public repository `rykergrey/RykerSoft-APKs`. Source projects can remain private. Publishing another app usually requires no hub APK rebuild.

Read [the deployment contract and procedure](references/deployment.md) for a deployment. Use [app release patterns](references/app-release-patterns.md) to locate an existing source project and its build/signing conventions. Read [capability integration](references/capabilities.md) for every new-app onboarding (including free apps), or when the app has RykerSoft account, Pro, provider, or backend requirements.

## Start from the actual request and current state

- An explicit request to deploy/release to RykerSoft normally includes publishing the requested artifacts, documentation, and catalog change. Preparation, learning, or review requests do not authorize a release. Respect already established scope; do not add a redundant approval step.
- Find the requested app's source checkout and verify its Git remote, branch, changes, and release configuration. `/home/ryker/Work/RykerSoft` is the dedicated hub checkout on this machine; other folders such as `SynthingAndroid/RykerSoft` or `RykerSoft-global-chat-release` may be older or busy. Choose by verified state, not name alone. Preserve unrelated work.
- Read the live registry and remote heads each time. Versions, local paths, tags, assets, and signing arrangements in these references are discovery hints, not permanent facts. Match entries by exact package identity.
- Classify the task: existing-app update, new-app onboarding, hub implementation release, or metadata-only change. Keep additional platform releases and backend changes within the user's scope.
- For metadata-only work, edit only the requested document/catalog fields and verify their public result. Preserve versions and artifact URLs; skip binary builds, tagging, new releases, signing checks, and capability writes unless the requested correction actually concerns them.

## Required release invariants

1. Preserve the existing Android application ID and trusted signing identity. Inspect the built APK; a task named `assembleRelease` does not prove production signing. An Android update requires a strictly greater integer `versionCode`; the registry's version code/name must match that APK.
2. Use the source project's actual native, Capacitor, Flutter, or desktop build process. Run relevant checks and reuse established build/signature helpers. Do not infer signing secrets or switch to a debug key when credentials are missing.
3. Publish versioned artifacts and app documentation before activating the catalog. Confirm direct, anonymous HTTPS downloads with redirects; an authenticated `gh` lookup is not proof that the hub can download them. Compare the downloaded APK's hash and signature with the verified local artifact.
4. New apps need all four app documents under the **computed documentation slug** in the artifact repository's `main` branch. For existing releases, update relevant documents and verify the remaining ones are reachable; a narrow documentation edit needs only its requested files. Release tags, screenshot folders, package suffixes, and documentation slugs can differ. Nonblank inline documentation in the registry overrides remote files.
5. Patch only the requested entries in the full current catalog, preserving unrelated entries, fields, and untouched platform URLs. A partial catalog can remove other apps from users' cached listings. Never invent assets or overwrite an existing released binary to reuse its URL.
6. Review capability/provider declarations for the target app. A local `.rykersoft/app-capabilities.json` is not a Firestore deployment. Use the capability reference for required integration; preserve existing users and grants.
7. Push the reviewed catalog to `main` only after artifact, docs, and required backend checks pass. If remote changes race the update, fetch and reapply the focused change; do not force-push or overwrite another release.

## Verify and report

Fetch the public registry again and confirm exact package, versions, URLs, all expected entries, and target documents/images. Refresh the hub and test install/update when a suitable device is available; preserve user data. Distinguish local/static checks from actual device verification. A catalog rollback does not downgrade an already-installed Android app; fixes normally need a higher version code.

Report the app/version/code, public download/release links, source and catalog commits, relevant checks, and concrete remaining blockers. If an intermediate step fails, leave the existing catalog active and explain what was actually published. Do not mark a release complete on an upload alone.

## Maintaining this knowledge

The version-controlled source of this skill is `skills/rykersoft-deploy/` inside the RykerSoft repository. Its installed copy lives in `${CODEX_HOME:-$HOME/.codex}/skills/rykersoft-deploy/`. Update the source and installed copy together after verified workflow changes. Runtime source and current release metadata take precedence over older prose.
