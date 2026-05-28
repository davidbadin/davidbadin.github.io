# Technical Specification — Punkáči deťom 15 (PD2026) Android App

> This document is the single source of truth for the technical design of the
> official Android app for the **Punkáči deťom 2026** music festival
> (also referred to as **Punkáči deťom 15**).
> It is written to be consumed directly by Claude Code as build input.

---

## 1. Project metadata

| Item | Value |
| --- | --- |
| Festival name (general) | `Punkáči deťom` |
| Festival name (year-specific) | `Punkáči deťom 2026` (a.k.a. `Punkáči deťom 15`) |
| App display name | `Punkáči deťom 2026` |
| Application ID (package) | `sk.punkacidetom.pd2026` |
| Project folder | `PD2026_app/` inside the `davidbadin.github.io` Git repo |
| Repo URL | https://github.com/davidbadin/davidbadin.github.io |
| Min SDK | **24** (Android 7.0) |
| Target / Compile SDK | **35** (Android 15) |
| JDK | 17 |
| Kotlin | latest stable (2.x) |
| UI framework | **Jetpack Compose** (Material 3) |
| Build system | Gradle (Kotlin DSL, `build.gradle.kts`) |
| Architecture | MVVM + Repository, single-Activity, modular by feature |

**Do not translate** the festival name, the stage names, or any data coming
from the Google Sheet — only translate static app UI strings.

All dates and times **must** be displayed in Central-European format, e.g.
`26. 5. 2026, 13:00` (day-first, 24-hour clock, leading zero **not** required
on the day/month component, space-separated). Use Slovak locale `sk-SK` as
the default and English locale `en-US`/`en-GB` (24-hour) for EN.

---

## 2. App architecture & modules

The app is split into **feature modules** so any section can be added or
removed without touching the rest. Each feature module owns its UI, its
ViewModel(s), its navigation route, and is registered with the bottom-nav /
home grid through a small `FeatureRegistry`.

Suggested Gradle module layout:

```
PD2026_app/
├── app/                       # Application module (Activity, NavHost, DI wiring)
├── core/
│   ├── core-ui/               # Theme, typography, shared composables, icons
│   ├── core-data/             # Sheet fetcher, cache, stale-while-revalidate
│   ├── core-model/            # Domain models (Band, Slot, Stage, FestivalDay…)
│   ├── core-i18n/             # String loaders, locale switch
│   └── core-notifications/    # FCM integration
└── feature/
    ├── feature-home/
    ├── feature-timetable/
    ├── feature-bands/         # list + detail
    ├── feature-news/
    ├── feature-info/          # WebView-based
    ├── feature-tickets/
    ├── feature-settings/
    └── feature-spotify/       # shared Spotify components
```

Removing a feature module = removing its line from `settings.gradle.kts`,
its entry in `FeatureRegistry`, and its nav route. The rest of the app
must continue to build and run.

Dependency injection: **Hilt**.
Async: **Kotlin Coroutines + Flow**.
Navigation: **Navigation-Compose** with type-safe routes.

---

## 3. Data layer

### 3.1 Data source

The data lives in a Google Sheet shared as "Anyone with the link →
Viewer". The app reads **only the `PD2026` tab**. 

- **Spreadsheet (shared edit URL, human-friendly):**
  `https://docs.google.com/spreadsheets/d/1ClI4BqoEIWRudAnckKxY8hBg768NCVuD6YF-m6EVm24/edit?usp=sharing`
- **Spreadsheet ID:** `1ClI4BqoEIWRudAnckKxY8hBg768NCVuD6YF-m6EVm24`
- **PD2026 tab `gid`:** `968980279`
- **CSV export endpoint used by the app:**

  ```
  https://docs.google.com/spreadsheets/d/1ClI4BqoEIWRudAnckKxY8hBg768NCVuD6YF-m6EVm24/export?format=csv&gid=968980279
  ```

  This works because the sheet is shared "Anyone with the link". No
  auth, no API key.

