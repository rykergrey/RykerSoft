# Storytime User Guide

## Table of Contents

- [Standard Experience](#standard-experience)
- [PRO Features](#pro-features)
- [Getting Started](#getting-started)
- [Story Studio](#story-studio)
- [World Library](#world-library)
- [Local Games](#local-games)
- [Online Rooms](#online-rooms)
- [Stories and Imports](#stories-and-imports)
- [Narration](#narration)
- [Google Drive Sync](#google-drive-sync)
- [RykerSoft Pro Access](#rykersoft-pro-access)
- [Personal Provider Keys](#personal-provider-keys)
- [Privacy and Recovery](#privacy-and-recovery)

## Standard Experience

Storytime works without a writing-provider connection. You can build and edit the Library, import and read existing stories, export your work, use device or locally hosted narration, connect a private Google Drive backup, play Anonymous Author or Pass the Pen, and join another person's online game.

Standard features remain available when you are signed out of RykerSoft, your Pro grant is missing or revoked, the hub is offline, or a provider is unavailable.

## PRO Features

- * Quick Start — turn rough notes into selected scene cards, linked characters, and story details while preserving the original material.

- * Writing assistant — discuss your writing with current-screen context, saved conversations, and reviewed changes, throughout solo Story Studio.

- * Adventure creation — generate setup material and play an interactive story.
- * Story generation and revision — create scenes, choices, rerolls, endings, and rewrites.
- * Character and world assistance — enhance library entries and generate casts, lore, locations, and items.
- * Generated artwork — create portraits and story imagery.
- * Story Party hosting — generate the shared adventure while guests vote.
- * Cloud voices — use Gemini, GPT, or ElevenLabs narration.

`*` items require administrator-granted RykerSoft Pro access followed by sign-in to the same RykerSoft Google account inside the app. Entitled family and friends receive configured provider access automatically and never need to copy or manage RykerSoft API keys.

## Getting Started

Choose **Solo story → Write a story** to start an offline scene board, or **Play an adventure** for an interactive story. Home puts your latest story first. **Your stories** contains saved work; **World library** holds reusable characters, places, and items. Advanced options remain available under customization and Settings.

## Story Studio

Use **Add scene** for a quick title-only card, or choose **Details** to develop it immediately. Expand full drafts on the board. **Ideas**, **Manuscript**, **Threads**, and **Versions** support capture, focus writing, continuity, and revision. The floating **Writing assistant** follows the active screen throughout solo Studio; its automatically named conversations are saved on this device, outside story backups. Pro **Quick Start** organizes rough notes into a reviewable set of scenes, characters, and details. See [the complete Story Studio guide](story-studio.md) for all controls and recovery options.

Start with one moment per card. Drag cards into sequence, use the arrow buttons, or choose a position. Open a card to expand its short premise into a full scene. **Develop the scene** contains goals, conflict, consequences, setting, and direction. Link cast members under **Characters in this scene**.

Use **Characters** to edit the cast together. Review changes before applying them. A full-name rename updates matching names throughout the story; aliases and partial names are not guessed. Undo and redo are available during the open session.

The **Custodian** searches only the current story. Without a connection it returns source passages and supports commands such as `Add card The visitor | A stranger arrives`, `Move card 3 before 1`, and `Rename Mara to Lena`. Connected writing tools enable open-ended questions and editing requests. Changes remain proposals until reviewed and applied; an outdated proposal cannot overwrite newer work.

Applied changes autosave on the device. Unreviewed character drafts recover within the browser session, but enter backups only after being applied. If another window changes the story, save your version as a separate copy. **Manuscript** follows the card order. Export the draft, cards and draft, or a complete editable ZIP backup.

## World Library

Library stores reusable characters, locations, items, and voice presets. Manual creation and editing are standard features. Buttons marked with `*` use a provider and require available Pro or personal-provider access.

Use the Library import/export controls to move reusable content without starting a story. Deleting a Library record does not delete an already saved story that contains its own copy.

## Local Games

Anonymous Author asks each player to write secret candidates, rate entries, and guess the winning author. Pass the Pen asks players to add sentences to one shared story and agree when it should end. Both can be played pass-and-play on one device without an API key.

Optional assistance buttons marked with `*` remain disabled until provider access is available. The core writing and scoring flow does not depend on them.

## Online Rooms

Online Anonymous Author and Pass the Pen use Google sign-in in Storytime's app Firebase project. Choose a public game name yourself; Storytime never publishes your Google name, email, picture, or hub entitlement.

Story Party guests choose **Director** or **Listener** before marking themselves ready. Directors vote privately on each choice. Listeners see the story and choices but do not vote and never hold up the ballot. The host remains a director and creates each assisted scene once, so hosting requires Pro or a personal writing-provider key.

For a road trip, have the phone connected to the car stereo join as a Listener. In the live room, select **Use this phone as car speaker**. That phone alone prepares and plays each new section in order. It keeps a short renewable room lease, caches audio locally, and publishes only playlist progress to the room. If it disconnects, another listener can take over after the lease expires. Use **Pause playlist**, **Resume playlist**, or **Retry this section** when needed.

The speaker phone uses its own narration provider, API access, and local voice presets. No API keys or audio files are written to the multiplayer room. Room codes and room data expire according to the online-room policy documented in the app.

## Stories and Imports

Saved stories remain on the device and can be opened for reading without a provider. Continuing or regenerating adventure content requires provider access. Export a story before clearing application data or moving to a device that is not connected to your Drive backup.

## Narration

Device Native TTS, Piper, and local IndexTTS do not require a cloud API key. Gemini Voice, GPT Speech, and ElevenLabs are provider-backed Pro features and appear with `*` in feature guidance.

Generated audio can consume substantial storage. Settings lets you choose whether all retained audio is included in Drive synchronization.

Road-trip speaker audio is cached on the selected speaker device so reconnecting that same phone does not needlessly regenerate completed clips. Other phones receive text and playback progress, not duplicate audio requests.

## Google Drive Sync

Google Drive sync is separate from RykerSoft Pro access. It uses your private Drive account to back up stories, pass-and-play sessions, Library content, generated media, and settings.

Personal provider-key overrides are included when you explicitly synchronize settings. RykerSoft-managed credentials are never stored locally and never enter Drive backups.

## RykerSoft Pro Access

1. Sign into RykerSoft Application Manager with Google.
2. Ask the administrator to grant Storytime to that hub account. The exact package is `com.superstorycraft.ing`.
3. In Storytime, open Settings → Account & access.
4. Select **Sign in with Google** and choose the same account.
5. Select **Refresh** after the administrator grants or updates access.

An active grant unlocks only Storytime. It does not automatically grant future RykerSoft applications. Storytime cannot grant, revoke, or edit its own entitlement.

If access is revoked, the next startup or refresh clears all RykerSoft-managed credentials. Because trusted-family delivery places a provider credential on a trusted device for the session, rotate the package credential if a device is lost or trust is withdrawn.

## Personal Provider Keys

Personal keys are optional overrides under Settings → Account & access. Each field starts hidden and has its own accessible show/hide button. A personal key takes priority over the matching RykerSoft-managed key and remains on the device until removed.

Do not enter a shared RykerSoft key manually. Remove personal keys before sharing the device, browser profile, or connected Drive account.

## Privacy and Recovery

App Firebase identity, hub Pro identity, public room name, and Google Drive authorization are separate. Signing out of one does not intentionally publish or merge another identity.

When offline, local work remains available. Pro operations fail closed when entitlement cannot be confirmed or a provider credential is missing. If Google sign-in, account linking, or room recovery reports a conflict, do not create a replacement identity to overwrite data; preserve local exports and resolve the existing account first.
