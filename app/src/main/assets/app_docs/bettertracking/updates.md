# Release notes

## v1.1.5
- Replace app-funded day/week/month Health Coach generation with portable prompts for the user's own chatbot
- Default to Perplexity, with ChatGPT, Google Gemini, and copy-to-any-chatbot options
- Remove automatic background monthly AI reports while preserving access to previously saved reports
- Include chronological logs, notes, nutrients, custom values, recipe portions, targets, and adaptive context in each coaching prompt

## v1.1.4
- Restore **Continue with Google** in Profile settings and remove obsolete password/create-account controls
- Bundle the canonical RykerSoft Hub Firebase configuration so release builds no longer depend on an unpublished local environment file
- Use Android Credential Manager with the Hub web client ID, preserving the existing release signing identity and Pro entitlements

## v1.1.3
- Restore a complete, source-backed release after an unreleased direct-device build advanced the Android version
- Preserve the trusted Android signer and provide a monotonic update path without removing local app data
- Rebuild the current stable application bundle and synchronize RykerSoft hub metadata

## v1.1.1
- Add-from-library (staging tray and Library magic search) now uses each item’s library-defined unit (pcs, g, srv, etc.) instead of adjectives from the phrase (e.g. “three whole eggs” → 3 pcs)
- Library tab keeps search text, scroll position, and expanded groups when you switch to the staging tray and back

## v1.1.0
- RykerSoft AI unlock: sign in with your RykerSoft account under **Profile → API Keys** to sync Gemini and Groq keys after unlocking bettertracking in the RykerSoft App Manager
- AI features (Quick Log, AI Architect, chat, Coach Analysis, transcription) are now unlock-gated; manually entered keys still work and take priority
- All tracking, journal, library, and reminder features remain fully available without the unlock

## v1.0.2
- Release-signed APK for the RykerSoft hub
- Display name changed from bettertrack.ing to bettertracking

## v1.0.1
- First RykerSoft hub release (`com.rykersoft.bettertracking`)