Parsing: use a small CSV parser (e.g. `com.opencsv:opencsv` or a pure
Kotlin implementation). Treat the tab as a typed `List<Row>` mapped
into the domain model `Band`.

### 3.2 Sheet schema (PD2026 tab — verified against live sheet)

Header row is row 1. Columns observed in the live sheet:

| Col | Sheet header | Type | Used by | Notes |
| --- | --- | --- | --- | --- |
| A | `ID` | int | internal | Primary key. A **blank `ID` means "ignore this row"** — the row is skipped entirely by the parser, does not appear in Bands / Timetable / search, and produces no warning. This is the supported way to temporarily disable a row in the sheet without deleting it (e.g. a band that pulled out late, a placeholder slot under negotiation). Numeric IDs are otherwise unique and stable. If `ID` repeats by accident, use only the first one and treat the others as blank `ID` (see above).|
| B | `START_DATE` | date `D.M.YYYY` | Timetable | Slovak-style date with dots and **no leading zeros** (e.g. `28.8.2025`). |
| C | `START_TIME` | time `H:mm:ss` | Timetable | 24-hour, includes seconds. |
| D | `END_DATE` | date `D.M.YYYY` | Timetable | |
| E | `END_TIME` | time `H:mm:ss` | Timetable | |
| F | `BAND` | string | Bands, Timetable, BandDetail | Band/act name. Do **not** translate. May be a non-music act (documentary screening, stand-up, talkshow, slam, after-party DJ). |
| G | `DESCRIPTION` | string | BandDetail | Free-form Slovak text, may include emoji and hashtags. May be empty. |
| H | `STAGE` | string `A` / `B` | Timetable, BandDetail | **Code, not full name.** See §6 for the A/B → full-name mapping. |
| I | `SPOTIFY_URL` | string | BandDetail | Just the Spotify artist ID. Empty for non-music acts. |
| J | `GENRE` | string | Bands, Timetable | Free text (e.g. `punk rock`, `ska punk`, `punkrockový dokument`). |
| K | `SORTING_PRIORITY` | int / empty | Bands | Lowest value first; empty / non-numeric → bottom of the Bands list. |
| L | `DESCRIPTION_EN` | string | BandDetail | Free-form English description of the act, may include emoji and hashtags. Falls back to `DESCRIPTION` (Slovak) if empty. |
| M | `IMAGE_NAME` | string | BandDetail | Filename of the band's photo, e.g. `tamgasti-cz.jpg`. The app fetches it from `https://davidbadin.github.io/PD2026_app/bands/<IMAGE_NAME>`. If the column is empty or the image cannot be loaded, display the app logo instead. Accepted formats: `.png` or `.jpg`; if both exist for the same band, prefer `.png`. |

Notes for Claude Code:

- **Band images are not available yet.** Only one example image exists at this time: `https://davidbadin.github.io/PD2026_app/pd_resources/bands/tamgasti-cz.jpg`. Copy this file into the `bands/` image folder of the app project (the folder that will serve as the base path for all `IMAGE_NAME` values). The remaining band images will be added to that same folder before the app is published — no code changes will be needed at that point.

### 3.3 Caching — stale-while-revalidate

- Persist last fetched data and last-fetched timestamp locally
  (Claude Code may choose Room, DataStore, or a JSON-on-disk cache).
- On read: **return cached data immediately**, then trigger a background
  refresh under the rules below; emit fresh data to the UI when ready.
- Triggers for a background refresh:
  1. **App start** — always.
  2. **App resume from background** — only if last successful fetch was
     more than **30 minutes** ago.
  3. **Manual** — via the Settings screen "Update data" button.
     Manual fetch ignores the 30-minute cooldown.
- On fetch failure: keep showing cached data, show a small inline error
  (toast or non-blocking snackbar, never block the UI) - only for manual refresh; no error in other cases.

### 3.4 Derived values (recomputed on every fetch)

- **Festival start** = chronologically earliest `START_DATE` + `START_TIME`
  across all rows in `PD2026`.
