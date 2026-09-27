# WordPlay.ing

Two word games for Android and desktop: swipe adjacent letters in Bungle or place tiles to make connected words in Scramble. Play locally, join online matches, explore custom rules, and compare verified scores within each game.

## Key Features
- **Scramble**: Place letter tiles on an 11×11, 15×15, or 19×19 board, with crossings, premium squares, and tile exchanges. Play Solo, Pass & Play, Online, or against Raid. Choose 5, 10, or unlimited turns per player and a separate turn timer. Combine barriers, hidden or randomized bonuses, stacking, or falling tiles and save custom modes. Verified online scores have their own rankings and awards.
- **Classic boards**: 4×4 or 5×5 dice grids with TWL06 dictionary validation (min 3 letters; Q expands to QU).
- **Modifiers & presets**: Letter Lock tile ownership, Strict modes, Gravity, Blind Mode, Combo Meter, Reroll, First Claim, Sabotage, and Word Grid's tile-swapping arrangement puzzle — plus Classic / Gravity Well / Ghost Grid / Zenith of Chaos presets.
- **Community challenges**: Start a compatible signed-in solo round to reserve a trusted publishable board, then automatically post its validated result at game over. Abandoned boards remain used without posting a score. Firebase recalculates scores and owns immutable per-board and combined standings; offline/local-only boards cannot be published after reveal.
- **Local pass & play**: Hand the device around with shared seeds, initials tumbler, and colored local players.
- **Live multiplayer**: Host or join a 4-character room code; lobby ready → countdown → simultaneous play with result reconciliation.
- **Seasons & leaderboards**: Today / This Week boards, a Career leaderboard (lifetime high score, longest word, and other public stats), season recaps, and tap-through player career profiles via Firebase.
- **Private Google accounts & cloud progression**: Google sign-in keeps career totals, personal bests, Daily Missions, awards, and unlocked tile themes available across devices. A separate, user-chosen player name is the only identity shown publicly. Guest play remains available for ordinary solo games.
- **Audio Lab**: Synthesized melodies, success chords by word length, mute toggle, and an in-app sequencer workstation.
- **Word gallery**: Post-game collection with Free Dictionary API definitions.
- **Daily mastery**: One shared seeded board each day, a Monday–Sunday week picker, comparable rankings that stay hidden until you play that board, account-isolated daily skill goals, personal bests, and static-board missed-word analysis. Only the first secured run is competitive; retries are practice-only.
- **Persistent progression**: Home page Awards tab tracks 50+ achievements across career milestones, scoring feats, Endless survival, streaks, custom modifiers, and multiplayer matches. Progression automatically syncs to Firestore with deduplication receipts to prevent double counting. Special achievements unlock exclusive tile themes.
- **Responsive feedback**: Connected selection paths, precise submission results, scoring callouts, adjustable effects, and optional haptics.
- **Neon collection**: Eight retro palettes, six tile finishes, five interface styles, and three restrained motion choices work across Bungle and Scramble. Four starters and 18 earnable upgrades; browse locked silhouettes, track category completion, and switch between real game previews. Appearance choices and locally earned cosmetic progress stay per profile on this device.
- **Accessible play**: Guided onboarding, keyboard tile controls, reduced motion, high contrast, larger letters, focus-managed dialogs, and live selection announcements.
- **Compact setup**: Tap a setting to open its option list above or below the button. Swipe over settings to scroll normally. Both games share full-screen Match Options, a Load a mode control, and expandable Selected rules with Save mode inside. The complete options page is prepared below Home so both pages move upward together smoothly. Home game tiles follow your scroll position directly. Player Settings contains theme, sound, haptics, motion, contrast, and letter-size preferences.
- **Reliable match completion**: Timer expiry is fail-safe even when a tile is selected, Android audio is unavailable, or post-match board analysis is still running.

## Platforms
- **Android** — Capacitor release APK for the RykerSoft hub (`com.rykersoft.wordplaying`)
- **Web** — Vite + React
- **Desktop** — Electron packaging from the same codebase
