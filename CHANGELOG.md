# Changelog

## Version 1.3 — incoming

### iOS

- **Generation Dance** — new station added with HD stream option; dedicated theme.
- **Per-station theme** — theme can be automatically linked to the selected station via a new toggle in Settings
- **Advanced panel** — floating side panel showing real-time stream info and an event log; visible tab to open it
- **SD / HD** — stream quality selector available from the advanced panel, also visible in landscape mode even when no track is playing

### Android *(waiting google approval)*
- **iOS feature parity** — theme system, advanced mode, custom stations and 9 languages ported from iOS
- **Station logos** — full favicon → bundled assets cascade aligned with iOS
- **Bug fixes** — logo cropping, delete button, theme switching, playback resume

## Version 1.2 — 2026-03-14

### iOS
- **Favourites & Radio menus** — replaced native List with ScrollView + custom cards to match the Settings visual style; per-row trash button replaces swipe-to-delete
- **iPad panels** — favourites panel and station sidebar rows now share the same card style as Settings
- **Settings** — "Clear all" button visible but disabled (greyed out) when no favourites, consistent with Export button
- **Top buttons** — Settings and Done buttons repositioned to the right; station selector button moved to centre

---

## Version 1.1 — 2026-03-01

### iOS
- **iPad: animated split panels** — station sidebar and favourites panel now slide in/out independently instead of pushing the layout
- **iPad: favourites panel** — removed panel title, clear-all button right-aligned, corrected top padding on overlay buttons
- **iPad: share button** — fades out when station panel is open to avoid overlap; bottom divider repositioned
- **Bottom bar** — restored original AirPlay → Play/Stop → Share order after layout experiments
- **Share button** — disabled while playback is paused, re-enabled when playing
- **Playback fix** — isPlaying is now set to false synchronously on pause, preventing a brief stale-state window

### Android *(initial public build)*
- **Player UI** — full redesign to match iOS: language button, heart in content area, two-button control bar (Share · Play/Stop)
- **ICY metadata** — real-time artist & title extracted from Shoutcast/Icecast in-stream metadata
- **Album artwork** — fetched from iTunes Search API, same logic as iOS
- **Station logos** — bundled assets for known stations with Google Favicon / Facebook Graph API fallback cascade
- **Favourites & share** — save tracks, share now-playing info in the same format as iOS
- **Multilingual UI** — Lëtzebuergesch · Français · Deutsch · English (Portuguese removed after review)
- **No auto-play on startup** — stream starts only when the user taps Play
- **HTTP streams** — network security config allows cleartext where required

---

## Version 1.0 — 2026-02-28
- Initial release — iOS app published; Android project scaffolded (internal testing)