- **Festival end** = chronologically latest `END_DATE` + `END_TIME`
  across all rows in `PD2026`.
- **Festival day** = the 24-hour window starting at `06:00` local time;
  a band playing at `03:00` on Saturday belongs to the **Friday** festival
  day. Festival days are derived from the data, **not** hard-coded.

All derivation logic lives in `core-data` and is unit-tested.

---

## 4. Navigation & screen map

Single Activity, `NavHost` with the following routes:

```
home          → Home screen (default before/after festival)
timetable     → Timetable (default during festival)
bands         → Band list
bands/{id}    → Band detail
news          → News (Facebook feed — see §11.4)
info          → Info (WebView)
tickets       → Tickets & Eshop
settings      → Settings
```

Bottom bar (`Footer`, always visible):

1. **HOME** — `home`
2. **TIMETABLE** — `timetable`
3. **SPOTIFY** — opens the festival playlist (in-app player screen
   `feature-spotify`)
4. **SETTINGS** — `settings`

Each footer button has an icon **and** a text label under it. The Spotify
icon may use `pd_resources/spotify32.png` as a starting point.

### 4.1 Default screen logic

On cold start the app picks the default screen by current time vs. the
derived festival start/end:

- Before festival → `home` (with "Countdown" block)
- During festival → `timetable`
- After festival  → `home` (with "Thank you" block)

### 4.2 Dynamic Header (now-playing)

A composable `NowPlayingHeader` sits above the main content. It is visible
**only** when at least one band is currently playing (based on the current
local time vs. the timetable). When visible it shows:

- Band name(s) currently playing
- Stage name
- A thin horizontal progress bar (elapsed / total slot duration)

It must appear and disappear automatically (recompose on a 1-minute tick
or whenever the slot changes).

---

## 5. Screens

### 5.1 HOME

Vertical, single column. Blocks in order:

1. Logo (`pd_resources/favicon180.png` or final logo when supplied).
2. **Countdown block** — only **before** festival start. Days, hours,
   minutes, seconds remaining until festival start. Updates every second.
3. **Thank-you block** — only **after** festival end. Reads its text from
   a separate plain-text file so it can be edited without rebuilding:
   `app/src/main/assets/thankyou_sk.txt` and `thankyou_en.txt`
   (10–20 words each, placeholder content shipped — see §10).
4. Buttons: **News**, **Bands**, **Timetable**, **Info**, **Tickets & Eshop**.
5. Spotify festival-playlist player (see §7).
6. Social links: Facebook → `https://www.facebook.com/punkacidetom`,
   Instagram → `https://www.instagram.com/festival_punkaci_detom/`.

### 5.2 TIMETABLE

Vertical timeline, one column per stage (so two columns for the two
current stages). Per festival day:

- Day-tab buttons across the top — labelled with **day-of-week** in the
  current locale (`Friday`, `Saturday`, …), generated from data.
- Hide hours that have no slot at the start or end of the day. Each day
  has its own start/end on the timeline.
- During-festival default tab = the current festival day; otherwise the
  earliest festival day.
- Each slot card contains:
  - Band name
  - Favourite icon (heart) — visible only when marked as favourite
  - Stage
  - Start–end **time** only (no date)
  - Genre
- Tapping a slot opens the **Band detail** screen.

### 5.3 BANDS

Flat list of all bands. Sort:

1. By `SORTING_PRIORITY` ascending (lowest first).
2. Bands with empty / non-numeric `SORTING_PRIORITY` go to the **bottom**.
3. Tie-break by `NAME` ascending.

Each row shows band name, stage, genre, start day/time, and a small
favourite toggle.

### 5.4 BAND DETAIL

In order:

1. **Header image** — load from `https://davidbadin.github.io/PD2026_app/pd_resources/bands/<IMAGE_NAME>`
   where `IMAGE_NAME` comes from the sheet. If the column is empty or the
   image fails to load, fall back to the app logo. The bottom edge of the
   image fades into the background colour via a vertical gradient mask, and
   the metadata section below begins **inside** that fade.
   > **PLACEHOLDER — band images not yet uploaded.** There is just one image for now:
   > `https://davidbadin.github.io/PD2026_app/pd_resources/bands/tamgasti-cz.jpg` . 
