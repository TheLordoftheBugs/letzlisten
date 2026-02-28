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

## Copyright

© 2026 Florentin Arno — see [LICENSE](LICENSE).
