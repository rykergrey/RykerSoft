# RykerSoft Firebase — UID entitlements and transitional provider keys

Active project: **`rykersoft-abe84`** (display name **RykerSoft**).

Google Auth is the standard account provider. Email/password remains enabled only while legacy accounts are linked or recovered. Pro access is authorized by the signed-in user's Firebase Auth UID and an app-specific Boolean in Firestore:

```text
users/{uid}/entitlements/apps
  com.rykersoft.superthinking: true
  com.rykersoft.bettertracking: true
```

The existing entitlement documents are already the canonical records. Do not delete, replace, or recreate them during this migration. The two users who already have pro access keep it as long as their existing UID and `true` package fields are preserved.

## 1. Enable products

1. Firebase Console → Authentication → Sign-in method → enable **Google** and set the approved public support email.
2. Keep **Email/Password** enabled only while legacy-account migration and recovery remain supported; do not expose password sign-up.
3. Register the App Manager release certificate SHA-1 and SHA-256 fingerprints for `com.rykersoft.appmanager`.
4. Create a Firestore database if one does not already exist, then deploy the rules below.

## 2. Deploy and verify rules

From this repository, after `firebase login`:

```powershell
npx -y firebase-tools@latest use rykersoft-abe84
npx -y firebase-tools@latest deploy --only firestore:rules --project rykersoft-abe84
```

The deployed rules must enforce all of the following:

- A signed-in user can read only their own `users/{uid}/entitlements/apps` document.
- Mobile and web clients cannot create, update, or delete entitlement records.
- Firebase Console, a trusted callable Function using the Admin SDK, or another trusted server environment performs grants and revocations.
- `unlockCodes` and legacy `unlockRequests` are not client-readable or client-writable.

Deploying rules does not alter existing Firestore documents. Before and after deployment, verify that both current pro users still have the same UID and package fields.

## 3. Android / hub environment

Copy `.env.example` to an ignored local `.env` and fill in the Web app configuration from Project settings → Your apps → Web app:

```text
FIREBASE_API_KEY=...
FIREBASE_APP_ID=...
FIREBASE_PROJECT_ID=rykersoft-abe84
FIREBASE_STORAGE_BUCKET=...
FIREBASE_MESSAGING_SENDER_ID=...
FIREBASE_AUTH_DOMAIN=...
FIREBASE_WEB_CLIENT_ID=...
```

Register an Android app with package `com.rykersoft.appmanager` if `google-services.json` is also needed. The hub currently initializes Firebase from the local BuildConfig values above.

Do not add a GitHub personal access token, family token, service-account JSON, Firebase Admin credential, or provider secret to an Android, web, or Windows client.

## 4. Admin user and application management

The signed-in Google account `heavensounds@gmail.com` is the only client recognized as the RykerSoft administrator. Its verified Firebase token may read the account directory, merge exact package grants, maintain app capability manifests, and create or rotate package-scoped provider values. No other client may perform those operations.

Each successful hub sign-in maintains `users/{uid}` with `email`, `displayName`, `createdAt`, and `updatedAt`. The admin page watches this directory and raises a local Android notification when a UID appears after the initial baseline. This covers accounts that have actually signed into the Application Manager; Firebase Auth users that have never completed a hub sign-in do not yet have a directory profile.

Deploy each Pro-capable app with `appCapabilities/{packageId}`:

```json
{
  "packageName": "com.rykersoft.example",
  "displayName": "Example",
  "proEnabled": true,
  "providerModel": "trusted-family",
  "credentialFields": [
    { "field": "gemini", "label": "Gemini API key", "provider": "gemini", "required": true }
  ]
}
```

Use `credentialFields: []` and `providerModel: "none"` when Pro features require no external API. The admin page uses this manifest to render grant toggles and masked credential-entry fields. It reports only whether a value exists; it does not prefill or display stored secrets.

Grant/revoke and provider-key entry should normally be performed in the Application Manager admin page. Firebase Console remains the recovery surface.

## 5. Grant or revoke pro access manually

Manual Firebase Console administration is the safest default for the current solo-maintainer workflow.

### Find the authoritative UID

1. Open Firebase Console → **Authentication** → **Users**.
2. Find the account. An email address may be used here as an administrator's search input.
3. Open the Firebase Auth user and copy its **UID**.
4. Confirm that this is the UID the user sees after signing in to the RykerSoft hub.

The UID is the authorization identity. Never authorize from an email field supplied by a client or from `users/{uid}.email`; profile fields can be stale or client-writable. If a future trusted backend accepts an email for convenience, it must resolve that email through Firebase Auth with the Admin SDK and then operate on the returned UID.

### Grant one application

1. Open Firebase Console → **Firestore Database** → **Data**.
2. Navigate to collection `users`, document `{uid}`, subcollection `entitlements`, document `apps`.
3. If the `apps` document exists, use **Add field** or edit only the exact package field. Do not replace the document.
4. If it does not exist, create document `apps` with the one exact package field needed.
5. Set the field name to the Android package ID, the type to **boolean**, and the value to `true`.
6. Save, have the user refresh/sign in again, and verify both the granted app and an ungranted app.

Common package IDs are:

| Application | Entitlement field |
|---|---|
| RykerSoft | `com.rykersoft.appmanager` |
| INFORMANT | `com.rykersoft.informant` |
| Rush | `com.rykersoft.rush` |
| Synthing | `com.rykersoft.synthing` |
| SuperThink.ing | `com.rykersoft.superthinking` |
| bettertracking | `com.rykersoft.bettertracking` |
| WordPlay.ing | `com.rykersoft.wordplaying` |
| Photocraft.ing | `com.rykersoft.photocrafting` |
| FreeBall.ing | `com.rykersoft.freeballing` |

