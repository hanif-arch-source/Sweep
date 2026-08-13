# Sweep — Android Network Monitoring App

Sweep gives you a clear, real-time view of the network your Android device
is connected to: Wi-Fi status, signal strength, upload/download traffic,
discovered local devices, and alerts for important network events.

Built with **Kotlin + Jetpack Compose + MVVM**, following the architecture
and design in `Sweep_Project_Document.docx`.

## What's implemented

- Wi-Fi/network status (connected/disconnected, SSID, local IP, gateway)
- Signal strength (dBm + 4-bar indicator) and link speed
- Upload/download traffic monitoring with a live rolling graph
- "Scan Network" — local subnet device discovery
- Discovered device list with online/offline status
- Device details screen (IP, MAC, hostname, manufacturer, last seen)
- Manual refresh + automatic refresh on a configurable interval
- Alerts: new device, device offline, weak signal, disconnect, high traffic
- Android notifications for alerts (respecting per-event settings)
- Settings screen: auto-update toggle/interval, auto-scan, alert
  thresholds, per-event notification toggles
- Clean red-and-white Material 3 theme matching the spec's visual identity

## Project structure

```
com.sweep.networkmonitor
├── data
│   ├── model            Network, Device, TrafficStats, Alert, NetworkEvent
│   └── repository       NetworkRepository
├── monitoring            NetworkMonitor, TrafficMonitor
├── scanner                NetworkScanner
├── notifications          NotificationHelper
├── viewmodel              NetworkViewModel, NetworkViewModelFactory
├── ui
│   ├── theme              Color.kt, Type.kt, Theme.kt
│   ├── components          Shared cards, signal bars, status pills
│   ├── dashboard           DashboardScreen, TrafficGraph
│   ├── devices             DevicesScreen, DeviceDetailsScreen
│   ├── alerts              AlertsScreen
│   ├── settings             SettingsScreen
│   └── navigation           SweepNavHost (bottom nav + NavHost)
└── MainActivity.kt
```

This mirrors the structure recommended in Section 6.1 of the project
document.

## Opening the project

1. Open **Android Studio** (Koala or newer recommended).
2. **File → Open** and select this `Sweep/` folder.
3. If Android Studio prompts to create a Gradle wrapper, accept — this
   repo ships `gradle-wrapper.properties` (Gradle 8.7) but not the wrapper
   jar itself, so the IDE (or `gradle wrapper` from the CLI if you have
   Gradle installed) will generate it on first sync.
4. Copy `local.properties.example` to `local.properties` and set
   `sdk.dir` to your Android SDK path (Android Studio usually does this
   automatically on first sync).
5. Let Gradle sync, then Run on an emulator or a real device
   (**minSdk 26 / Android 8.0+**).

> The project doc recommends testing on a real device rather than relying
> entirely on the emulator  — local network scanning behaves
> more realistically on real Wi-Fi.

## Permissions

Sweep requests only what the implemented features need:

- `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE`, `INTERNET` — network status
- `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` — required by Android
  to read the current SSID and to perform local network discovery on many
  OS versions/OEMs
- `POST_NOTIFICATIONS` (Android 13+) — alert notifications

If a permission is denied, Sweep degrades gracefully (e.g. SSID or scan
results may be unavailable) rather than crashing — see Section 14 of the
project document.

## Known limitations 

- Sweep is a **device-side** monitor, not a router admin replacement. It
  cannot see other devices' bandwidth usage — only their presence via
  local network discovery.
- MAC address / manufacturer lookups depend on the device's ARP cache
  (`/proc/net/arp`) being populated and accessible, which varies by
  Android version and OEM. When unavailable, these fields show as
  "Unavailable" rather than being guessed.
- `NetworkScanner`'s manufacturer field is left `null` — an OUI
  (MAC-prefix → vendor) lookup is called out as a future enhancement in
  Section 17 of the spec, not part of the MVP.

## Suggested next steps 

- Router integration for network-wide traffic stats
- Historical/daily/weekly traffic charts
- Device naming, custom icons, OUI-based manufacturer lookup
- Exportable network reports
- Home-screen widgets
