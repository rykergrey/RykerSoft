---
name: rykersoft-hub-ai-unlock
description: >-
  Integrate a RykerSoft Android, Capacitor, web, or Windows-capable app with the
  RykerSoft Firebase Auth and per-application UID entitlement system. Covers
  Google sign-in, legacy UID preservation, admin-managed pro access,
  transitional provider keys, callable backends, and free-mode verification.
---

# RykerSoft Hub Pro Access Integration

## Canonical architecture

| Concern | Canonical location |
|---|---|
| Install and updates | RykerSoft App Manager + `registry.json` + anonymous public artifact URLs |
| Authentication | Firebase Auth in hub project `rykersoft-abe84` |
| Per-app pro access | `users/{hubUid}/entitlements/apps` Boolean package fields |
| Administrative writes | Firebase Console today; authenticated callable Functions/Admin SDK later |
| App-specific user data | The app's own Firebase project, when applicable |
| Provider secrets | Transitional `providerKeys/{packageId}` for existing apps; trusted backend end state |

The Firebase Auth UID is authoritative. Email is allowed only as a trusted administrator lookup input that a backend resolves through Firebase Auth. Never authorize from a client-provided email or profile field.

Do not create a second APK or "pro flavor." The same app supplies free features to everyone and enables only its own pro capabilities when the signed-in UID has an exact package field set to `true`.

## User journey

1. Anyone can read app information and install the public APK without a GitHub or family token.
2. The user signs in to their RykerSoft account with Google. A legacy password account is linked to Google in a UID-preserving migration instead of creating a replacement user.
3. App Manager reads the user's own `users/{uid}/entitlements/apps` document. Clients cannot write it.
4. If that package is `true`, its pro UI is available. Otherwise its complete free feature set remains operational.
5. A target app that needs hub authorization signs in to the same hub account and verifies the same package entitlement.
6. Apps with their own product login keep it separate from the hub identity.

There is no reusable family unlock code. Existing entitlement records remain canonical and must not be deleted or replaced.

## Entitlement contract

The entitlement document is:

```text
users/{hubUid}/entitlements/apps
  com.rykersoft.superthinking: true
  com.rykersoft.bettertracking: false
```

Requirements:

- A package ID is the exact field name and its Boolean value is independent of every other app.
- `true` grants that package; `false` is revoked/free mode.
- Client rules allow the owner to read their document and deny client create, update, and delete.
- Administrative updates merge only the intended package field. Never replace or delete the whole document.
- Preserve the two existing pro users' UIDs and `true` fields. Rules deployment does not require a data migration.
- When duplicate UIDs exist, choose the UID that owns the user's existing data, union every `true` package field into it, and test identity, data, granted apps, and an ungranted app before any separately approved cleanup.

The exact manual Firebase Console procedure is maintained in [`firebase/SEED.md`](../../firebase/SEED.md).

## PRO Features documentation

Every app with unlock-gated capabilities must ship hub documentation that:

1. Includes a dedicated `## PRO Features` section in both `docs/description.md` and `docs/user_guide.md` and lists it in the user-guide table of contents.
2. Marks every pro feature with an asterisk list marker so App Manager renders the pro marker consistently.
3. Leaves free features on normal bullets.
4. Explains briefly that pro access is managed for the user's RykerSoft account and requires sign-in to that same account in the app when applicable.
5. Does not imply that an email, APK variant, invitation code, or provider key itself is the entitlement.

Example:

```markdown
## Key Features
- **Offline journal** — Works without an account.
- **Reminders** — Local notifications for habits.

## PRO Features
Items marked * require app-specific RykerSoft pro access for your signed-in account.

* **Quick Logging** — Photo or natural-language meal capture
* **Coaching insights** — Personalized tips from your journal
```

## Checklist for each app deployment

### A. Identity and existing-user safety

1. Confirm the exact Android `applicationId` / Capacitor `appId` (`com.rykersoft.<shortname>`).
2. Use Google sign-in for the hub identity.
3. Preserve an existing Firebase UID when linking a legacy password user to Google. Do not create a second account and silently abandon its entitlements or app data.
4. Keep app-specific Firebase Auth separate where the product already uses it for user content.
5. Never use email as authorization data. A trusted backend may resolve an administrator-entered email through Firebase Auth to obtain the UID.

### B. Entitlements and rules

