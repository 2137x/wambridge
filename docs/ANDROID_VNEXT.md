# Android vNext roadmap

This is the maintained backlog for the next WAM Bridge Android improvements after the
first in-app speaker controls and fresh-install fixes.

## 1. Home / Now Playing

**Foundation shipped 2026-09-22; TuneIn artwork + ICY metadata shipped 2026-09-23.** Home is
the daily-driver surface with shared Now Playing state, direct controls and a stateful DLNA
renderer toggle. Radio owns playback navigation: the three physical M5 Radio-button slots are
shown explicitly first, followed by the remaining TuneIn presets and saved TuneIn/direct/fallback
stations. Notifications, MediaSession and widgets deep-link back to that same Radio tab instead
of separate playback activities. Native TuneIn thumbnails and saved TuneIn IDs use the shared
artwork-card renderer and bounded artwork cache. Settings no longer duplicates DLNA start/stop;
Home is the single in-app DLNA control surface.

Turn the main screen into a daily-driver view instead of a service panel.

- direct-radio station logo/artwork when a trusted source is available
- source / station / current playback state
- M5 state
- play / pause, mute, volume and Stop
- clear DLNA / Radio state
- move speaker IP, manual discovery and low-level service controls to Settings / Diagnostics

## 2. Android MediaSession

**Shipped 2026-09-22 for radio.** RadioService owns the Android MediaSession and MediaStyle
notification token; DLNA remains owned by the external player.

Use a proper MediaSession for radio playback so controls can appear naturally in Android:

- notification shade
- lock screen / system media controls
- compatible headset and Bluetooth media buttons

DLNA playback should remain owned by the external player that started it, rather than
having two apps fight over the speaker.

## 3. Sleep Timer / Standby

**Software implemented 2026-09-22; Standby now hardware-validated 2026-09-23.** Android exposes
15/30/45/60/Off, reads speaker timer state, and routes commands through the current radio/renderer
owner. On the rolling release, Standby now released app-owned playback, armed the one-second
speaker timer and the M5 front lamp went dark. Timed 15/30/45/60 presets still need a longer
duration/readback pass before that whole path is marked fully hardware-validated.

Expose the measured M5 sleep path through normal UI.

- 15 / 30 / 45 / 60 minute presets
- immediate standby
- clear current timer state where possible
- clean interaction with radio and renderer teardown

## 4. Physical Radio preset manager

Manage the presets cycled by the M5's physical Radio button.

- show physical preset slots
- select a station for a slot
- sync to the speaker
- validate the write-side preset commands safely before shipping the editing UI

## 5. Radio favourites 2.0

**Shipped 2026-09-23.** Android now uses the shared `station_packs.json` favourites pack as
its default library and layers user pin/order/default/recent state on top. The station manager
also supports duplicate/edit plus JSON/M3U/PLS import/export.

Make saved stations pleasant to live with.

- pinning and manual ordering
- recently played
- default station / Play last
- duplicate and edit helpers
- import / export in useful formats such as M3U, PLS and JSON
- clear TuneIn / direct / fallback indicators

## 6. Smarter fallback routing

**Shipped 2026-09-23.** The mobile relay passively remembers successful/failed endpoints,
prefers the last working endpoint on later starts, cools failed URLs for 15 minutes, and
publishes the active fallback position through shared runtime state. It never opens a second
probe beside playback.

Improve radio recovery without adding a second probing client to the active stream.

- remember the last working endpoint
- temporarily de-prioritize recently failed URLs
- surface which fallback is active
- keep ordered fallback behavior deterministic

## 7. ICY metadata pipeline

**Expanded 2026-09-23; physical metadata pass pending.** Direct radio requests ICY metadata on
the existing upstream connection, strips metadata blocks before they reach the M5, and publishes
`StreamTitle` plus image-like `StreamUrl` artwork through the shared speaker snapshot. Radio
Paradise has a lightweight provider for its now-playing JSON API, including per-track cover art
and the endpoint's remaining-track refresh hint. The audio stream is still observed only once.

Extract metadata such as `StreamTitle` from direct radio and keep one shared playback
state that can feed:

- Home / Now Playing
- widgets
- MediaSession / notifications

## 8. Phone-side HLS / Ogg support

**Merged in PR #187; first physical M5 pass 2026-09-28.** The isolated Media3 path decodes
HLS/Ogg/Opus, normalizes to PCM16 stereo 44.1 kHz and serves endless WAV to the speaker while
MP3/AAC/FLAC stay on the lightweight direct relay. The transcoder stays bound to the selected
Wi-Fi network and feeds Media3 title/artist/web artwork into the shared Now Playing state. BBC 6
Music and Fallout FM 5 are exposed to Android because their HLS/Ogg formats now have a software
path.

Passed on the M5: BBC Radio 1 (HLS) and Trójka (Ogg) play; Ogg `StreamTitle` reaches Now
Playing; HLS -> Ogg switch without wedging; Stop during playback then restart; Czwórka (direct
Shoutcast MP3) unchanged; Diagnostics stays on the selected Wi-Fi.

Still to check on hardware:

- [ ] Stop during transcoder startup (start BBC1, Stop from the notification within 1-2 s),
  then start another station.
- [ ] Each transcoded station stable for at least 60 s (only Trójka was listened to at length).
- [ ] Volume on station start: confirm it lifts from 0 to the safe step 3 once the M5 pulls
  audio and never stays at 0. Decide whether a station switch while already playing should
  keep the current volume instead of re-running the safe start (existing `main` behaviour).
- [ ] BBC HLS shows no track title (`Now playing: None`); confirm the stream carries no timed
  metadata rather than it being dropped.

## 9. Quick actions everywhere

**Shipped 2026-09-23.** Runtime launcher shortcuts use the shared `station_packs.json`
`top3` pack plus Stop/Standby up to the device launcher limit. A second Quick Settings tile
starts last/default radio when idle and cycles the same top3 pack while radio is active.
A configurable 1×1 Station widget selects one top3 station per widget instance and starts it
with one tap.

Make common actions reachable without opening the app.

- launcher shortcuts for favourite stations, Stop and Standby
- optional Radio Quick Settings tile
- reuse the same command/state routing as the app instead of duplicating control logic

## 10. Human-friendly Diagnostics

**Shipped 2026-09-22.** Diagnostics now exposes the shared speaker/network/runtime state,
copyable text output and controlled Fix connection recovery; manual IP moved under Advanced.

Keep protocol archaeology out of the normal UI while still making failures explainable.

- speaker ID and IP
- Wi-Fi / Android Network binding
- current M5 owner: radio, renderer or idle
- active stream and fallback
- last error
- app version
- Copy diagnostics
- controlled Fix connection flow: stop -> rediscover -> probe -> refresh

## Suggested order

Most of the daily-driver layer is shipped. The remaining sequence is deliberately hardware-led:

1. finish the remaining HLS/Ogg hardware checks from section 8: startup Stop, 60 s stability,
   safe-start volume and BBC timed-metadata verification;
2. finish the 15/30/45/60-minute sleep-timer duration/readback pass;
3. validate preset write commands before exposing physical preset editing on Android.

Diagnostics should grow alongside those checks rather than becoming a separate second control
stack.

The goal is to keep WAM Bridge small, local and useful: no account system, cloud backend or
framework migration unless a concrete feature eventually requires one.
