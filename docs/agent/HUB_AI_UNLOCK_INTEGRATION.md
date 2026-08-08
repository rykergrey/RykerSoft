---
name: rykersoft-hub-ai-unlock
description: >-
  Integrate a RykerSoft Android/Capacitor/web app with the RykerSoft Application
  Manager pro unlock system (hub Firebase Auth, entitlements, remote provider
  keys). Use when adding a new app to the hub, wiring pro/AI unlock, providerKeys,
  dual Firebase Auth, PRO Features docs with colored asterisks, or when the user
  mentions RykerSoft unlock / pro features access.
---

# RykerSoft Hub Pro Unlock Integration

## System overview (do not reinvent)

| Concern | Where it lives |
|---------|----------------|
| App install / updates | RykerSoft App Manager + `registry.json` |
| Family unlock code entry | **App Manager only** |
| Entitlements + provider keys | Hub Firebase project **`rykersoft-abe84`** |
| App-specific user data (vault, journal, etc.) | That app’s **own** Firebase project (if any) |
| Provider keys at runtime | Fetched from hub Firestore after entitlement + hub sign-in |

**Unlock code is never typed inside the target app.** Users unlock in App Manager, then sign into the **same RykerSoft hub account** inside the app (Settings → RykerSoft pro unlock) so keys can sync.

Prefer user-facing language **“pro features”** over heavy “AI” branding (badges, buttons, settings, hub docs).

Hub project ID: `rykersoft-abe84`  
Reference implementation: `RykerSoft/` (hub), `BetterTrackingV2/BetterTrackingV2/services/rykersoftHub.ts`, `superthinkingCursor/services/rykersoftHub.ts`, `supertube/src/lib/rykersoftHub.ts`.

## User journey (explain this if asked)

1. User signs in with Google to the RykerSoft account in **App Manager** (hub Auth).
2. User opens app detail → **UNLOCK PRO FEATURES** → enters family unlock code.
3. App Manager submits an atomic unlock request plus entitlement update. Firestore rules validate its code hash against the server-only unlock document and permit only the listed package; clients cannot grant unrelated access.
4. User installs/opens the app.
5. If the app has its **own** Firebase login (e.g. SuperThinking vault), that login is **separate** and unchanged.
6. User also signs into **RykerSoft hub** inside the app (second account UI) → app reads `providerKeys/{packageName}` and enables pro features.

Apps with **no** local auth (e.g. INFORMANT) only need the hub sign-in step inside the app.

## PRO Features docs (MANDATORY on every deploy of an AI-capable app)

Any app that uses AI functions or other unlock-gated capabilities MUST ship hub docs that:

1. Include a dedicated **`## PRO Features`** section in **both** `docs/description.md` and `docs/user_guide.md` (exact heading; list it in the user-guide TOC).
2. Mark **every** pro / unlock-gated feature and function with a **colored asterisk** list marker so App Manager renders a magenta `*`:
   - Prefer `* Feature name — …`
   - Or `- * Feature name — …`
3. Keep free / non-pro features on normal `-` bullets (hub shows `•`).
4. Briefly note that `*`-marked items need a RykerSoft pro unlock in App Manager, then sign-in to the same account inside the app. Do **not** add long “features stay off until…” filler copy in the hub UI or docs.

Example (`description.md`):

```markdown
## Key Features
- **Offline journal**: Works without an account.
- **Reminders**: Local notifications for habits.

## PRO Features
Items marked * require a RykerSoft pro unlock (App Manager), then sign-in to the same RykerSoft account inside this app.

* **Quick Logging** — Photo or natural-language meal capture
* **Coaching insights** — Personalized tips from your journal
```

Same asterisk convention applies inside relevant user-guide sections (not only the PRO Features heading).

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

3. UI: Settings section **“RykerSoft pro unlock”** with Google sign-in / sign-out / refresh keys. Existing password accounts use a migration-only sign-in followed by Google credential linking that preserves their Firebase UID; do not expose password sign-up.
4. On hub auth state change: `syncRemoteKeysToLocalStorage()` (or merge into that app’s preferences model).
5. Gate pro/AI calls: if no Gemini (or required) key after sync, show a clear “unlock in App Manager + sign in here” message. Core non-pro features must keep working.
6. **Do not bake costly provider keys** into release builds. Optional local-only escape hatch: `VITE_ALLOW_BAKED_AI_KEYS=true` for developer machines only.
7. Free non-pro secrets (e.g. INFORMANT transcript/Webshare/YouTube Data for metadata) may remain baked if the product owner says they are free-tier.
8. Write/update **`## PRO Features`** docs with `*` markers (see above) before every hub deploy.

### D. Native Android-only apps (Kotlin)

1. Initialize a named FirebaseApp `rykersoft-hub` with hub options (same pattern as App Manager `RykerSoftFirebase.kt` / `EntitlementRepository.kt`).
2. Do **not** replace the app’s primary FirebaseApp if it already has one.
3. Add hub sign-in UI + entitlement/key fetch equivalent to the web module.

### E. App Manager changes (only when adding unlockable pro features)

1. Add package id to `AiUnlockPackages` (or shared constant list) in the hub repo.
2. Bump hub docs (`docs/user_guide.md` pro unlock section) if user-facing flow changes.
3. No second APK / “pro flavor” — unlock is entitlement + remote keys.

### F. Deploy / verify (RykerSoft-APKs distribution model)

APKs and hub docs distribute from the **private `rykergrey/RykerSoft-APKs` repo** (one shared fine-grained PAT for family/friends, scoped to that repo only). App source repos stay private and are NOT referenced by `registry.json`.

1. Rebuild the signed release APK in the app repo; commit/tag/release in the app repo as usual (source of truth).
2. Also publish the APK to the distribution repo:
   `gh release create <slug>-v<version> app-release.apk --repo rykergrey/RykerSoft-APKs --title "<AppName> v<version>"`
3. Copy the app repo's `docs/*.md` into `RykerSoft-APKs/docs/<slug>/`, commit, push (hub fetches docs from there). Confirm `## PRO Features` + `*` markers are present for AI-capable apps.
4. Point `registry.json` `apkUrl` at:
   `https://github.com/rykergrey/RykerSoft-APKs/releases/download/<slug>-v<version>/app-release.apk`
   (Exception: the App Manager's own APK stays on the public `rykergrey/RykerSoft` repo so self-update needs no token.)
5. Verify: locked → pro features disabled with clear message; unlock in manager → hub sign-in in app → pro works; other apps’ keys not readable.

## Dual Firebase Auth — intentional

- **App Firebase**: user content sync for that product.
- **Hub Firebase**: cross-app entitlements + provider keys for the family/friends circle.

Do not merge these projects. Do not require the unlock code inside the target app.

## Out of scope unless explicitly requested

- Play Billing / purchases (same entitlement fields later)
- Deep-link / custom-token SSO to avoid the second hub login (nice future UX)
- Making app source repos public
- Putting keyed APKs on the public RykerSoft repo
