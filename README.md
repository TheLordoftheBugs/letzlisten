# LetzListen

A native iOS radio app for Luxembourg, built with SwiftUI.

## Features

- Stream curated Luxembourg radio stations
- Automatic song & artist detection from stream metadata
- Album artwork fetched via iTunes Search API
- AirPlay support
- Lock screen / Control Centre integration
- Favorites
- Multilingual UI: Lëtzebuergesch · Français · Deutsch
- Offline-capable: stations cached locally, updated silently from remote

## Tech stack

- SwiftUI + Combine
- AVFoundation (audio streaming)
- MediaPlayer (lock screen / Now Playing)
- URLSession (remote station list + album art)
- UserDefaults (cache versioning + last played station)

## Station list

Stations are driven by a remote `stations.json` file. The app ships a
bundled fallback and updates silently on launch when a new version is
detected.

## Shoutcast / Icecast compatibility

Some stations stream over Shoutcast v1, which replies with `ICY 200 OK`
instead of a standard HTTP response. iOS rejects this by default.

The app works around this by loading every stream through `AVURLAsset`
with the `Icy-MetaData: 1` header. This signals ICY protocol support to
the server, which causes it to fall back to a valid HTTP handshake and
also enables in-stream track metadata. The header is a no-op for
standard HTTPS / Icecast streams.

## Requirements

- iOS 16+
- Xcode 15+

## License

See [LICENSE](LICENSE).