Each field is independent. Adding or updating one field must preserve every other field in the document (Firestore merge semantics).

### Revoke one application

1. Return to `users/{uid}/entitlements/apps`.
2. Change only the exact package field to Boolean `false`.
3. Do not delete or replace the entitlement document.
4. Verify that the app returns to its free feature set while unrelated pro grants still work.

Free features and public APK access must work for signed-out, unentitled, revoked, and temporarily offline users. An entitlement controls only that package's pro capabilities.

## 6. Preserve and reconcile existing users

No bulk entitlement migration is required. For the two current pro users:

1. Record each Firebase Auth UID and inspect `users/{uid}/entitlements/apps` without changing it.
2. Confirm all existing `true` fields remain present.
3. Sign in with the same account after the new App Manager release and verify access in every granted app.

If one person accidentally has multiple Firebase Auth UIDs (for example, an unlinked legacy password account and a separate Google account):

1. Choose the canonical UID based on the account that owns the user's existing app data and sign-in identity.
2. Read every duplicate UID's entitlement document.
3. Union all package fields whose value is `true` into the canonical UID by adding only those fields. Preserve every field already on the canonical document.
4. Test sign-in, app data, each granted package, and at least one ungranted package with the canonical account.
5. Do not delete a duplicate Auth user or Firestore document unless a separate, backed-up cleanup has been explicitly approved.

Prefer linking Google credentials to the existing Firebase user so the UID is preserved instead of creating a replacement user.

## 7. Package-scoped provider keys

Existing app versions currently read provider keys from documents such as:

- Path: `providerKeys/{packageName}`
- Fields: `gemini` (string), `groq` (string, optional), and only the other providers that app needs

Known current documents include:

| Document ID | App |
|---|---|
| `com.rykersoft.superthinking` | SuperThink.ing |
| `com.rykersoft.bettertracking` | bettertracking |
| `com.rykersoft.informant` | INFORMANT |
| `com.rykersoft.photocrafting` | Photocraft.ing |

The admin page writes only nonblank replacement values declared by the app's capability manifest. Existing values are preserved when a field is left blank. Entitled clients can read only their exact package document.

On each future app deployment, move provider-backed operations into an authenticated callable Cloud Function or equivalent trusted backend. The backend verifies the caller's UID and package entitlement, uses secrets stored only server-side, and returns the operation result rather than the provider key. After all supported versions of every consuming app have migrated and been verified, remove client access to `providerKeys` and retire the documents.

## 8. Optional one-time invitations (future, not implemented)

Manual UID grants are sufficient today. If invitation convenience becomes worthwhile, implement it only as a trusted backend feature; do not restore reusable family codes or rules-only client redemption.

A secure design uses:

- At least 128 bits of random code entropy; store only `sha256(code)` as the invitation document ID.
- `invitations/{codeHash}` fields such as `status`, explicit `packages`, `expiresAt`, `maxUses: 1`, `createdBy`, and optional `intendedUid`.
- An authenticated callable Function with App Check enforcement.
- One Admin SDK transaction that validates status, expiry, intended UID, and usage; merges only the explicit package fields into `users/{uid}/entitlements/apps`; then records `consumedBy` and `consumedAt`.
- An immutable audit record for creation, revocation, successful redemption, and rejected administrative operations.

If an administrator enters an email when creating an invitation, the callable backend must resolve it through Firebase Auth and bind the invitation to the resulting UID. Never trust a client-provided email as entitlement proof. Invitations must not grant wildcard access to apps added in the future.

## 9. Future Windows/.NET administration app

A future Microsoft/.NET user-management application should be a thin authenticated client:

```text
Windows admin UI → authenticated callable Functions → Firebase Admin SDK → Auth / Firestore
```

- Bootstrap the maintainer's UID as an administrator with a server-set custom claim or a locked-down server-only `admins/{uid}` record.
- Have callable Functions verify authentication, App Check where supported, and the administrator role on every request.
- Expose narrow operations such as resolving an Auth user, reading current grants, setting one package Boolean, granting an explicit list of current packages, creating/revoking an invitation, and reading audit events.
- Preserve merge semantics and write an audit event with actor UID, target UID, package, previous value, new value, and timestamp.
- Never bundle a Firebase service-account file, Admin SDK private key, GitHub token, or provider key in the Windows executable.

## 10. Public, tokenless application distribution

Free binaries and documentation consumed by App Manager must be reachable over anonymous HTTPS. Source repositories may remain private, but the release assets referenced by `registry.json` must not require a GitHub token in the client.

Use this migration order:

1. Publish only audited current documentation and artifacts to a clean public distribution repository or public release location; do not expose an old private repository's unreviewed history.
2. Verify every registry, document, screenshot, APK, and Windows metadata URL in a signed-out browser or unauthenticated request.
3. Release the tokenless App Manager while the old token remains valid so existing installations still have a recovery path.
4. Update and verify every known App Manager installation/profile, including both current pro users.
5. Confirm anonymous update/install, sign-in, entitlement reads, and free-mode behavior.
6. Revoke the legacy GitHub token and remove every ignored local copy and CI secret.

Never revoke the legacy token before the tokenless release is publicly accessible and installed on the known devices.