1. Keep the schema `users/{uid}/entitlements/apps` with exact package Boolean fields.
2. Deny all client writes to the entitlement document; owners may read only their own document.
3. Grant or revoke by merging one exact field through Firebase Console or a trusted Admin SDK backend.
4. Do not delete or rewrite existing records during deployment.
5. Test signed-out, unentitled, entitled, revoked, offline, and backend-error states. Core free behavior must work in every non-entitled state.
6. Test that an entitlement for one package cannot read or activate another package's protected capability.

### C. Provider-backed features

Current released apps may still read an entitlement-scoped document at `providerKeys/{packageId}`. Keep this route working until all supported versions of that app migrate; do not remove it globally during an unrelated deployment.

For every future app deployment:

1. Prefer an authenticated callable Function or equivalent trusted backend for provider-backed operations.
2. Verify the caller's UID and exact package entitlement on the server.
3. Keep provider credentials in server-side secret storage and return only the operation result, never the key.
4. Add App Check and server-side abuse controls appropriate to the platform and sideloading model.
5. Once all supported versions of every consuming app have migrated and been verified, remove client reads of `providerKeys` and retire those documents.

Never bake costly provider keys, service-account credentials, GitHub tokens, or Admin SDK credentials into release builds.

### D. App integration

For apps that need hub access:

1. Initialize a named secondary Firebase app such as `rykersoft-hub`; do not replace the product's primary Firebase app.
2. Configure it with the hub project's public Firebase client values from an ignored local environment file. Commit only placeholders.
3. Add Google sign-in/sign-out and entitlement refresh UI using user-facing language such as **RykerSoft account** and **pro access**.
4. Read only the signed-in user's entitlement and request only that app's package capability.
5. On auth change, refresh the entitlement and clear cached pro state on sign-out, revoke, or error.
6. Keep the app's free workflows usable without hub auth.

### E. App Manager behavior

1. App Manager may display access status and administration guidance, but it does not accept a reusable code or write entitlement documents.
2. Installation and updates must remain independent of pro access.
3. Free app metadata, documentation, screenshots, and APKs must be reachable anonymously.
4. Do not add a GitHub token setting, authorization header, or private distribution credential to the client.

### F. Release and distribution

Source repositories may remain private. Everything referenced by the public hub registry must be tokenless and anonymously downloadable.

1. Build, sign, and verify the release in the source repository using local signing credentials that never enter the app or a public artifact.
2. Publish only reviewed current binaries and documentation to a clean public distribution repository or public release location. Do not make an old unreviewed private binary history public as a shortcut.
3. Point `registry.json` at anonymous HTTPS URLs and verify them without a signed-in GitHub session or authorization header.
4. Verify package ID, version, signing certificate, hashes, and absence of embedded tokens before publishing.
5. During the legacy token migration: publish the public artifacts first, release the tokenless manager while the old token still works, update every known installation/profile, verify anonymous operation, and only then revoke and delete the old token.

## Optional one-time invitations (not currently implemented)

Manual UID grants are the current administration method. If invitations later provide enough convenience to justify backend work, they must be single-use, high-entropy, and server-redeemed:

- Store only a hash of at least 128 random bits.
- Bind the invitation to explicit current package IDs and optionally an intended UID; never grant a wildcard for future apps.
- Require authenticated callable Functions, App Check, expiry, revocation status, and `maxUses: 1`.
- Consume and merge entitlement fields in one Admin SDK transaction, recording `consumedBy` and `consumedAt`.
- Resolve any administrator-entered email through Firebase Auth on the server before binding it to a UID.
- Audit creation, revocation, redemption, and rejected administrative attempts.

Do not implement invitation redemption with client Firestore writes or security rules alone.

## Future Windows/.NET admin application

The supported design is a thin authenticated UI calling narrow server endpoints:

```text
Windows/.NET admin UI → callable Functions → Firebase Admin SDK → Auth / Firestore
```

Bootstrap the maintainer's UID with a server-set administrator claim or locked server-only admin record. Each Function verifies the caller's administrator role and performs one narrowly scoped operation, such as resolving a Firebase Auth user, reading grants, setting one package Boolean, granting an explicit list, or creating/revoking an invitation. Every write uses merge semantics and produces an audit event.

Never ship a service-account JSON file, Admin SDK private key, GitHub token, or provider secret in the Windows executable.

## Out of scope unless explicitly requested

- Play Billing or purchases (the same entitlement fields can support them later)
- Automatic bulk deletion or merging of Auth users
- Making app source repositories public
- Re-enabling reusable family codes
- Removing transitional provider-key access before all consuming apps migrate
