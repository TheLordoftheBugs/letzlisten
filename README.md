# Letz Listen

A native radio app for Luxembourg, available on **iOS** and **Android** _(Android version currently under testing)_.

> Source code is maintained in a private repository.
> This repository hosts public assets and documentation only.

## What's here

```
letzlisten/
├── docs/          # Privacy policy & support pages
└── stations.json  # Station list (shared source of truth for both apps)
```

## Station list

Stations are driven by `stations.json` at the repository root. Both apps
ship a bundled copy and refresh silently from the remote GitHub URL on each launch.

To enable or disable a station, edit the `isEnabled` field in `stations.json`
and push — both apps will pick up the change on next launch.

## About the app

### Features

- Stream curated Luxembourg radio stations
- Automatic song & artist detection from stream metadata
- Album artwork fetched via iTunes Search API
- Lock screen / Control Centre integration
- Favourites — save and search songs you loved
- Multilingual UI: Lëtzebuergesch · Français · Deutsch · English

### Platforms

| Platform | Status |
|---|---|
| iOS 16+ | Available on the App Store |
| Android 8.0+ | Under testing |

---

## Copyright

© 2026 Florentin Arno — see [LICENSE](LICENSE).
