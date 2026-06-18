# PD2026 App — Claude Project Memory

Festival app for **Punkáči deťom 2026** (punk festival for children, Slovakia).
Package: `sk.punkacidetom.pd2026`  
Repo: `davidbadin/davidbadin.github.io` (mono-repo — app lives in `PD2026_app/`)

---

## ⚠️ Hard Constraints — Never Break These

### Files that must NEVER be committed
| File | Why |
|---|---|
| `local.properties` | Holds `SPOTIFY_CLIENT_ID`, `SPOTIFY_REDIRECT_URI`, `SHEET_ID`, `SHEET_GID` |
| `app/google-services.json` | Real Firebase config — only `google-services.json.placeholder` is committed |
| `keystore.properties` | Play Store signing passwords |
| `pd2026-upload.jks` / `*.keystore` / `*.jks` | Signing keystores |
| `libs/spotify-app-remote-release-*.aar` | Proprietary Spotify SDK (download manually) |
| `secrets.properties` | Any other secrets |

All of the above are already in `.gitignore`. Double-check before every commit with `git status`.

### Source of truth
`app_tech_specifications.md` wins over `guide.md` in any conflict. When in doubt, read the spec file first.

### Never translate
Festival name ("Punkáči deťom"), stage names, and raw band data from the spreadsheet are never put through the translation/i18n system.

---

## Module Architecture

```
PD2026_app/
├── app/                        # :app — wires everything; MainActivity, NavHost, Hilt AppModule
├── core/
│   ├── core-model/             # :core:core-model — data classes (Band, FestivalInfo, …), no Android deps
│   ├── core-ui/                # :core:core-ui — theme, colours, spacing, FaIcon, shared composables
│   ├── core-data/              # :core:core-data — BandRepository, CsvSheetFetcher, XlsxAssetReader, DataStore
│   ├── core-i18n/              # :core:core-i18n — language preference (SK/EN), LocaleHelper
│   └── core-notifications/     # :core:core-notifications — FCM push notification handling
└── feature/
    ├── feature-home/           # Festival countdown / landing screen
    ├── feature-timetable/      # Day-by-day timetable grid (favourite filter)
    ├── feature-bands/          # Band list + BandDetailScreen
    ├── feature-news/           # News feed (HTML from assets / GitHub Pages)
    ├── feature-info/           # Info page (HTML, offline-first — see below)
    ├── feature-tickets/        # Tickets WebView
    ├── feature-settings/       # Language toggle, notification prefs
    └── feature-spotify/        # Spotify App Remote integration + embedded player
```

Navigation routes live in `app/src/main/kotlin/…/navigation/`. Bottom nav tabs: Home, Timetable, Spotify, Settings. Info and other screens are reachable from within tabs.

---

## UI / Theme Conventions

### Colours (`core-ui/theme/Color.kt`)
```kotlin
Navy        = #10133D   // primary background / large surfaces
NavyDark    = #070D25   // bottom nav bar, slightly darker
NavyLight   = #1A2550   // elevated card surface
Crimson     = #AD1C24   // accent: active icons, buttons, dividers
CrimsonDark = #8B1620
White       = #FFFFFF   // primary text on Navy
WhiteAlpha60 = 0x99FFFFFF  // secondary / muted text
WhiteAlpha30 = 0x4DFFFFFF
```

### Fonts
Display / headings → **BebasNeue** (`core-ui` / `--f-mid` in HTML)  
Body → **Poppins** (`core-ui` / `--f-body` in HTML)  
Both TTF files are bundled in `app/src/main/assets/fonts/` for the offline HTML page.

### Icons
FontAwesome via `FaIcon(name = "…", family = FaFamily.Solid|Brands, size = spacing.iconLg, tint = …)`.  
Material Icons used only where no FA equivalent exists (e.g. Settings, Favourite).

### Spacing (`LocalAppSpacing`)
Use `LocalAppSpacing.current` — never hard-code dp values inline.

### Modifier ordering rule
```kotlin
// CORRECT — content area equals full bottomNavHeight
.fillMaxWidth()
.navigationBarsPadding()   // ← must come BEFORE height()
.height(spacing.bottomNavHeight)

// WRONG — nav bar inset eats into the height; icons get clipped
.height(spacing.bottomNavHeight)
.navigationBarsPadding()
```

---

## Data Flow — Band Data

1. **App start** → `BandRepositoryImpl.init` loads DataStore cache immediately, then fires `backgroundRefreshIfStale(ignoreCooldown = true)`
2. **Network fetch** → `CsvSheetFetcher` pulls Google Sheets CSV → `CsvParser` → `BandMapper`
3. **Network failure + no cache** → `XlsxAssetReader` reads `app/src/main/assets/PD2026_program_test_data.xlsx` (bundled fallback, no external library — uses `ZipInputStream` + Android `XmlPullParser`)
4. **Refresh cooldown** → 30 minutes (skipped on app start; enforced on resume)
5. **Favourites** → stored in DataStore as `Set<Long>` (band IDs)

### XLSX reader notes (`core-data/remote/XlsxAssetReader.kt`)
- Asset name: `PD2026_program_test_data.xlsx`
- Parses: `xl/sharedStrings.xml`, `xl/styles.xml`, `xl/worksheets/sheet1.xml`
- Excel date serial → `LocalDate`: `LocalDate.ofEpochDay(serial.toLong() - 25569L)`
- Excel time serial → `"H:mm:ss"`: `((serial % 1.0) * 86400.0).roundToInt()`
- Built-in date format IDs: `{14,15,16,17,22}`; time: `{18,19,20,21}`
- Do NOT add Apache POI — it caused KSP `error.NonExistentClass` failures