2. Name
3. Favourite icon (clickable — toggles persisted favourite state)
4. Genre
5. Day (real day-of-week in current locale), start date, start–end time
6. Stage
7. Description (locale-aware, falls back to SK if EN missing)
8. Spotify player for that artist (see §7)

Marking a band as favourite must also surface the heart icon in the
TIMETABLE slot card (§5.2).

### 5.5 NEWS

Facebook feed for `https://www.facebook.com/punkacidetom`.

Ship a `WebView`-based screen using the Facebook **Page Plugin** embed,
which renders the latest posts from a public Page without any login
token, server-side proxy, or developer registration. The embed lives
in a tiny static HTML file the app loads as a `data:` URL or from
assets, with the page-name parameter set to `punkacidetom`:

```html
<div id="fb-root"></div>
<script async defer crossorigin="anonymous"
  src="https://connect.facebook.net/sk_SK/sdk.js#xfbml=1&version=v19.0"></script>
<div class="fb-page"
     data-href="https://www.facebook.com/punkacidetom"
     data-tabs="timeline"
     data-width=""
     data-height=""
     data-small-header="true"
     data-adapt-container-width="true"
     data-hide-cover="false"
     data-show-facepile="false">
</div>
```

If Facebook is unreachable (offline, blocked), show a graceful empty
state with a button that deep-links to the Facebook app /
`https://www.facebook.com/punkacidetom` in the browser. See `guide.md`
§5 for the alternatives if the embed proves unstable.

### 5.6 INFO

`WebView` that loads `https://davidbadin.github.io/PD2026_app/info.html`.
Create a copy of this file to the relevant folder of the app project.
`https://davidbadin.github.io/PD2026_app/info.html` will be removed later.
Use **stale-while-revalidate** caching: keep the last fetched HTML in
local cache, show it immediately, then refresh in the background. Allow
external links to open in an in-app browser tab (Chrome Custom Tabs).

### 5.7 SETTINGS

- Language switch: **SK** (default), **EN**.
- Font size: **Normal** / **Large** (see §9).
- "Update data" button — triggers immediate refresh ignoring the 30 min
  cooldown.
- Text at the bottom: "Created by David Badin"

### 5.8 TICKETS & ESHOP

External-link buttons that open in an in-app browser (Chrome Custom Tabs):

- Tickets: `https://punkacidetom.sk/vstupenky/`
- Tickets (GoOut): `https://goout.net/sk/punkaci-detom-2026/szbuqay/`
- Eshop: `https://shop.punkacidetom.sk/`

---

## 6. Stages

The sheet stores stages as single-letter **codes** in column `STAGE`.
The app must map each code to its display name. The mapping table:

| Sheet code | Display name (never translated) |
| --- | --- |
| `A` | Punk For Children Stage |
| `B` | United Stage |

Display order in the Timetable: stage `A` left column, stage `B` right
column. Adding a new stage = adding a new entry to `core-model/Stages.kt`
(code + display name) and the Timetable picks it up automatically.

The display name list is the single source of truth and must **not** be
translated. The sheet code (`A`, `B`, …) is internal.

---

## 7. Spotify integration

Use the **Spotify Android SDK** (`spotify-app-remote` and/or
`spotify-auth`) per user request. The festival's Spotify playlist:

- Festival playlist: `https://open.spotify.com/playlist/5QL8HJ0cWaLGS2Qxby0xDG`
  (playlist ID `5QL8HJ0cWaLGS2Qxby0xDG`)
- Artist URL pattern: `https://open.spotify.com/artist/<SPOTIFY_URL>`
  where `<SPOTIFY_URL>` is the value from the band row.

### 7.1 Requirements (mirrored in `guide.md`)

