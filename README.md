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

## Requirements

- iOS 16+
- Xcode 15+

## License

See [LICENSE](LICENSE).
