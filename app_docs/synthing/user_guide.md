# Synthing User Guide

Synthing is an Android music sketchpad for chords, melodies, arrangements, and dual synth voices.

This guide covers **v1.0.14**. The update reduces repeated drawing work and adds clearer save and audio-recovery feedback.

## Table of Contents

- [1. Getting around](#1-getting-around)
- [2. Projects](#2-projects)
- [3. Playing chords and melodies](#3-playing-chords-and-melodies)
- [4. Recording](#4-recording)
- [5. Synth design](#5-synth-design)
- [6. Piano roll (ROLL)](#6-piano-roll-roll)
- [7. Overview strip (playhead scrubber)](#7-overview-strip-playhead-scrubber)
- [8. Clips and export](#8-clips-and-export)
- [9. Undo, settings, and saving](#9-undo-settings-and-saving)

## 1. Getting around

The top bar switches between four modes:

- **PROJECT** — create, load, and manage projects
- **PLAY** — perform with chords, keys, and grids
- **SYNTH** — design Synth A / Synth B sounds
- **ROLL** — edit the piano-roll arrangement

Transport controls (play, stop, record, loop), BPM, and scale live in the top control bar.

## 2. Projects

1. Open **PROJECT**.
2. Tap **New** for a blank project, or **Load** an existing one.
3. Use **Rename**, **Duplicate**, **Delete**, **Save**, and JSON **Import/Export** as needed.
4. Projects can contain **sections** with clip launcher slots and a continuous arrangement timeline.

Arrangement loop markers (IN / OUT) apply to the whole project timeline.

Creating or switching projects preserves pending edits in their original project. Deleting the selected project or section loads the replacement workspace before further saves run. Chord-sheet imports begin with their own notes rather than the previous workspace's take.

Projects load in the background. If startup shows **Couldn’t open your projects.**, use **Retry** to attempt loading again.

## 3. Playing chords and melodies

1. Open **PLAY**.
2. Use the **Synth A** (orange) and **Synth B** (purple) panels. Drag the divider to resize; tap headers to arm a synth for recording.
3. Switch each panel surface between **CHORDS**, **KEYS**, **GRID**, and **MODS**.
4. Tap chord pads to play. Empty pads show `—` until you assign pitches in edit mode.
5. Use performance toggles such as **Glide**, **Latch**, **Tog**, **Mono**, **Chord**, **Slide**, **Legato**, and **Arp**.
6. On **KEYS** or **GRID**, play scale-aware notes. Drag for expression when modulators are assigned.

Both standard and dual-row keyboards keep the complete 88-key range, A0 through C8, prepared while they are present. Quickly revealing offscreen keys does not wait for those keys to be built. When resizing fixed-cell grids, Synth A keeps its left corner anchored and Synth B keeps its right corner anchored, including fractional pan positions.

Previously visited tabs stay prepared for returning to them; hidden playhead displays stop collecting visual updates. Audio and recording timing continue independently of how often the screen redraws.

### Tempo and scale

- Adjust **BPM** with +/- or **TAP**.
- Open the scale control to change key and scale type for the keyboards and grids.

## 4. Recording

1. Arm Synth A, Synth B, or both from the **PLAY** tab.
2. Arm **Record** in the transport bar.
3. Optionally open **ROLL** and tap or drag the timeline ruler to place the playhead at the exact punch-in position.
4. Optionally enable **Loop** and set arrangement IN/OUT.
5. Press **Play**. Pre-roll (if enabled in settings) counts in before the selected punch-in position and held notes are captured on the punch-in boundary.
6. Play on the armed synths. Overdub is enabled by default: recording adds to the active project and preserves earlier notes, replacing only a same-synth/same-pitch note at the exact same start position.
7. Press **Stop** once to end recording and keep the playhead at that position. Press **Stop** again to return to arrangement IN/start. Long-press **Stop** for panic (silence all voices).

Long-press **Record** to configure pre-roll length, pre-roll click, or disable overdub for explicit overlap punch mode. Use the full-width arrangement overview above Synth A/B to position the playhead without leaving PLAY.

Held notes grow in the recording display. For a take extending beyond the current overview, its displayed range expands by whole bars. Stopping or changing projects finalizes held-note capture before changing the recording destination.

If the app shows **Audio stopped after an overload. Tap to dismiss, then press Play to resume.**, the engine has stopped voices, arpeggiators, and transport to recover from a full critical command queue. Tap the notice to dismiss it, check your take and playhead position, then press **Play** when ready. Play held keys again to start a new live note. Long-press **Stop** remains available for panic.

## 5. Synth design

1. Open **SYNTH**.
2. Choose **Synth A** or **Synth B**.
3. Browse **PRESETS**, or edit **CONTROL**, **LFO**, and **MOD**.
4. Parameter groups follow the signal path: source -> filters -> envelopes -> modulation -> FX -> output.
5. Use **+ SAVE**, restore/rename/update, and **EXPORT** / import for preset JSON.

Synth changes reach the live sound immediately. Saving combines rapid parameter changes in the background, and pending edits are captured before switching projects, clips, or sound scopes, previewing or copying a clip, saving a template, or exporting.

## 6. Piano roll (ROLL)

1. Open **ROLL**.
2. Switch the track between Synth A and Synth B.
3. Paint, select, and edit notes on the grid.
4. Use the left touchpads: **ZOOM**, **SCROLL**, **NUDGE**, **SELECT**, **EDIT SELECTED**.
5. Side tabs include **CTRL** (snap, note length, clip region, selection tools), **CHORDS**, **MOD** (automation), and **FILTER**.
6. Place the playhead with the overview or ruler, then press **Play** to start from that exact arrangement position.

### Editing tips

- Set snap and note length in **CTRL**.
- Use **SET IN** / **SET OUT** for clip/arrangement region markers.
- Create **Slide** (portamento) or **Legato** note links from the selection tools.
- Draw automation in the **MOD** side panel.

Dense arrangements reuse prepared note, link, and automation drawing. Scrolling keeps long notes and automation curves visible when they cross the view, even if their starting point is offscreen. The moving playhead and growing recorded notes use a separate visual layer; editing and scrubbing still use the current transport position.

## 7. Overview strip (playhead scrubber)

Across the full width above the ROLL control panel and piano-roll grid is a **bird's-eye overview** of the whole arrangement:

- The blue box is the current viewable area (viewport lens).
- **Tap and release** anywhere on the strip to place the playhead at that absolute arrangement position.
- **Drag with one finger** to scrub relative to the current playhead. The playhead moves by the drag distance and does not jump to where your finger first touched.
- **Drag with two fingers** to scrub relatively while snapping the playhead to each measure.
- The same overview is always available above both synth panels on **PLAY**. Pressing **Play** starts from the position selected there.

During recording, the overview expands by whole bars when the take outgrows its current range. Clip-property previews also keep their note drawing separate from the moving cursor.

## 8. Clips and export

When working inside a section, the clip launcher shows slots for takes.

- Long-press a slot for clip properties (length, loop, clear, preview).
- Choose MIDI or WAV export from clip properties. MIDI exports note events; WAV renders an audio bounce with the clip's selected synth sounds.
- Clear a clip for a fresh take without deleting the slot.

Choose the tracks and loop length before exporting, then select a destination in Android's document picker. Preparation and file writing happen in the background. Allow the current export to finish, or cancel its picker, before starting another export; an in-flight guard keeps each picker result paired with the correct MIDI/WAV payload. The app reports success after writing the document and reports an error if the write fails.

Project and preset JSON import/export also use the document picker. A cloud document provider may need a network connection even though Synthing's local projects and instruments work offline.

## 9. Undo, settings, and saving

- Use undo/redo in the top bar for notes and many performance edits.
- Open the gear icon for system settings across PROJECT / PLAY / SYNTH / ROLL.
- Projects auto-save in the background after edits. This includes arrangement and piano-roll notes, Synth A/B parameters, chords and arp settings, filters/colors, note links, and Play/Synth/Roll workspace state.
- Leaving the app finalizes active recording, captures the latest project/synth state, and queues remaining saves without waiting for disk.
- You can also export project JSON from **PROJECT** for a portable backup or transfer.

### If a save needs attention

When **Changes not saved yet.** appears, Synthing retains the failed save in memory and retries automatically. Tap **Retry** to request another attempt. If storage is full, free space and retry; keep the app open until the notice clears. Atomic file replacement protects the last successfully saved version. Changes still waiting in memory can be lost if Android ends the process before a write succeeds, so do not treat an unresolved warning as a completed save.

Save and delete operations are ordered together, and rapid edits are combined before writing. These safeguards prevent an older queued save from recreating a deleted file or sending pending synth edits into a newly selected project.
