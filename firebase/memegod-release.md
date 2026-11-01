# Meme God access rule, 2026-10-31

`memegod.firestore.rules` snapshots the actual live Firestore rules and adds only the Meme God provider-read exception for any literal Boolean true RykerSoft Pro grant. Other package access, Google-session requirements, administrator protections and user-write restrictions are preserved. No grants were modified by this rule deployment.

This snapshot intentionally starts from deployed rules, which differ from the older root `firestore.rules`. Do not deploy the older file over these protections.

Deployed ruleset: `projects/rykersoft-abe84/rulesets/98027678-ba50-4969-9885-fbbf09d7fb2e`.

Six emulator tests passed using `firebase.memegod-tests.json` and `firestore-tests/memegod.rules.test.js`, with Java 21. The provider capability uses structured credential fields; the OpenAI key remains in protected Firestore, never this repository or the APK.
