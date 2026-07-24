---
name: rykersoft-hub-ai-unlock
description: >-
  Integrate a RykerSoft Android/Capacitor/web app with the RykerSoft Application
  Manager AI unlock system (hub Firebase Auth, entitlements, remote provider
  keys). Use when adding a new app to the hub, wiring AI unlock, providerKeys,
  dual Firebase Auth, or when the user mentions RykerSoft unlock / pro AI access.
---

# RykerSoft Hub AI Unlock Integration

## System overview (do not reinvent)

| Concern | Where it lives |
|---------|----------------|
| App install / updates | RykerSoft App Manager + `registry.json` |
| Family unlock code entry | **App Manager only** |
| Entitlements + AI provider keys | Hub Firebase project **`rykersoft-abe84`** |
| App-specific user data (vault, journal, etc.) | That app’s **own** Firebase project (if any) |
| AI keys at runtime | Fetched from hub Firestore after entitlement + hub sign-in |

**Unlock code is never typed inside the target app.** Users unlock in App Manager, then sign into the **same RykerSoft hub account** inside the app (Settings → RykerSoft AI unlock) so keys can sync.

Hub project ID: `rykersoft-abe84`  
Reference implementation: `RykerSoft/` (hub), `BetterTrackingV2/BetterTrackingV2/services/rykersoftHub.ts`, `superthinkingCursor/services/rykersoftHub.ts`, `supertube/src/lib/rykersoftHub.ts`.

## User journey (explain this if asked)

1. User creates/signs in to RykerSoft account in **App Manager** (hub Auth).
2. User opens app detail → **UNLOCK AI FEATURES** → enters family unlock code.
3. Hub writes `users/{uid}/entitlements/apps` → `{ "<packageName>": true }`.
4. User installs/opens the app.
5. If the app has its **own** Firebase login (e.g. SuperThinking vault), that login is **separate** and unchanged.
6. User also signs into **RykerSoft hub** inside the app (second account UI) → app reads `providerKeys/{packageName}` and enables AI.

Apps with **no** local auth (e.g. INFORMANT) only need the hub sign-in step inside the app.

## Checklist for each new AI-capable app

### A. Identity

1. Confirm Android `applicationId` / Capacitor `appId` is `com.rykersoft.<shortname>` (no hyphens).
2. Register the app in hub `registry.json` (lightweight fields + public screenshot URLs on `rykergrey/RykerSoft`).

### B. Hub Firebase data (project `rykersoft-abe84`)

1. Ensure unlock code doc grants this package (array field `packages` on `unlockCodes/{sha256}`), **or** document that admin will set entitlements manually.
2. Create `providerKeys/{packageName}` with at least:
   - `gemini` (string)
   - `groq` (string, optional)
   - other provider fields only if that app needs them from the hub
3. Deploy/update Firestore rules if adding new collections (existing rules already allow `providerKeys/{packageId}` read when that package is unlocked).

### C. App code (Capacitor / Vite / React — preferred pattern)

1. Copy/adapt `services/rykersoftHub.ts` (or `src/lib/rykersoftHub.ts`):
   - Second Firebase app name: `rykersoft-hub`
   - `RYKERSOFT_PACKAGE_ID` = this app’s package id
   - Read keys from `providerKeys/{packageId}` (not legacy `config/providerKeys`)
2. Add env vars (never commit secrets; commit `.env.example` only):

```env
VITE_RYKERSOFT_FIREBASE_API_KEY=
VITE_RYKERSOFT_FIREBASE_AUTH_DOMAIN=rykersoft-abe84.firebaseapp.com
VITE_RYKERSOFT_FIREBASE_PROJECT_ID=rykersoft-abe84
VITE_RYKERSOFT_FIREBASE_STORAGE_BUCKET=rykersoft-abe84.firebasestorage.app
VITE_RYKERSOFT_FIREBASE_MESSAGING_SENDER_ID=809926103320
VITE_RYKERSOFT_FIREBASE_APP_ID=
```

   Use the **hub web app** config from Firebase Console (same values as RykerSoft App Manager `.env` `FIREBASE_*` web fields).

3. UI: Settings section **“RykerSoft AI unlock”** with sign-in / create / sign-out / refresh keys (mirror BetterTracking Profile → API Keys or SuperThinking Settings).
4. On hub auth state change: `syncRemoteKeysToLocalStorage()` (or merge into that app’s preferences model).
5. Gate AI calls: if no Gemini (or required) key after sync, show a clear “unlock in App Manager + sign in here” message. Non-AI features must keep working.
6. **Do not bake costly AI keys** into release builds. Optional local-only escape hatch: `VITE_ALLOW_BAKED_AI_KEYS=true` for developer machines only.
7. Free non-AI secrets (e.g. INFORMANT transcript/Webshare/YouTube Data for metadata) may remain baked if the product owner says they are free-tier.

### D. Native Android-only apps (Kotlin)

1. Initialize a named FirebaseApp `rykersoft-hub` with hub options (same pattern as App Manager `RykerSoftFirebase.kt` / `EntitlementRepository.kt`).
2. Do **not** replace the app’s primary FirebaseApp if it already has one.
3. Add hub sign-in UI + entitlement/key fetch equivalent to the web module.

### E. App Manager changes (only when adding unlockable AI)

1. Add package id to `AiUnlockPackages` (or shared constant list) in the hub repo.
2. Bump hub docs (`docs/user_guide.md` AI unlock section) if user-facing flow changes.
3. No second APK / “pro flavor” — unlock is entitlement + remote keys.

### F. Deploy / verify

1. Rebuild release APK; register in hub if version bumped.
2. Verify: locked → AI errors/disabled; unlock in manager → hub sign-in in app → AI works; other apps’ keys not readable.

## Dual Firebase Auth — intentional

- **App Firebase**: user content sync for that product.
- **Hub Firebase**: cross-app entitlements + AI keys for the family/friends circle.

Do not merge these projects. Do not require the unlock code inside the target app.

## Out of scope unless explicitly requested

- Play Billing / purchases (same entitlement fields later)
- Deep-link / custom-token SSO to avoid the second hub login (nice future UX)
- Making app source repos public
- Putting keyed APKs on the public RykerSoft repo
