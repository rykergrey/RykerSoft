# Storytime

Storytime is a local-first writing studio and social storytelling game for Android and Windows. Build reusable characters, locations, and items; import and revisit saved stories; narrate with device or local voices; and play collaborative writing games without an external AI provider.

## Standard Features

- Character, location, item, and voice-preset library
- Story import, export, reading, local saves, and undo history
- Anonymous Author and Pass the Pen in pass-and-play or online rooms
- Joining an online Story Party as a voting guest
- Device-native, Piper, and local IndexTTS narration
- Private Google Drive backup and restore
- User-chosen public room names that remain separate from private Google identity

The standard experience does not require a Gemini, OpenAI, Kimi, or ElevenLabs API key.

## PRO Features

- * AI story setup — generate concepts, casts, worlds, lore, themes, and story direction.
- * Interactive story generation — write new scenes, dialogue, choices, endings, and rerolls.
- * Creative revision — rewrite passages and enhance characters or player submissions.
- * Generated artwork — create portraits, expressions, scenes, and comic-style images.
- * Story Party hosting — host an AI-created adventure while guests vote privately.
- * Cloud narration — use Gemini Voice, OpenAI Speech, or ElevenLabs when the matching provider is configured.

`*` items require administrator-granted RykerSoft Pro access followed by Google sign-in to the same RykerSoft account inside Storytime. For trusted family and friends, Storytime retrieves only its package-scoped provider record at runtime and keeps those credentials in memory; users do not need to create or manage API keys.

Personal provider keys remain an optional device-local override. They are not required for standard features or for an entitled account whose RykerSoft provider record is configured.

## Platforms

- Android 7.0 or later (`minSdk 24`)
- Windows 10/11 x86-64 through the packaged Electron application
- Canonical package: `com.superstorycraft.ing`
