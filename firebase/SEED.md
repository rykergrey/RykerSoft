# RykerSoft Firebase — seed unlock codes & provider keys

Active project: **rykersoft-abe84** (display name RykerSoft).

Auth (email/password) and Firestore rules are already deployed via Firebase CLI.

## 1. Enable products

1. Firebase Console → Authentication → Sign-in method → enable **Email/Password**
2. Create a Firestore database (production mode is fine; deploy rules next)

## 2. Deploy rules

From this repo (after `firebase login` and `firebase use <project-id>`):

```bash
npx -y firebase-tools@latest deploy --only firestore:rules
```

## 3. Android / hub env

Copy `.env.example` → `.env` and fill Web app config from Project settings → Your apps → Web app:

```
FIREBASE_API_KEY=...
FIREBASE_APP_ID=...
FIREBASE_PROJECT_ID=...
FIREBASE_STORAGE_BUCKET=...
FIREBASE_MESSAGING_SENDER_ID=...
FIREBASE_AUTH_DOMAIN=...
```

Register an Android app with package `com.rykersoft.appmanager` if you also want `google-services.json` (optional; hub initializes Firebase from these BuildConfig values).

## 4. Seed provider keys

Per-app documents (already seedable via CLI):

- Path: `providerKeys/{packageName}`
- Fields: `gemini` (string), `groq` (string, optional)

| Document ID | App |
|-------------|-----|
| `com.rykersoft.superthinking` | SuperThink.ing |
| `com.rykersoft.bettertracking` | bettertracking |
| `com.rykersoft.informant` | INFORMANT |
| `com.rykersoft.photocrafting` | Photocraft.ing |

INFORMANT free features (transcript / Webshare / YouTube Data for metadata+comments) stay baked in the app — not in hub unlock.

## 5. Seed unlock codes

Generate a SHA-256 hex of your family code (trimmed, lowercase). PowerShell:

```powershell
$code = "your-family-code".Trim().ToLowerInvariant()
$bytes = [System.Text.Encoding]::UTF8.GetBytes($code)
$hash = [System.BitConverter]::ToString([System.Security.Cryptography.SHA256]::Create().ComputeHash($bytes)).Replace("-", "").ToLowerInvariant()
Write-Host $hash
```

Create document:

- Path: `unlockCodes/{hash}`  (collection `unlockCodes`, document ID = hash)
- Field `packages` (array of strings), for example:

```
com.rykersoft.superthinking
com.rykersoft.bettertracking
com.rykersoft.informant
com.rykersoft.photocrafting
```

Use one code for all AI apps, or separate codes with different `packages` arrays.

## 6. Manual entitlements (optional)

Path: `users/{uid}/entitlements/apps`

```
com.rykersoft.superthinking: true
com.rykersoft.bettertracking: true
com.rykersoft.informant: false
```

## 7. Web apps (BetterTracking / SuperThinking / INFORMANT)

Set the same Firebase web config as `VITE_RYKERSOFT_FIREBASE_*` (see each app’s `.env.example`).
