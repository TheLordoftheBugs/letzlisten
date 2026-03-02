# Letz Listen

A native radio app for Luxembourg, available on **iOS** and **Android** _(Android version currently under testing)_.

## Repository structure

```
letzlisten/
├── ios/                  # iOS app (Swift / SwiftUI)
│   ├── Radio/
│   └── Radio.xcodeproj
├── android/              # Android app (Kotlin / Jetpack Compose)
│   ├── app/
│   └── gradle/
├── docs/                 # Privacy policy & support pages
└── stations.json         # Shared station list (source of truth)
```

## iOS

### Features

- Stream curated Luxembourg radio stations
- Adaptive layout: portrait, landscape, and **iPad-optimised** (persistent station sidebar)
- Automatic song & artist detection from stream metadata
- Album artwork fetched via iTunes Search API
- AirPlay support
- Lock screen / Control Centre integration
- Favourites — save and search songs you loved
- Multilingual UI: Lëtzebuergesch · Français · Deutsch · English
- Offline-capable: stations cached locally, updated silently from remote

### Tech stack

| Layer | Technology |
|---|---|
| UI | SwiftUI |
| Audio | AVFoundation · AVPlayer |
| Lock screen | MediaPlayer (Now Playing / Remote Commands) |
| Networking | URLSession |
| Persistence | UserDefaults |
| Album art | iTunes Search API |
| Station logos | Google Favicon · Facebook Graph API |

### Requirements

- iOS 16+
- Xcode 15+

### Opening the project

```
open ios/Radio.xcodeproj
```

---

## Android _(under testing)_

### Features

- Stream curated Luxembourg radio stations
- Adaptive layout: persistent station sidebar on tablets (≥ 600 dp), bottom-sheet picker on phones
- Background playback via `MediaSessionService`
- Same `stations.json` source as iOS — stays in sync automatically
- HTTP streams supported (network security config included)

### Tech stack

| Layer | Technology |
|---|---|
| UI | Jetpack Compose · Material 3 |
| Audio | ExoPlayer (Media3) |
| State | ViewModel · StateFlow |
| Networking | kotlinx.serialization + URLSession |
| Images | Coil |

### Requirements

- Android 8.0+ (API 26)
- Android Studio Hedgehog or later

### Opening the project

1. Open **Android Studio**
2. `File → Open` → select the `android/` folder
3. Let Gradle sync and download dependencies

---

## Station list

Stations are driven by `stations.json` at the repository root. Both apps
ship a bundled copy in their respective asset folders and refresh silently
from the remote GitHub URL on each launch.

To enable or disable a station, edit the `isEnabled` field in `stations.json`
and push — both apps will pick up the change on next launch.

---

## Shoutcast / Icecast compatibility (iOS)

Some stations stream over Shoutcast v1, which replies with `ICY 200 OK`
instead of a standard HTTP response. iOS rejects this by default.

The app works around this by loading every stream through `AVURLAsset`
with the `Icy-MetaData: 1` header. This signals ICY protocol support to
the server, which causes it to fall back to a valid HTTP handshake and
also enables in-stream track metadata. The header is a no-op for standard
HTTPS / Icecast streams.

---

## Changelog

### Version 2.0 — 2026-03-01

#### iOS

- **iPad: animated split panels** — station sidebar and favourites panel now slide in/out independently instead of pushing the layout
- **iPad: favourites panel** — removed panel title, clear-all button right-aligned, corrected top padding on overlay buttons
- **iPad: share button** — fades out when station panel is open to avoid overlap; bottom divider repositioned
- **Bottom bar** — restored original AirPlay → Play/Stop → Share order after layout experiments
- **Share button** — disabled while playback is paused, re-enabled when playing
- **Playback fix** — `isPlaying` is now set to `false` synchronously on pause, preventing a brief stale-state window

#### Android _(initial public build)_

- **Player UI** — full redesign to match iOS: language button, heart in content area, two-button control bar (Share · Play/Stop)
- **ICY metadata** — real-time artist & title extracted from Shoutcast/Icecast in-stream metadata
- **Album artwork** — fetched from iTunes Search API, same logic as iOS
- **Station logos** — bundled assets for known stations with Google Favicon / Facebook Graph API fallback cascade
- **Favourites & share** — save tracks, share now-playing info in the same format as iOS
- **Loading states** — spinner in the artwork box while the station list is being fetched; track-info area hidden until first play
- **Multilingual UI** — Lëtzebuergesch · Français · Deutsch · English (Portuguese removed after review)
- **No auto-play on startup** — stream starts only when the user taps Play
- **HTTP streams** — network security config allows cleartext where required

#### Shared

- **stations.json v1.5** — Country Radio disabled; Crazy Poisons Radio retained as the sole active station

---

### Version 1.0 — 2026-02-28

- Initial release — iOS app published; Android project scaffolded (internal testing)

---

## Copyright

© 2026 Florentin Arno — see [LICENSE](LICENSE).