---

## Spotify Integration

### SDK setup (local dev)
1. Download `spotify-app-remote-release-0.8.0.aar` from Spotify developer portal
2. Place in `PD2026_app/libs/`
3. The file is gitignored — never commit it

### Gradle split (required by AGP)
AGP forbids local `.aar` as `implementation` in a library module (breaks `bundleDebugAar`).
```kotlin
// feature/feature-spotify/build.gradle.kts
compileOnly(files(spotifyAar))   // compile-time only

// app/build.gradle.kts
implementation(files(spotifyAar)) // included in final APK
```

### CI stub
`scripts/create-spotify-stub.py` generates a minimal stub AAR so CI can compile without the real SDK.
It is run as a CI step **before** `gradle test`.
Stubs cover: `SpotifyAppRemote`, `ConnectionParams`, `Connector.ConnectionListener`, `PlayerApi` (`play`, `resume`, `pause`, `skipNext`, `skipPrevious`, `subscribeToPlayerState`), `PlayerState`, `Track`, `Artist`, `Subscription<T>`, `CallResult<T>`.

If you add a new `PlayerApi` method call in `SpotifyViewModel`, also add the stub method to `create-spotify-stub.py`.

---

## Info Screen — Offline-First HTML

The Info tab displays an HTML page using a `WebView` with stale-while-revalidate caching.

### Content priority
1. `context.filesDir/info_cache.html` (previously fetched network version) → shown first if exists
2. `app/src/main/assets/info.html` (bundled fallback) → used on first launch when offline
3. Background fetch of `https://davidbadin.github.io/PD2026_app/info.html` → updates cache

### Base URL split (critical)
`InfoViewModel` returns `InfoContent(html: String, baseUrl: String)`:
- **Asset HTML** → `baseUrl = "file:///android_asset/"` — relative paths resolve locally
- **Cached/network HTML** → `baseUrl = "https://davidbadin.github.io/PD2026_app/"` — relative paths resolve to GitHub Pages

`InfoScreen` calls `wv.loadDataWithBaseURL(info.baseUrl, info.html, …)`.

Do NOT hard-code the HTTPS base URL for all cases — asset HTML resources (logo, fonts) would fail to load when offline.

### assets/info.html rules
- Must be **100% self-contained** (zero external URLs)
- Logo: `<img src="logo_pd.png">` — resolves to `file:///android_asset/logo_pd.png`
- Fonts via `@font-face { src: url("fonts/bebas_neue_regular.ttf") }` — resolves locally
- No `<link rel="stylesheet" href="https://fonts.googleapis.com/…">` — requires internet

---

## Band Detail — Missing Image Fallback

When a band has no photo (`bandImagePngUrl` is blank), `BandHeaderImage` shows `logo_pd.png` centred on a Navy background:
```kotlin
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data("file:///android_asset/logo_pd.png")
        .crossfade(true).build(),
    contentScale = ContentScale.Fit,
    modifier = Modifier.fillMaxWidth(0.55f),
)
```
Do not replace this with an initial letter or a generic placeholder icon.

---

## CI / GitHub Actions

Workflow: `.github/workflows/android.yml` (repo root, not inside `PD2026_app/`)  
Triggers on push/PR to `master` for paths under `PD2026_app/**`.

### CI steps (in order)
1. Checkout
2. JDK 17 (temurin)
3. Gradle 8.13 (`gradle/actions/setup-gradle@v4`)
4. Create `local.properties` (sheet IDs + Spotify placeholders)
5. Copy `google-services.json.placeholder` → `google-services.json`
6. **`python3 scripts/create-spotify-stub.py`** ← must run before Gradle
7. `gradle test --no-daemon`
8. `gradle assembleDebug --no-daemon`
9. Upload APK artifact (7-day retention)

### Watching CI
```bash
gh run watch $(gh run list --branch <branch> --limit 1 --json databaseId -q '.[0].databaseId') --exit-status
```

### Build output location (Windows dev)
Gradle build dirs are redirected to `C:\Users\FS0605\AndroidBuild\PD2026\` to avoid OneDrive/Dropbox file-locking. Configured in `settings.gradle.kts`.

---

## Git Workflow

- Branch naming: `fix/batch-XX`, `fix/<short-description>`, `feature/<name>`
- One PR per logical batch of fixes
- Always check `git status` before committing — never stage `local.properties`, `google-services.json`, or any `.jks`/`.aar`
- Merge strategy: standard merge commit via `gh pr merge <number> --merge`
- After merging a PR, create a new branch from `master` for the next batch — don't keep committing onto a merged branch

---

## Current State (as of 2026-06-18)

| Item | Value |
|---|---|
| `versionCode` | 4 |
| `versionName` | 1.0.0 |
| `minSdk` | 24 |
| `targetSdk` / `compileSdk` | 35 |
| Last merged PR | #54 — offline info HTML fix |
| Active branch | `master` |

### Key decisions already made — don't revisit without good reason
- Apache POI removed; XLSX parsed with built-in `ZipInputStream` + `XmlPullParser`
- Spotify SDK: `compileOnly` in library, `implementation` in `:app`
- Bottom nav modifier order: `navigationBarsPadding()` before `height()`
- Info screen base URL is dynamic per content source (not hard-coded)
- Band image fallback is `logo_pd.png`, not an initial letter