- A Spotify Developer App registration is required; client ID and
  redirect URI must be configured in `local.properties` (or
  `secrets.properties`) as `SPOTIFY_CLIENT_ID` and `SPOTIFY_REDIRECT_URI`
  — both should be **PLACEHOLDER**s in the generated source.
- The Spotify Android SDK needs the Spotify app installed on the user's
  device; if it's missing, the app falls back to opening the playlist /
  artist URL in the device browser (Chrome Custom Tabs).
- Premium-only features are gated behind a runtime check; non-Premium
  users still see metadata and can tap-through to the Spotify app/web.

### 7.2 Fallback embed (reference)

For reference, this is the embed Spotify provides; do **not** ship it as
the primary player but it may be used as a no-auth fallback in a
WebView if the SDK path fails entirely:

```html
<iframe data-testid="embed-iframe" style="border-radius:12px"
  src="https://open.spotify.com/embed/playlist/5QL8HJ0cWaLGS2Qxby0xDG?utm_source=generator&theme=0"
  width="100%" height="352" frameBorder="0" allowfullscreen=""
  allow="autoplay; clipboard-write; encrypted-media; fullscreen; picture-in-picture"
  loading="lazy"></iframe>
```

Adjust `src`, `width`, `height`, and `theme` as needed.

---

## 8. Push notifications

Push notifications are **Slovak-only** — a single message text is
broadcast to every subscribed device regardless of the in-app
language setting. The English locale displays the same Slovak text;
no translation is generated.

Pipeline:

```
Google Form  →  Google Sheet (PUSH tab — single MESSAGE column)
            →  Apps Script trigger (on form submit / on change)
            →  Firebase Cloud Messaging HTTP v1 API
            →  Android device (FCM service)
            →  Notification displayed (and stored in-app history)
```

### 8.1 PUSH sheet schema

There is no need to do anything with this. Just an information.

| Column | Set by | Purpose |
| --- | --- | --- |
| `TIMESTAMP` | Google Form | When the message was sent. |
| `EMAIL` | Google Form | Who sent the message. |
| `MESSAGE` | Human / form | Slovak text. Single line is fine, but multi-line is supported. |
| `SENT` | Apps Script | "OK" or error message if something went wrong. |
| `TIMESTAMP_SENT` | Apps Script | When the message was processed (only when SENT="OK"). |

### 8.2 FCM payload contract

```jsonc
{
  "message": {
    "topic": "pd2026_all",
    "notification": {
      "title": "Punkáči deťom 15",
      "body":  "<MESSAGE>"
    },
    "data": {
      "message": "<MESSAGE>",
      "sent_at": "<ISO-8601 timestamp>"
    },
    "android": {
      "priority": "HIGH",
      "notification": { "channel_id": "pd2026_news" }
    }
  }
}
```

The Apps Script source that produces this payload lives in
`guide.md` §3.3.

### 8.3 App side

- Add Firebase BoM and `firebase-messaging`, plus an
  `FirebaseMessagingService` subclass.
- Request `POST_NOTIFICATIONS` runtime permission on Android 13+.
- **One topic only**: subscribe to `pd2026_all` on first launch. No
  per-locale topic split — see §8 intro.
- Notification channel: `pd2026_news`, importance HIGH, default
  vibration on, default sound on.
- Tapping a notification resumes or opens the app.
- `google-services.json` is **NOT** committed; the real file lives in
  the git-ignored `PD2026_app/temp/` folder and is copied into
  `app/google-services.json` before each build (or symlinked from
  `temp/`). A safe `app/google-services.json.placeholder` is committed
  to make the gradle plugin's error message useful. See `guide.md` §3.

---

## 9. Design

### 9.1 Visual identity

- **Background colour:** `#0B1338` (deep navy — fills screen
  backgrounds and large surfaces)
- **Main / accent colour:** `#AD1C24` (the only accent; used for
  buttons, active states, dividers, icons, ribbons, focus rings, key
  graphics)
