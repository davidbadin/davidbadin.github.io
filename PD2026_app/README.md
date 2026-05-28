# Punkáči deťom 2026 — Android App

Official Android app for **Punkáči deťom 2026** (also known as Punkáči deťom 15).

- Min SDK 24 (Android 7.0) · Target SDK 35 (Android 15)  
- Kotlin 2.x · Jetpack Compose · Material 3 · Hilt · Navigation-Compose

---

## Quick start

### 1. Prerequisites

| Tool | Version |
|------|---------|
| Android Studio | Hedgehog or newer |
| JDK | 17 |
| Android SDK | 35 |

### 2. Clone and open

```bash
git clone https://github.com/davidbadin/davidbadin.github.io
cd davidbadin.github.io/PD2026_app
```

Open the `PD2026_app/` directory in Android Studio.

### 3. Create `local.properties`

Copy the placeholder and fill in real values:

```bash
cp local.properties.placeholder local.properties
```

Edit `local.properties`:

```properties
# Android SDK path (auto-set by Android Studio)
# sdk.dir=/path/to/your/Android/sdk

# Google Sheet — already has the real values as defaults
SHEET_ID=1ClI4BqoEIWRudAnckKxY8hBg768NCVuD6YF-m6EVm24
SHEET_GID=968980279

# Spotify Developer App — register at developer.spotify.com
SPOTIFY_CLIENT_ID=your_client_id_here
SPOTIFY_REDIRECT_URI=pd2026://callback
```

### 4. Set up Firebase (push notifications)

1. Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com).
2. Add an Android app with package `sk.punkacidetom.pd2026`.
3. Download `google-services.json` and place it in `app/`.

> The committed `app/google-services.json.placeholder` is a stub that lets the project compile. Replace it before building a release.

### 5. Spotify SDK (optional)

The Spotify embed player works without the native SDK (falls back to a WebView iframe). For the full native SDK experience:

1. Download the Spotify Android SDK from the [Spotify developer portal](https://developer.spotify.com/documentation/android/).
2. Copy `spotify-app-remote-release-*.aar` into `libs/`.

See [`libs/README.md`](libs/README.md) for details.

### 6. Build and run

```bash
./gradlew assembleDebug
# or use Android Studio's Run button
```

---

## Module structure

```
PD2026_app/
├── app/                    # Activity, NavHost, Hilt wiring
├── core/
│   ├── core-model/         # Domain models + nav routes
│   ├── core-ui/            # Theme, typography, shared composables, FA icons
│   ├── core-data/          # Sheet fetcher, CSV parser, SWR cache
│   ├── core-i18n/          # Locale switching (AppCompatDelegate)
│   └── core-notifications/ # FCM MessagingService
└── feature/
    ├── feature-home/       # Countdown / thank-you / nav grid / Spotify player
    ├── feature-timetable/  # Day-tab two-column timetable
    ├── feature-bands/      # Sorted band list + detail with image
    ├── feature-news/       # Facebook Page Plugin (WebView)
    ├── feature-info/       # Info HTML (SWR WebView)
    ├── feature-tickets/    # External ticket links (Chrome Custom Tabs)
    ├── feature-settings/   # Language / font size / data update
    └── feature-spotify/    # Festival playlist player
```

**Removing a feature:** delete the `include()` line in `settings.gradle.kts` and the corresponding `*navGraph(navController)` call in `app/src/main/kotlin/.../navigation/AppNavHost.kt`. The rest of the app continues to build.

---

## Data source

Band data comes from a public Google Sheet as a CSV export — no API key required.

- Sheet ID: `1ClI4BqoEIWRudAnckKxY8hBg768NCVuD6YF-m6EVm24`  
- Tab (gid): `968980279`
- CSV endpoint: `https://docs.google.com/spreadsheets/d/{SHEET_ID}/export?format=csv&gid={SHEET_GID}`

The app uses **stale-while-revalidate** caching: cached data is displayed immediately, then refreshed in the background (30-minute cooldown on automatic refresh; manual refresh in Settings ignores the cooldown).

---

## Localization

| Locale | Default |
|--------|---------|
| Slovak (`sk`) | Yes |
| English (`en`) | No — switch in Settings |

Festival name, stage names, and band data are **never** translated.

---

## Push notifications

Topic: `pd2026_all` (FCM). Slovak-only — a single text is broadcast to all devices regardless of in-app language. Subscribe/unsubscribe is automatic on app start.

---

## Fonts

Custom fonts live in `core/core-ui/src/main/res/font/`. They are **not** included in the repo (licensing). Copy from `fonts/` in the project root:

| File | Usage |
|------|-------|
| `third_man_regular.otf` | Display headings |
| `bebas_neue_regular.ttf` | Mid-size headings |
| `poppins_regular.ttf` | Body text |
| `fa_regular_400.otf` | Font Awesome icons (Regular) |
| `fa_brands_400.otf` | Font Awesome icons (Brands) |

---

## CI

GitHub Actions workflow: [`.github/workflows/android.yml`](../.github/workflows/android.yml)

Triggers on push/PR to `master` touching `PD2026_app/**`. Runs unit tests and builds a debug APK. Artifact is retained for 7 days.

---

## Secrets that must never be committed

| File | Contains |
|------|----------|
| `local.properties` | SDK path, Sheet IDs, Spotify credentials |
| `keystore.properties` | Release keystore path + passwords |
| `*.jks` / `*.keystore` | Release keystore file |
| `app/google-services.json` | Firebase config |

All listed in `.gitignore`.
