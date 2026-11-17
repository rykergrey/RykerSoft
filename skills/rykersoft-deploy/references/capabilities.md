# Account, Pro, and capability integration

Verified against hub commit `1f0e33d2ff3571a951cf8a5a9a81fc0b1f992d62` on 2026-09-30. This describes checked-in code/rules; live Firebase state and deployed rules were not inspected. Recheck relevant implementation before changing integration.

## What an app release needs

Review provider use and the application's `.rykersoft/app-capabilities.json` when present. The existing integration guide asks that `appCapabilities/{exactPackageId}` be published before catalog activation, including an empty provider declaration for apps without provider APIs. For an existing app, verify that the current declaration still describes the release; do not rewrite it merely to release another binary.

A local manifest is an authoring record, not an automatic Firebase deployment. The hub reads Firestore directly; it does not import local manifests or `metadata.json`. New Pro apps can be discovered dynamically from a deployed `proEnabled:true` capability. Adding the app to the hardcoded `AiUnlockPackages` fallback or rebuilding the hub is generally unnecessary.

The current client rules allow only these capability fields (plus an optional string `providerKeysPackage` naming a shared `providerKeys` document, used by Hyperscribe Desktop):

```json
{
  "packageName": "com.example.actualapp",
  "displayName": "Example",
  "proEnabled": false,
  "providerModel": "none",
  "credentialFields": []
}
```

`updatedAt` is also allowed. Project the runtime fields from a local manifest; do not upload its `schemaVersion`, `backendRequirements`, `firebase`, or `evidence` fields through the client API. Declare credential names exactly as the consuming app reads them; capitalization matters. For Pro without external providers, `proEnabled:true`, `providerModel:"none"`, and an empty credential list are valid. Current trusted-family provider apps use `providerModel:"trusted-family"`; proxy/backend apps need declarations consistent with their actual implementation.

No generic capability publisher is supplied by the hub repository. Discover the target app's existing registration tool or use an authorized Firebase administrative surface. Inspect any script before running it; publication should touch only the target capability. If backend access is unavailable, prepare the exact document and identify that blocker before catalog activation; do not claim registration from a local file.

Ordinary releases do not imply changing user grants, migrating accounts, rotating provider keys, deploying hub Firestore rules, or introducing a new provider backend. Existing integration and the user's requested scope determine what is necessary.

## Identity and access

- Hub Firebase project: `rykersoft-abe84`. Products with their own Firebase account/data should keep the hub identity in a named secondary Firebase app rather than replace their product project.
- Authentication uses Firebase Auth UIDs. Preserve the UID when linking legacy users to Google; do not substitute a new account or use a client profile email as entitlement proof.
- Pro grants are exact package Boolean fields at `users/{uid}/entitlements/apps`. Preserve all unrelated fields and existing users; write merges for a targeted grant/revoke. Dots in package IDs must remain literal field names, not nested paths.
- Ordinary owners read their own grants and cannot self-grant. Current rules also allow the verified administrator to read/create/update grants; they deny deletion. The hub's grant UI requires the target `users/{uid}` profile to exist.
- Installation, updates, documentation, and the app's free features remain usable without Pro. Test relevant signed-out, unentitled, entitled, revoked, offline, and backend-error states when changing this integration.
- Do not create a separate Pro APK, embed reusable family unlock codes, or bundle provider secrets, service-account credentials, GitHub tokens, or Admin SDK keys in a release.

The current rules identify the administrator from the authenticated token's exact email `heavensounds@gmail.com`, `email_verified == true`, and a Google sign-in session. This is a server-checked token condition, not permission inferred from editable profile data. Normal administration uses the hub admin page, with Firebase Console as a recovery surface. A deployment agent still needs an actually authorized administrative session; knowing the email supplies no credentials.

## Provider details and current caveats

Current trusted-family apps read `providerKeys/{packageId}` only with the relevant entitlement; admin provider writes also require a matching capability with Pro enabled. Backend-proxied operations should verify UID and package entitlement on the server. Preserve an established delivery mechanism until a scoped migration is requested and compatible clients are ready.

Provider writes from the hub use manifest-declared field names, trim values, omit blank values, and merge changes. Blank entries preserve stored keys. This restriction is implemented in the hub, not fully enforced by the current Firestore key schema.

The hub performs special capability seeding while opening the admin app list:

- INFORMANT merges known fields if its manifest is absent or lacks `openai`. It uses `elevenLabs` (capital L), unlike Hyperscribe's `elevenlabs`.
- Hyperscribe Mobile and Desktop are seeded with `gemini`, `openai`, `groq`, and `elevenlabs`; an update can replace their credential-field arrays. Check this before changing those manifests.
- Hyperscribe Desktop admin key operations map to `providerKeys/com.rykersoft.hyperscribemobile`; the two editions keep separate entitlement fields. This mapping is code, not generic behavior driven by arbitrary capability metadata.

The root `firestore.rules` is the deployed ruleset (resynced 2026-11-17: Google sign-in required for admin and provider-key reads, Meme God any-Pro exception, optional string `providerKeysPackage` allowed on capabilities). Still compare it with live rules before any rules deployment.

One checked-in inconsistency relevant to Hyperscribe remains, requiring verification of actual deployed behavior before a related release:

1. Provider-key read rules require entitlement for the document's exact package. A Desktop-only grant does not authorize a read of the shared Mobile key document under those rules.

Do not assume that pushing the current rules fixes this, or silently expand an unrelated deployment into a backend repair. Report a related integration blocker if encountered. The existing unit tests for alias mapping do not establish that deployed shared-key authorization works.

## App documentation

For unlock-gated apps, the existing integration guide requires a dedicated `## PRO Features` section in both description and user guide, with a guide table-of-contents link. Use `*` list markers for Pro features and ordinary `-` bullets for free features. Explain that access is managed for the user's RykerSoft account and requires the same identity in the app when relevant. Do not present the provider key, email, or APK variant as the entitlement.

## Where to verify changes

Paths relative to the hub repository:

- `app/src/main/java/com/rykersoft/appmanager/entitlements/EntitlementRepository.kt`: dynamic catalog, admin operations, seeders, Hyperscribe alias.
- `app/src/main/java/com/rykersoft/appmanager/entitlements/AiUnlockPackages.kt`: fallback package eligibility and known provider fields.
- `firestore.rules`: actual checked-in administrator/access/schema conditions.
- `firestore-tests/firestore.rules.test.js`: emulator access tests.
- `docs/agent/HUB_AI_UNLOCK_INTEGRATION.md` and `firebase/SEED.md`: fuller integration intent and historical migration guidance.

Older prose says clients cannot write grants and that only Firebase Console administers them; current code/rules allow the verified admin client. Historical claims about two Pro users, legacy token status, and migration completion are not a current inventory. Some `.rykersoft/app-capabilities.json` evidence paths are also stale. Use actual code plus verified deployed state when resolving these conflicts.