- **Font colour:** `#FFFFFF` (on the navy background)
- **Vibe:** cool, modern, **1977-punk** style —  photocopied, 
  hand-stencil aesthetic. High contrast, bold typography,
  slightly noisy textures are welcome, fading. See
  `https://davidbadin.github.io/PD2026_app/pd_resources/bands/tamgasti-cz.jpg` 
  for the festival's own visual language: deep navy ground, red accents, 
  white display type, torn edges, condensed bold headlines, a small heart 
  graphic between the two festival-name words.
- Dark mode is not expected.

There is **only one main accent colour** — `#AD1C24`. Earlier drafts
mentioned "Main color 1" and "Main color 2"; that has been simplified.
The navy `#0B1338` is the background, not a second accent.

### 9.2 Where design tokens live

All visual tokens are centralised so they can be edited without touching
screen code:

- Colours: `app/src/main/res/values/colors.xml`
- Dimensions / spacing: `app/src/main/res/values/dimens.xml`.
- Typography & component theming: `core-ui/Theme.kt`
  (Material 3 `ColorScheme` + `Typography` built from the colour /
  dimens resources).
- Drawable assets: `app/src/main/res/drawable/` (icons, logos).

### Typography

Three type roles, wired through the Material 3 `Typography` in
`core-ui/Theme.kt`:

| Role | Family | Where it's used |
| --- | --- | --- |
| **Display — big elements** | **3rd Man Font Family** | Screen titles ("INFO", "BANDS", "TIMETABLE"), big home-screen section buttons, day-tab labels on the timetable, the festival lockup. |
| **Mid — sub-titles / labels** | **Bebas Neue** | Sub-section headers, list-row labels, tags, captions, ribbon overlays, anywhere you want condensed uppercase between a 3rd Man headline and body text. |
| **Body — regular text** | **Poppins (Regular)** | Body paragraphs, settings labels, band descriptions, list rows, all general copy. Matches the typeface used on the festival website (punkacidetom.sk). |

### Icons

Icons are provided by **Font Awesome 7 Free** (two weights only —
Regular and Brands; no Solid). They're loaded as a `FontFamily` and
drawn either via a small `FaIcon(name, ...)` composable (preferred) or
inline via `Text(text = "", fontFamily = FontAwesomeBrands)` for
one-offs.

Roles per FA variant:

- **Font Awesome 7 Brands Regular (fa_brands_400)** — third-party logos 
  (Facebook, Instagram, Spotify, YouTube, …).
- **Font Awesome 7 Free Regular (fa_regular_400)**  — general UI glyphs 
  (heart, cog, arrow, calendar, map-pin, …) drawn in the outlined style.

For places where Font Awesome doesn't have what we need, or where
we want an animated / multi-colour icon, fall back to vector
drawables in `app/src/main/res/drawable/`. Code path:
`Icon(painter = painterResource(R.drawable.ic_xxx), ...)`.

### Font family wiring

Each family lives under `app/src/main/res/font/` and is referenced from
`Theme.kt`. Poppins is the default `Typography` body face; 3rd Man and
Bebas Neue are exposed as named `FontFamily` instances and applied
through custom Material 3 `Typography` text styles and one-off
`Text(... fontFamily = ...)` usages. Font Awesome Regular and Brands
are exposed as two more `FontFamily` instances (`FontAwesomeRegular`,
`FontAwesomeBrands`) used only for icon glyphs.

### Font files

The `.otf`/`.ttf` files currently live in **`PD2026_app/fonts/`**:

```
fonts/third_man_regular.otf
fonts/bebas_neue_regular.ttf
fonts/poppins_regular.ttf
fonts/fa_regular_400.otf
fonts/fa_brands_400.otf
```

When creating the app project files, copy the font files to
the Android-friendly paths Claude Code expects:
app/src/main/res/font/

The interim `info.html` references the files in `fonts/`
directly so it can be previewed locally. Therefore we need to keep 
the font files there (plus copy in res/font/).

### 9.3 Font sizing (accessibility)

Two app-wide scales: **Normal** and **Large**. Implemented as a
multiplier applied to the Material 3 `Typography` and to component
paddings/min-heights so layout scales coherently, not just text.

