# Story studio

Open **Solo story → Write a story** to work locally. No account or writing-provider connection is required.

## Writing flow

- **Add scene** opens a small title-first popup. Enter a title and press Enter or **Insert** to stay on the board. **Keep adding scenes** clears and refocuses the title after each insert, preserving insertion order. **Details** inserts the card and opens its full editor. Canceling an empty popup creates nothing.
- Keep one moment on each card: a title and short premise remain visible on the board. Expand **Full scene draft** (or **Full script**) to read the entire draft in place, with paragraphs and screenplay line breaks preserved.
- Open a card or choose **Details** to write the scene, develop its conflict and consequences, link characters, or add direction and production notes.
- Arrange cards by dragging, using earlier/later arrows, or choosing a position. Search, section filters, bulk section assignment, and reviewed bulk removal support longer stories.
- Edit the cast together in **Characters**. Review a batch of edits before applying it. Renaming replaces whole full-name matches throughout the current story in one pass, including name swaps; it does not replace partial words or guess aliases.
- **Ideas** keeps fragments outside the manuscript. Paste one idea per line, optionally with `title | description`, or dictate using the browser's speech service. Add several ideas at once, promote selected ideas, or drag an idea from the board's tray into position. Unfinished capture text is retained for the browser session.
- **Manuscript** edits the same scene titles, summaries, and drafts as the board. **Focus scene** keeps one scene beside its purpose, characters, and neighbors. **Next time, start here…** stores a return note with the story.
- **Board view** groups scenes by character, point of view, story thread, location, or chronology. These views leave manuscript order intact. Each scene's planning details can link prerequisites, thread appearances, setups, and payoffs.
- **Threads** follows subplots, character arcs, clues, and themes. Linked prerequisites and setups support continuity questions when scenes move or disappear. Authors can mark an ordering as intentional and revisit those choices. These checks follow explicit links; they do not automatically establish every narrative dependency.
- **Read & listen** offers narrator and reading-pace controls, and character voices for explicit screenplay dialogue cues. Voices come from the browser and device, with local voices listed first. Mark a passage for revision while reading or listening. Notes preserve the original quotation, flag changed or ambiguous passages, and can be resolved later.
- Export the manuscript, cards plus drafts, or a complete editable ZIP backup.

## Quick Start

**Quick Start · Pro**, available on the scene board and in Add scene, accepts freeform material: titles, scene ideas, character descriptions, places, objects, plot points, dialogue, or a screenplay. It uses the existing Pro/personal writing-provider access and the story's writing settings. Manual scene creation remains available without a connection.

- **Organize my notes** captures supplied material, derives short titles and card summaries, and leaves unspecified details empty. **Help develop them** also invites proposed creative additions.
- The proposal can create or update scenes and characters, link the cast, fill all scene-development fields, and set the story title, premise, notes, and draft format. Scene locations and direction plus story notes hold places and objects; Quick Start does not create separate World library records.
- New scenes are inserted in order at the chosen position. Existing scenes keep their positions. Existing character names are reused; renames remain in the character editor or writing assistant.
- Review shows complete before/after changes and lets the author select individual scenes, characters, and story fields, edit scene titles, or merge a proposed character into an existing one. References are relinked; missing dependencies block applying until the selection is consistent.
- Verified source quotations link back to the supplied notes. Creative additions are labeled as proposed; material without a verified quotation is labeled unlinked. Exact quotation checks confirm wording, not the correctness of an interpretation.
- Apply saves a **Before Quick Start** version, preserves the original input and source links, and applies the selected plan atomically. It is undoable as one step; a plan from an earlier story revision cannot overwrite later writing. Canceling or encountering an invalid response applies none of the plan.
- Notes stay in the open form and session storage until successfully applied, including when navigating to Settings to connect writing access. Closing a request ignores its eventual response. Input is limited to 80,000 characters, with up to 100 scenes and 100 characters per batch; larger projects can be added in batches.

## Writing assistant

A floating **Writing assistant** button is available throughout the solo writing studio, including Add scene, Quick Start, and change-review dialogs. It is not mounted in other solo modes or multiplayer. Conversations are saved separately for each story on this device; new conversations receive a title from the first prompt. Authors can switch, rename, or delete conversations. Closing the panel or changing studio tabs preserves the active conversation and unfinished message. Pending answers stay with their originating conversation; canceled or deleted requests cannot recreate a thread.

