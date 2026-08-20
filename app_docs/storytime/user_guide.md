# Storytime User Guide

## Table of Contents

- [Standard Experience](#standard-experience)
- [PRO Features](#pro-features)
- [Getting Started](#getting-started)
- [Library](#library)
- [Local Games](#local-games)
- [Online Rooms](#online-rooms)
- [Stories and Imports](#stories-and-imports)
- [Narration](#narration)
- [Google Drive Sync](#google-drive-sync)
- [RykerSoft Pro Access](#rykersoft-pro-access)
- [Personal Provider Keys](#personal-provider-keys)
- [Privacy and Recovery](#privacy-and-recovery)

## Standard Experience

Storytime works without an AI-provider API key. You can build and edit the Library, import and read existing stories, export your work, use device or locally hosted narration, connect a private Google Drive backup, play Anonymous Author or Pass the Pen, and join another person's online game.

Standard features remain available when you are signed out of RykerSoft, your Pro grant is missing or revoked, the hub is offline, or a provider is unavailable.

## PRO Features

- * New AI-assisted stories — generate setup material and play an interactive story.
- * Story generation and revision — create scenes, choices, rerolls, endings, and rewrites.
- * Character and world assistance — enhance library entries and generate casts, lore, locations, and items.
- * Generated artwork — create portraits and story imagery.
- * Story Party hosting — generate the shared adventure while guests vote.
- * Cloud voices — use Gemini, OpenAI, or ElevenLabs narration.

`*` items require administrator-granted RykerSoft Pro access followed by sign-in to the same RykerSoft Google account inside the app. Entitled family and friends receive configured provider access automatically and never need to copy or manage RykerSoft API keys.

## Getting Started

The Home screen describes both experiences. Choose Library or a standard writing game to begin without a provider. Choose New Story for Pro-assisted creation. If provider access is unavailable, Storytime opens Settings so you can sign into RykerSoft Pro access or optionally add a personal key.

## Library

Library stores reusable characters, locations, items, and voice presets. Manual creation and editing are standard features. Buttons marked with `*` use a provider and require available Pro or personal-provider access.

Use the Library import/export controls to move reusable content without starting a story. Deleting a Library record does not delete an already saved story that contains its own copy.

## Local Games

Anonymous Author asks each player to write secret candidates, rate entries, and guess the winning author. Pass the Pen asks players to add sentences to one shared story and agree when it should end. Both can be played pass-and-play on one device without an API key.

Optional assistance buttons marked with `*` remain disabled until provider access is available. The core writing and scoring flow does not depend on them.

## Online Rooms

Online Anonymous Author and Pass the Pen use Google sign-in in Storytime's app Firebase project. Choose a public game name yourself; Storytime never publishes your Google name, email, picture, or hub entitlement.

Story Party guests may join and vote without provider access. The host creates the AI-assisted story and therefore needs Pro or a personal writing-provider key. Room codes and room data expire according to the online-room policy documented in the app.

## Stories and Imports

Saved stories remain on the device and can be opened for reading without a provider. Continuing or regenerating AI-authored content requires provider access. Export a story before clearing application data or moving to a device that is not connected to your Drive backup.

## Narration

Device Native TTS, Piper, and local IndexTTS do not require a cloud API key. Gemini Voice, OpenAI Speech, and ElevenLabs are provider-backed Pro features and appear with `*` in feature guidance.

Generated audio can consume substantial storage. Settings lets you choose whether all retained audio is included in Drive synchronization.

## Google Drive Sync

Google Drive sync is separate from RykerSoft Pro access. It uses your private Drive account to back up stories, pass-and-play sessions, Library content, generated media, and settings.

Personal provider-key overrides are included when you explicitly synchronize settings. RykerSoft-managed credentials are never stored locally and never enter Drive backups.

## RykerSoft Pro Access

1. Sign into RykerSoft Application Manager with Google.
2. Ask the administrator to grant Storytime to that hub account. The exact package is `com.superstorycraft.ing`.
3. In Storytime, open Settings → Pro & Keys.
4. Select **Sign in with Google** and choose the same account.
5. Select **Refresh** after the administrator grants or updates access.

An active grant unlocks only Storytime. It does not automatically grant future RykerSoft applications. Storytime cannot grant, revoke, or edit its own entitlement.

If access is revoked, the next startup or refresh clears all RykerSoft-managed credentials. Because trusted-family delivery places a provider credential on a trusted device for the session, rotate the package credential if a device is lost or trust is withdrawn.

## Personal Provider Keys

Personal keys are optional overrides under Settings → Pro & Keys. Each field starts hidden and has its own accessible show/hide button. A personal key takes priority over the matching RykerSoft-managed key and remains on the device until removed.

Do not enter a shared RykerSoft key manually. Remove personal keys before sharing the device, browser profile, or connected Drive account.

## Privacy and Recovery

App Firebase identity, hub Pro identity, public room name, and Google Drive authorization are separate. Signing out of one does not intentionally publish or merge another identity.

When offline, local work remains available. Pro operations fail closed when entitlement cannot be confirmed or a provider credential is missing. If Google sign-in, account linking, or room recovery reports a conflict, do not create a replacement identity to overwrite data; preserve local exports and resolve the existing account first.