```kotlin
val fontScale = if (settings.largeFont) 1.30f else 1.0f
```

The chosen scale must be persisted (DataStore) and applied at the root
of the composition tree.

### 9.4 Icons

Footer / nav icons live in `app/src/main/res/drawable/`. The Spotify
button may reuse `pd_resources/spotify32.png` (if not found in Font Awesome); 
other icons should be generated as vector drawables for crisp scaling 
at the Large font size.

---

## 10. Internationalisation (i18n)

Two locales: **SK** (default) and **EN**. The festival name, stage
names, and any value coming from the sheet are **never** translated.

### 10.1 String files

Plain XML files, editable in any text editor (no Android Studio
required):

```
app/src/main/res/values/strings.xml         # SK — default
app/src/main/res/values-en/strings.xml      # EN
```

Convention: every key is present in both files; missing EN keys fall
back to SK.

### 10.2 Editable plain-text content blocks

For longer chunks of text that the user wants to tweak without touching
XML (which can break the build on a stray character), keep them as flat
`.txt` files under `app/src/main/assets/`:

```
assets/thankyou_sk.txt
assets/thankyou_en.txt
```

The app loads these at runtime. The build does **not** validate them, so
they're safe to edit in any editor.

### 10.3 Runtime language switch

Settings change immediately rewraps the activity context with the new
`Locale` (via `AppCompatDelegate.setApplicationLocales`) — no app
restart.

---

## 11. External integrations

### 11.1 Google Sheets

See §3. The app fetches the `PD2026` tab as CSV via the public export
URL; **no auth, no API key, no token**. The `PUSH` tab is **not** read
by the app (Apps Script + FCM only).

### 11.2 Spotify

See §7.

### 11.3 Firebase Cloud Messaging

See §8.

### 11.4 News feed (Facebook)

WebView-based Facebook Page Plugin embed for the public Page
`https://www.facebook.com/punkacidetom`. No tokens, no Meta developer
registration. Locale parameter follows the current app locale
(`sk_SK` / `en_US`). See `guide.md` §5 for fallback approaches.

### 11.5 Chrome Custom Tabs

Used for all external HTTP links (tickets, eshop, socials, fallback
Spotify links). Dependency `androidx.browser:browser`.

---

## 12. Permissions

| Permission | Why | When requested |
| --- | --- | --- |
| `INTERNET` | All HTTP / FCM / Spotify | Manifest-only |
| `ACCESS_NETWORK_STATE` | Show offline state | Manifest-only |
| `POST_NOTIFICATIONS` | Push notifications on Android 13+ | Runtime on first launch |
| `FOREGROUND_SERVICE` *(if needed)* | Spotify SDK | Manifest-only |

---

## 13. Build, signing, release

- **Build variants:** `debug`, `release`. `release` is minified
  (R8) and shrinks resources.
- **Signing config:** `release` reads from
  `keystore.properties` (gitignored). Placeholder file shipped.
- **Versioning:** `versionCode` increments per release;
  `versionName` follows semver, starting at `1.0.0`.
- **Output:** App Bundle (`.aab`) for Play Store.

`.gitignore` exist in the root repository folder (davidbadin.githu.io/). 
Claude Code will update it if necessary.

```
# Android Studio / Gradle
.gradle/
build/
.idea/
*.iml
local.properties
captures/

# Secrets
keystore.properties
*.jks
*.keystore
app/google-services.json
secrets.properties

# OS
.DS_Store
Thumbs.db
```

---

## 14. Quality

- **Unit tests:** date/time derivation, festival-day logic, sheet
  parsing, sort order, stale-while-revalidate behaviour.
- **UI tests:** screen smoke tests with Compose UI Test for Home,
  Timetable, Band Detail, Settings.
- **Lint / static analysis:** Android Lint + `ktlint` + `detekt` (or
  Compose-specific lint rules).
- **CI:** GitHub Actions workflow that runs `./gradlew lint test` on
  pull requests — *(placeholder workflow file generated by Claude Code)*.
