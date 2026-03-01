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

## Download

<table>
  <tr>
    <td align="center">
      <strong>iOS — App Store</strong><br><br>
      <!-- Replace the href and data= URL below with the real App Store link -->
      <a href="https://apps.apple.com/app/letzlisten">
        <img src="https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=https://apps.apple.com/app/letzlisten" alt="App Store QR code" width="150" height="150"/>
      </a><br><br>
      <a href="https://apps.apple.com/app/letzlisten">Download on the App Store</a>
    </td>
    <td align="center">
      <strong>Android — Google Play</strong><br><br>
      <!-- Replace the href and data= URL below with the real Google Play link -->
      <a href="https://play.google.com/store/apps/details?id=com.letzlisten">
        <img src="https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=https://play.google.com/store/apps/details?id=com.letzlisten" alt="Google Play QR code" width="150" height="150"/>
      </a><br><br>
      <a href="https://play.google.com/store/apps/details?id=com.letzlisten">Get it on Google Play</a> _(coming soon)_
    </td>
  </tr>
</table>

> **Note:** Links above are placeholders. Update the `href` and `data=` parameters once the apps are live on the stores.

---

## Copyright

© 2026 Florentin Arno — see [LICENSE](LICENSE).
