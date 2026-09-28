# Technical Specifications

## Platform & Requirements
- **Android**: Android 7.0+ (API Level 24+)
- **Windows**: 64-bit Windows desktop executable built with Electron 40
- **Web**: Modern Chromium-, Firefox-, or WebKit-based browser
- **compileSdk / targetSdk**: 36
- **Package ID**: `com.rykersoft.wordplaying`
- **Release version**: 1.3.49 (Android version code 55)
- **Shell**: Capacitor 8 (WebView) wrapping a Vite + React 19 TypeScript app


## Architecture
- **UI**: React components for board, overlays, modals, multiplayer lobby
- **Game engine**: Client-side PRNG boards, adjacency rules, TWL06 DAWG dictionary
- **Backend**: Firebase Google Authentication + Firestore; trusted callable functions own Live Match v2 and public competitive attempts, boards, deadlines, validation, scores, challenges, seasons, and expiry. Firestore rules separately isolate public player-name reservations and each account's private progression data.
- **Audio**: Web Audio API synth / step sequencer
- **Definitions**: Free Dictionary API (`api.dictionaryapi.dev`)
- **Optional desktop**: Electron + electron-builder
- **Release artifacts**: Signed Android APK, portable Windows executable, and Linux AppImage from the same versioned source
- **Android updates**: The app compares its installed version code with the RykerSoft registry on launch, shows a persistent notice in Notifications when a newer release exists, and verifies the downloaded APK before asking Android to install it.

## Network
- Maintained remote traffic is HTTPS-only. Live Match uses Firebase rather than a VPS or Colyseus endpoint, and Android does not require cleartext or mixed-content exceptions.
- Cloud features require secure network access to Firebase; definitions use the HTTPS Free Dictionary API.
- The packaged Electron app's HTTP origin is loopback-only and serves bundled local UI; all maintained remote requests remain encrypted.
- Trusted competitive callables reserve Daily, challenge, and eligible solo-draft boards before reveal; own canonical configuration, seed, board, start/deadline, dictionary/path validation or final Word Grid arrangement, scoring, summaries, expiry, and immutable result writes. Firestore rules deny all direct V2 mutations and all reads of protected boards and attempts.
- V2 competition uses isolated collections (`competitiveAttemptsV2`, `leaderboardV2`, `challengesV2`, `challengeBoardsV2`, `challengeSeriesV2`, `competitiveUsersV2`, `careerModeStatsV2`, `playedMatchesV2`, and `weekWinnersV2`). Legacy v1.2.1 collections remain a migration-only compatibility surface and are excluded from v1.3 ranked queries and weekly finalization.
- Word Builder online matches, verified results, rankings, ruleset statistics, and weekly totals live under `games/word-builder`. Server transactions validate turns and save each finished match once. These records do not enter Word Hunt's competitive collections or career totals.
- Signed-in players may read public career fields on `competitiveUsersV2` and `careerModeStatsV2` (chosen player name plus lifetime/mode stats only). Clients cannot write those collections. `playedMatchesV2` word lists are readable only by the submitting player or another signed-in player who has also submitted that same match.
- Live Match v2 requires a registered public profile. Callable functions own mutations, validate canonical boards and word claims against server deadlines, resolve First Claim atomically, publish results, and maintain presence and expiry.
- Private progression documents live under each registered user and are readable/writable only by that account; per-match progression receipts are create-only. These personal collection/cosmetic records are not used as trusted public rankings or competitive aggregates.
- Public player profiles contain only a custom player name. Rules enforce the display-name-to-case-folded-reservation mapping, reject reserved and abusive names, and make reservations choose-once and client-immutable. Moderation and recovery are trusted administrator operations keyed to the same Firebase UID. Public profiles contain no Google name, email, provider ID, entitlement, or token data.
- Existing password accounts use a migration-only sign-in and link a Google credential to the current Firebase user so UID-owned data is preserved.
- Android Google sign-in uses the Capacitor Firebase Authentication bridge and the registered RykerSoft release SHA-1/SHA-256 certificate fingerprints.
