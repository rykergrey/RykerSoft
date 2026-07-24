# Synthing Technical Specs

## App identity

| Field | Value |
|-------|-------|
| Product name | Synthing |
| Package / applicationId | `com.rykersoft.synthing` |
| Namespace | `com.rykersoft.synthing` |
| Latest version | 1.0.8 (versionCode 9) |

## Platform requirements

| Field | Value |
|-------|-------|
| minSdk | 24 (Android 7.0) |
| targetSdk / compileSdk | 36 |
| JDK | 17 |
| ABIs | arm64-v8a, armeabi-v7a, x86, x86_64 |
| NDK | 27.1.12297006 |
| CMake | 3.22.1+ |
| C++ | C++17 (`c++_shared`) |

## Stack

- Kotlin + Jetpack Compose (Material 3)
- Navigation 3
- kotlinx.serialization JSON persistence
- Native audio: Oboe 1.9.x via `synthingaudio` (`AudioEngine.cpp`)
- Timing: 480 PPQ (ticks per quarter note); 1920 ticks per 4/4 measure

## Architecture

```
UI (Compose, ui/main/)
  -> MainScreenViewModel (+ domain extensions)
    -> Kotlin audio façade (Synthesizer, ArpSequencer, …)
      -> JNI
        -> C++ AudioEngine (Oboe callback, lock-free command ring)
  -> DataRepository
    -> Local JSON (projects, templates, settings)
```

## Persistence

- `projects/*.json` — song projects, sections, clips, continuous arrangement notes
- `project_templates/*.json`
- `system_settings.json`, `synth_settings.json`
- Auto-save with debounced writes; document-picker import/export

## Audio notes

- Never block or allocate in the Oboe audio callback
- Dual-track scheduling for Synth A and Synth B
- Offline WAV bounce via native clip render path
