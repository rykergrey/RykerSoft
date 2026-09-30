# Synthing

Synthing is an Android music sketchpad with a continuous arrangement timeline, dual synth voices, chord grids, scale-aware keyboards, and a piano-roll editor backed by a native low-latency Oboe engine.

Version **1.0.14** focuses on Play-grid resizing, dense visual editing, and project/audio reliability. Display updates follow screen frames while native audio and recording keep their own timing.

## Features

- Continuous infinite-arrangement timeline with arrangement-wide loop IN/OUT markers
- Dual Synth A / Synth B subtractive engines with presets, modulators, LFO, and FX
- Play tab chord pads, scale-aware keys, and isomorphic grids with performance toggles
- Standard and dual-row keyboards keep the full 88-key range prepared for immediate reveal and playing; cached grid shapes, labels, and note highlights reduce repeated work during resizing
- Piano-roll editor with touchpads, snap/quantize, automation lanes, and note links
- Cached note, link, and automation drawing with viewport indexes that retain sustained notes and curves crossing the screen; separate moving playheads and growing recording overlays
- Full-width ROLL bird's-eye overview above the control panel and editor grid, with absolute tap positioning, relative one-finger scrubbing, and two-finger measure snapping
- Full-width Play-tab overview above Synth A/B for immediate positioning and playback from the selected playhead
- Exact ROLL playback from the placed playhead without stale clip-length wrapping
- Non-destructive overdub recording by default from any playhead position, with reliable pre-roll capture, live note display, dynamic synth arming, and two-stage Stop/return-to-start behavior
- Project manager with sections, clip launcher slots, templates, and JSON import/export
- Background auto-save for arrangement and piano-roll notes, chord/arp assignments, note filters, Synth A/B parameters, and Play/Synth/Roll workspace state, with a visible **Retry** action when a save fails
- Atomic file replacement and ordered saves/deletes; project changes retain pending edits in their original destination, and pausing queues the latest state without waiting for storage
- Background project loading and document import/export, including MIDI and offline WAV clip export with guarded export requests
- Audio shutdown waits for native polling and recording finalization; a visible recovery notice explains how to resume after a command overload stops playback
- Optimized release builds with R8/resource shrinking and generated startup/baseline profiles; hidden visual clocks and inactive target pulses stop running

## Platforms

- Android API 24+ (arm64-v8a / armeabi-v7a / x86 / x86_64)
- Projects and settings stored locally on device as JSON
- No account or network connection required
