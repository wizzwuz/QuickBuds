# Experimental three-mode Quick Settings tile

Branch name intended for this change: `experimental/anc-three-mode-tile`

## Changes

- A single tap cycles through ANC -> Normal -> Transparency -> ANC.
- The tile label, subtitle, accessibility description, and icon reflect the active mode: Noise Cancellation (`ic_anc`), Normal (`ic_noise_off`), or Transparency (`ic_transparency`).
- A long press routes through Android's `ACTION_QS_TILE_PREFERENCES` handler. While ANC is active it shows a compact, app-themed picker for Smart / High / Low / Medium. Modes unsupported by the connected model are disabled rather than sending an unverified command.
- While Normal or Transparency is active, a long press opens the QuickBuds main screen instead of App Info.
- The APK workflow is configured to run on `experimental/**` branches as well as `main`.

## Important validation note

The Android manifest was XML-validated. A local Gradle build could not run in this environment because the Gradle 9.6.0 distribution could not be downloaded (`UnknownHostException: services.gradle.org`). This source is **not yet build-verified**; use the GitHub Actions workflow on the experimental branch to get the authoritative compile result and APK artifact.
