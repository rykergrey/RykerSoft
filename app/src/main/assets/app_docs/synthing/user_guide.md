# Synthing User Guide

Synthing is an Android music sketchpad for chords, melodies, arrangements, and dual synth voices.

## Table of Contents

- [1. Getting around](#1-getting-around)
- [2. Projects](#2-projects)
- [3. Playing chords and melodies](#3-playing-chords-and-melodies)
- [4. Recording](#4-recording)
- [5. Synth design](#5-synth-design)
- [6. Piano roll (ROLL)](#6-piano-roll-roll)
- [7. Overview strip (viewport navigator)](#7-overview-strip-viewport-navigator)
- [8. Clips and export](#8-clips-and-export)
- [9. Undo and settings](#9-undo-and-settings)

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

## 3. Playing chords and melodies

1. Open **PLAY**.
2. Use the **Synth A** (orange) and **Synth B** (purple) panels. Drag the divider to resize; tap headers to arm a synth for recording.
3. Switch each panel surface between **CHORDS**, **KEYS**, **GRID**, and **MODS**.
4. Tap chord pads to play. Empty pads show `—` until you assign pitches in edit mode.
5. Use performance toggles such as **Glide**, **Latch**, **Tog**, **Mono**, **Chord**, **Slide**, **Legato**, and **Arp**.
6. On **KEYS** or **GRID**, play scale-aware notes. Drag for expression when modulators are assigned.

### Tempo and scale

- Adjust **BPM** with +/- or **TAP**.
- Open the scale control to change key and scale type for the keyboards and grids.

## 4. Recording

1. Arm **Record** in the transport bar.
2. Optionally enable **Loop** and set arrangement IN/OUT.
3. Press **Play**. Pre-roll (if enabled in settings) counts in before capture.
4. Play on armed synths. Overdub can stack new takes; live notes appear in the roll while recording.
5. Press **Stop** to end. Long-press **Stop** for panic (silence all voices).

Use the Play-tab header **mini-map** to scrub the playhead for punch-in recording.

## 5. Synth design

1. Open **SYNTH**.
2. Choose **Synth A** or **Synth B**.
3. Browse **PRESETS**, or edit **CONTROL**, **LFO**, and **MOD**.
4. Parameter groups follow the signal path: source -> filters -> envelopes -> modulation -> FX -> output.
5. Use **+ SAVE**, restore/rename/update, and **EXPORT** / import for preset JSON.

## 6. Piano roll (ROLL)

1. Open **ROLL**.
2. Switch the track between Synth A and Synth B.
3. Paint, select, and edit notes on the grid.
4. Use the left touchpads: **ZOOM**, **SCROLL**, **NUDGE**, **SELECT**, **EDIT SELECTED**.
5. Side tabs include **CTRL** (snap, note length, clip region, selection tools), **CHORDS**, **MOD** (automation), and **FILTER**.

### Editing tips

- Set snap and note length in **CTRL**.
- Use **SET IN** / **SET OUT** for clip/arrangement region markers.
- Create **Slide** (portamento) or **Legato** note links from the selection tools.
- Draw automation in the **MOD** side panel.

## 7. Overview strip (viewport navigator)

Above the piano-roll grid is a **bird's-eye overview** of the whole arrangement:

- The blue box is the current viewable area (viewport lens).
- **Tap** anywhere on the strip to jump the piano roll to that region.
- **Drag** on the strip to scrub the viewport across the arrangement.
- This moves the view only — it does **not** place the playhead.

## 8. Clips and export

When working inside a section, the clip launcher shows slots for takes.

- Long-press a slot for clip properties (length, loop, clear, preview).
- Export a clip bounce as WAV from clip properties when available.
- Clear a clip for a fresh take without deleting the slot.

## 9. Undo and settings

- Use undo/redo in the top bar for notes and many performance edits.
- Open the gear icon for system settings across PROJECT / PLAY / SYNTH / ROLL.
- Projects auto-save; you can also export project JSON for backup or transfer.
