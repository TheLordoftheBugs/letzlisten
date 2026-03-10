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

## Features

- Stream curated Luxembourg radio stations
- (iOS) Adaptive layout: portrait, landscape, and iPad-optimised (persistent station sidebar and favourites panel)
- (Android) Adaptive layout: persistent station sidebar on tablets (≥ 600 dp), bottom-sheet picker on phones
- Automatic song & artist detection from stream metadata
- Album artwork fetched via iTunes Search API
- (iOS) AirPlay support
- (iOS) Lock screen / Control Centre integration
- Favourites — save, export, import and search songs you loved
- Multilingual UI: Lëtzebuergesch · Français · Deutsch · English
- (iOS) Offline-capable: stations cached locally, updated silently from remote
- (Android) Background playback via `MediaSessionService`
- (Android) HTTP streams supported (network security config included)

## Tech stack

### iOS

| Layer | Technology |
|---|---|
| UI | SwiftUI |
| Audio | AVFoundation · AVPlayer |
| Lock screen | MediaPlayer (Now Playing / Remote Commands) |
| Networking | URLSession |
| Persistence | UserDefaults |
| Album art | iTunes Search API |
| Station logos | Google Favicon · Facebook Graph API |

### Android

| Layer | Technology |
|---|---|
| UI | Jetpack Compose · Material 3 |
| Audio | ExoPlayer (Media3) |
| State | ViewModel · StateFlow |
| Networking | kotlinx.serialization |
| Images | Coil |

## Requirements

**iOS** — iOS 16+ · Xcode 15+

```
open ios/Radio.xcodeproj
```

**Android** — Android 8.0+ (API 26) · Android Studio Hedgehog or later

1. Open **Android Studio**
2. `File → Open` → select the `android/` folder
3. Let Gradle sync and download dependencies

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

### Version 1.2 — développement en cours

#### iOS

- **Favourites & Radio menus** — replaced native `List` with `ScrollView` + custom cards to match the Settings visual style; per-row trash button replaces swipe-to-delete
- **iPad panels** — favourites panel and station sidebar rows now share the same card style as Settings
- **Settings** — "Clear all" button visible but disabled (greyed out) when no favourites, consistent with Export button
- **Top buttons** — Settings and Done buttons repositioned to the right; station selector button moved to centre

---

### Version 1.1 — 2026-03-01

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