The assistant receives programmatic context: the current screen, selected and visible source IDs, selected text, and relevant screen details. These include unsaved character changes, idea capture, the pending scene title, Quick Start's notes and selected plan, alternative drafts, historical comparisons, and proposed-change review. This uses application state and semantic source markers, without screenshots. Archived snapshots, original imports, and alternative drafts are not treated as the current manuscript. Screen details and previous conversation are bounded; a very large story may need a smaller request.

With a writing-provider connection, **Discuss** offers brainstorming and writing guidance; **Find in story** requires exact supporting excerpts; **Request changes** prepares reviewed edits. No mode silently applies model output. Unsupported quoted evidence is rejected. In Find in story, an answer without evidence is replaced with an insufficient-support response. Exact quotation validation does not prove every interpretation; linked passages remain inspectable.

Without a connection, or with **Local search & commands only** selected, questions find matching story passages. Local edit commands include:

```
Add card The visitor | Someone arrives carrying an unopened letter.
Insert card after 2: The crossing | They reach the island at dawn.
Move card 3 before 1
Delete card 2
Rename Mara to Lena
```

Connected editing supports story notes and next-session notes, scene creation and all supported planning fields, insertion, movement, removal, character creation and updates, full-name renames, idea inbox entries, and narrative threads. Every edit has a before/after preview and a durable **Before writing assistant changes** snapshot. Continuity consequences appear in the review. Unknown fields, invalid references, duplicate identities, and stale proposals fail without partially applying changes. Reloaded chat history retains the discussion but requires proposals to be regenerated against the current story.

Only the current story and explicitly presented screen details are sent as writing context. The assistant has no browsing tools or access to other stories, application storage, or account credentials through that context. Chat history is stored locally outside the story document; it is not included in story ZIP or Drive backups. Chat storage failures are visible and retryable.

## Versions and alternatives

**Versions** saves named story snapshots that survive reopening and are included in full backups. Compare a snapshot with the current story, then review a restore; restoring first saves the current work as another version. Quick Start's original material is also available here. The workspace supports up to 40 saved versions; export or remove an older version when full.

Each scene has **Alternative drafts**. Keep the current draft, try another approach, and compare them. Reviewing an alternative replacement preserves the outgoing draft too. Alternatives are retained in backups but only the active scene draft appears in the manuscript. Character renames update current prose and workspace notes; historical versions, alternative drafts, original imports, and anchored quotations keep their original wording.

## Persistence and recovery

- Applied edits autosave to IndexedDB after a short debounce, with a visible save status. Navigation flushes pending work.
- Writes compare both the saved revision and the prior document in one transaction. A conflicting window cannot silently overwrite a newer copy. Save failures offer retry, a separate copy, and file export.
- Undo/redo retains the latest 40 editing steps during the open session. Typing in one field is grouped into short runs. Saved versions provide separate durable recovery points.
- Unreviewed character drafts are kept separately in session storage for recovery after browser navigation or reload. They are not part of the canonical story, sync, or exported backups until applied. Discarding explicitly clears this temporary draft.
- Optional workspace fields preserve compatibility with older studio documents. Existing ZIP and Drive backup paths retain the complete studio document, including ideas, planning links, revision notes, source material, snapshots, and alternatives. Imports validate it and receive a separate local identity. Cloud conflict copies mirror their new title into the studio document.

## Navigation changes

Home emphasizes Continue and four concise modes. Saved writing lives in **Your stories**; reusable characters, locations, and items live in **World library**. Access details and personal connections live in Settings. Adventures begin with a premise and optional mood, with cast, world, direction, and writing profiles available through customization. A shared room-code entry discovers the game type. Party lobbies prioritize starting or resuming, while custom rules are disclosed when needed. Reader controls put choices ahead of regeneration and ending controls.

## Verification

`npm run test:studio` covers document validation, atomic editing, stale proposals, reorder/insertion, rename boundaries and swaps, backup round trips, grounded excerpts, action parsing, bounded screen/conversation context, chat persistence and story isolation, selective Quick Start review and provenance, idea promotion, structural lenses, continuity impacts, durable versions, alternative drafts, speech chunking, and revision anchors. `npm run check` includes this suite with type checking, existing access/multiplayer checks, and the production build.

Browser QA covers integrated desktop/mobile authoring, dialog chat context, thread persistence, selective Quick Start, full ZIP round trips, manuscript revision notes, and simulated speech lifecycle. Provider responses and speech are simulated in those checks. Live provider responses, microphone transcription, installed voice quality, authenticated room joining, and live Drive synchronization need configured devices/accounts for end-to-end verification; local authoring is independently usable.
