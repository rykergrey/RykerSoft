# Storytime Updates

## v1.0.0

- First RykerSoft Application Manager release for Android and Windows
- Separate the no-key standard experience from clearly marked Pro-assisted creation
- Add Google-account-bound Storytime entitlement at `com.superstorycraft.ing`
- Deliver Gemini, OpenAI, Kimi, and ElevenLabs credentials only from the exact package-scoped RykerSoft provider record
- Keep RykerSoft-managed credentials in memory and clear them on sign-out, revocation, missing entitlement, or refresh failure
- Replace packaged Windows Firebase OAuth code exchange with the temporary localhost Firebase browser helper
- Preserve optional personal provider-key overrides and existing local data
- Preserve app-owned Firebase online rooms, private UID authorization, and user-chosen public game names
