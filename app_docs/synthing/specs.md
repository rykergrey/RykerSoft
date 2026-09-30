# Synthing Technical Specs

## App identity

| Field | Value |
|-------|-------|
| Product name | Synthing |
| Package / applicationId | `com.rykersoft.synthing` |
| Namespace | `com.rykersoft.synthing` |
| Latest version | 1.0.14 (versionCode 15) |

## Platform requirements

| Field | Value |
|-------|-------|
| minSdk | 24 (Android 7.0) |
| targetSdk / compileSdk | 36 |
| JDK | 17 |
| ABIs | arm64-v8a, armeabi-v7a, x86, x86_64 |
| NDK | 27.1.12297006 |
| CMake | 3.22.1 |
| C++ | C++17 (`c++_shared`) |

## Stack

- Kotlin + Jetpack Compose (Material 3)
- Navigation 3
- kotlinx.serialization JSON persistence
- Native audio: Oboe 1.9.3 via `synthingaudio` (`AudioEngine.cpp`), with Oboe packaged for all four ABIs
- Timing: 480 PPQ (ticks per quarter note); 1920 ticks per 4/4 measure

## Architecture

```
UI (Compose, ui/main/)
  -> Frame-sampled visual state and retained drawing geometry
  -> MainScreenViewModel (+ domain extensions)
    -> Kotlin audio façade (Synthesizer, ArpSequencer, …)
      -> JNI
        -> C++ AudioEngine (Oboe callback, bounded command queue and owned schedules)
  -> DataRepository
    -> Ordered background writer -> atomic local JSON files
```

## Persistence

- `projects/*.json` — song projects, sections, clips, continuous arrangement notes
- `project_templates/*.json`
- `system_settings.json`, `synth_settings.json`
- Auto-save and synth snapshot coalescing with a 300 ms delay; live synth controls apply immediately
- Project-level continuous arrangement notes plus section-workspace compatibility snapshots
- Snapshots retain their original project/clip/scope; pending synth edits flush into repository state before destination changes, preview/copy/template operations, exports, and pause
- One background writer orders atomic writes and deletes, retains failed operations, automatically retries, and exposes a user-triggered **Retry** action
- Pause finalizes recording, captures the latest state, and queues storage work without waiting for disk; atomic replacement protects the previous completed file, but queued changes are not durable until the write succeeds
- Project initialization and document streams/queries run on `Dispatchers.IO`; JSON/MIDI encoding and offline WAV preparation run off the UI thread with captured inputs
- Android document-picker import/export, separate pending export payloads, and request guards

## Rendering and release optimization

- Standard and dual-row keyboards prepare all 88 pitches from A0 to C8, including offscreen keys
- Cached grid geometry, labels, and note-highlight decisions; resize handling preserves Synth A's left and Synth B's right anchor
- Static piano-roll/overview layers are separate from playheads and live recording bars; viewport indexes include sustained notes and crossing automation segments
- Automation paths use adjacent-node interpolation; note-link paths and pitch-row lookups are reused
- Display-frame sampling updates visual state; raw transport readings remain available for gesture actions and capture
- Previously visited tabs retain their composition; hidden tabs suspend visual-clock subscriptions, and inactive target-selection pulses stop
- Release builds enable R8 minification/optimization and resource shrinking, consume generated startup/baseline profiles, and include AndroidX ProfileInstaller
- A separate Macrobenchmark module provides profile generation, frame journeys for grid resize/rapid key reveal, and lifecycle checks; profile generation requires API 33+ or a rooted device
- Physical-device frame and audio measurements remain necessary to characterize a particular device; emulator runs and host tests do not establish audio quality or a universal frame-rate guarantee

## Audio timing and ownership

- Dual-track scheduling for Synth A and Synth B
- The native audio clock supplies playback/capture timing at 480 PPQ; frame-paced UI updates do not schedule notes
- Live JNI producers and engine lifecycle are serialized outside the render callback; the callback does not acquire that producer monitor
- The polling worker owns one native handle generation and is joined before destruction; pending Oboe callbacks retain engine ownership
- Mutable audio state and playback schedule snapshots have explicit ownership so producers cannot overwrite a schedule being rendered
- Bounded command storage reserves capacity for release/stop requests and coalesces eligible controls without crossing note transitions; persistent patch/routing settings have latest-value fallbacks
- Critical queue exhaustion finalizes capture, silences voices, stops arpeggiators/transport, and raises the app's recovery notice
- Recording finalization waits for an acknowledged capture flush; timeout recovery quiesces the stream and completes cleanup before the recording destination changes
- Arpeggiator pitch/step preparation uses bounded storage; allocation and concurrency regression tests cover selected native paths
- Offline WAV bounce uses an independent native engine instance
