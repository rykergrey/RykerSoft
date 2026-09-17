# Storytime Updates

## v1.3.0

- Add title-first scene insertion and full draft expansion on the board
- Add selective Pro Quick Start with source notes, character merging, and automatic recovery versions
- Add a solo-only floating writing assistant with screen-aware context and saved, automatically named conversations
- Add an idea inbox, browser dictation, editable manuscript, focus writing, and next-session notes
- Add character, viewpoint, story-thread, location, and chronology views plus linked continuity checks
- Add durable saved versions, alternative scene drafts, and passage-linked read-through revision notes
- Preserve the existing release signer and account, entitlement, and story-backup compatibility

## v1.2.0

- Add an offline story studio with movable scene cards, expanded drafts, character details, and production notes
- Add batch character editing and full-name renaming with before/after review and undo
- Add a story-scoped custodian with local search and commands, plus optional conversational editing
- Simplify Home, mode selection, adventure setup, game rules, and reader controls
- Add autosave conflict recovery, separate-copy saving, character draft recovery, and editable backups

- Fix Android Pro Google sign-in error 28444 by using the OAuth client registered with Storytime's package and release certificate
- Register Storytime's external web client in the RykerSoft Hub Google provider's safelist
- Show Google's underlying credential error when sign-in fails and a clear message when it is cancelled
- Preserve the separate RykerSoft Pro session and package-specific entitlement checks

## v1.1.0

- Add Director and Listener roles to online Story Party rooms
- Let one listener phone become the leased car speaker for the whole room
- Queue narration section by section, cache audio on the speaker device, and share playlist progress without duplicating TTS requests
- Automatically release or recover the speaker role after disconnects, with pause, resume, and retry controls
- Keep listeners synchronized with story text and choices without requiring them to vote or delaying the directors
- Harden Firestore rules so listeners cannot submit choice ballots and only the current listener lease can publish playback progress

## v1.0.2

- Restore a complete, source-backed release after an unreleased direct-device build advanced the Android version
- Recover and verify the original Android release signer so existing installations update in place
- Rebuild synchronized Android and Windows artifacts from the current stable source

## v1.0.0

- First RykerSoft Application Manager release for Android and Windows
- Separate the no-key standard experience from clearly marked Pro-assisted creation
- Add Google-account-bound Storytime entitlement at `com.superstorycraft.ing`
- Deliver Gemini, GPT, Kimi, and ElevenLabs credentials only from the exact package-scoped RykerSoft provider record
- Keep RykerSoft-managed credentials in memory and clear them on sign-out, revocation, missing entitlement, or refresh failure
- Replace packaged Windows Firebase OAuth code exchange with the temporary localhost Firebase browser helper
- Preserve optional personal provider-key overrides and existing local data
- Preserve app-owned Firebase online rooms, private UID authorization, and user-chosen public game names
