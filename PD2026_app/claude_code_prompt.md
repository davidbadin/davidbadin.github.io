# Claude Code Prompt — Generate the Punkáči deťom 2026 Android App

Paste the contents below into Claude Code (from `PD2026_app/` as the
working directory). The three reference files (`app_tech_specifications.md`,
`hardware-software-setup.md`, `guide.md`) are expected to be present in the
same folder and will be read by Claude Code as part of the run.

---

## Prompt

You are generating a brand-new Android application from scratch. The
project root is the current working directory (`PD2026_app/`) inside the
`davidbadin.github.io` GitHub repo. Read the following files in full
before you write a single line of code:

1. `app_tech_specifications.md` — **authoritative** technical
   specification. Treat every requirement in it as a must-have unless it
   is explicitly flagged `PLACEHOLDER` or `TODO`.
2. `hardware-software-setup.md` — describes the two machines this code
   will be built on (a Windows 11 work PC without admin rights and a
   SteamOS Steam Deck running Ubuntu via Distrobox). Generate code that
   builds cleanly on **both** with Android Studio + command-line Gradle.
3. `guide.md` — operational notes for the human owner; you do **not**
   need to action it, but read it so you don't contradict it.

Build the app described in the spec. Some non-negotiables to keep in
mind while you work:

### Build target & stack
- Android, **min SDK 24, target/compile SDK 35**.
- **Kotlin** (latest stable), **Jetpack Compose** + **Material 3**.
- Gradle **Kotlin DSL** (`*.gradle.kts`), **JDK 17**.
- **Hilt** for DI, **Coroutines + Flow** for async,
  **Navigation-Compose** for routing.
- Single Activity, modular by feature (see spec §2 for the exact module
  layout). Each section on the home screen must live in its own feature
  module so it can be removed by deleting one `include(...)` line and
  one `FeatureRegistry` entry without breaking the rest of the app.

### Data
- Source = the public Google Sheet documented in spec §3.1. Spreadsheet
  ID `1ClI4BqoEIWRudAnckKxY8hBg768NCVuD6YF-m6EVm24`, PD2026 tab
  `gid=968980279`. Use the CSV `export?format=csv&gid=…` URL — no
  auth, no API key. Read the spreadsheet ID and gid from `BuildConfig`
  (filled from `local.properties`); don't hard-code them in source.
- Real column headers and date format `D.M.YYYY`, time format
  `H:mm:ss`, plus stage codes `A` / `B`, are all documented in spec
  §3.2.
- Implement **stale-while-revalidate**: cache-first reads, background
  refresh on app start, on resume if >30 min since last refresh, and on
  manual Settings tap (manual ignores the 30 min cooldown).
- Derive festival start, festival end, and per-festival-day windows
  from the sheet (see spec §3.4). Festival day starts at **06:00 local
  time**. Unit-test this logic.

### Design
- Apply the colour palette from spec §9.1. Centralise **every** design
  token in `res/values/colors.xml`, `res/values/dimens.xml`, and a
  Material 3 `Theme.kt` in `core-ui`. No hard-coded colours inside
  screen code.
- 1977-punk visual identity — high contrast, bold display type,
  photocopy/stencil texture is welcome. The palette is: 
  background `#0B1338` (navy) and accent `#AD1C24`
  (red), plus `#FFFFFF` text. See
  `PD2026_app/pd_resources/band_picture_example.jpg` for the
  festival's reference visual.
- **Typography (spec §9.2):** three text tiers + two icon families.
  - Big elements (titles, big home-screen buttons, day tabs) → **3rd
    Man Font Family** (`third_man_regular.ttf`).
  - Sub-titles, labels, ribbons, captions → **Bebas Neue**
    (`bebas_neue_regular.ttf`).
  - Body / regular text (paragraphs, descriptions, settings,
    list rows) → **Poppins Regular** (`poppins_regular.ttf`).
  - **Icons → Font Awesome 7 Free.** Two weights only — Regular and
    Brands; **no Solid weight is shipped**.
    - `fa_regular_400.ttf` (`FontAwesomeRegular`) — outlined UI
      glyphs: heart, cog, calendar, map-pin, arrow, etc.
    - `fa_brands_400.ttf` (`FontAwesomeBrands`) — third-party /
      social logos: Facebook, Instagram, Spotify, YouTube, etc.
    - Expose both as `FontFamily` instances. Build a small
      `FaIcon(name, family = FontAwesomeRegular, ...)` composable
      backed by a codepoint lookup table (e.g. a `Map<String, Char>`
      generated from FA's `icons.json`) so call sites read like
      `FaIcon("heart")` rather than raw unicode.
    - Footer nav buttons, the social-link buttons in the home
      screen, and the Spotify button all use FA. **Use FA for all
      the icons listed in the spec** unless FA has nothing
      appropriate.
  - Fall back to **vector drawables** in `app/src/main/res/drawable/`
    only when FA doesn't have a suitable glyph (or when we want a
    multi-colour / animated icon — e.g. the official Spotify gradient
    logo).
  - All the font files live in `app/src/main/res/font/` - copy them 
    from the interim `PD2026_app/fonts/` folder.
- Implement the **Normal / Large** font-scale toggle as a multiplier
  applied to both Typography and component paddings/min-heights — not
  just text size.

### Screens
- Build every screen listed in spec §5 with the exact content, ordering,
  and behaviour described there. Pay special attention to:
  - The dynamic **NowPlayingHeader** (spec §4.2): visible only while a
    slot is currently playing, with a progress bar; auto-appears and
    disappears.
  - The **Timetable** screen's hide-empty-hours behaviour and
    day-of-week tab labels generated from the data.
  - The **Info** screen, which is a WebView loading
    `https://davidbadin.github.io/PD2026_app/info.html` with
    stale-while-revalidate.
  - The **News** screen: WebView-based Facebook Page Plugin embed for
    `https://www.facebook.com/punkacidetom` (spec §5.5 / §11.4). No
    Meta developer tokens needed. Pass the current app locale to the
    embed (`sk_SK` / `en_US`).

### Spotify
- Use the **Spotify Android SDK** (`spotify-app-remote` /
  `spotify-auth`). Read `SPOTIFY_CLIENT_ID` and `SPOTIFY_REDIRECT_URI`
  from `local.properties` via `BuildConfig`; both ship as
  **PLACEHOLDER** values.
- If the Spotify app is not installed on the device, fall back to
  opening the playlist / artist URL in **Chrome Custom Tabs**.

### Push notifications
- Implement the Android side of the FCM pipeline described in spec §8:
  `FirebaseMessagingService` subclass, notification channel
  `pd2026_news` (importance HIGH), `POST_NOTIFICATIONS` runtime
  permission on API 33+, and a **single** topic subscription:
  `pd2026_all`. There is no per-locale topic — push notifications
  are Slovak-only and broadcast to everyone.
- Parse incoming FCM payloads using the contract in spec §8.2: read
  the `notification.body` (or the `data.message` field for data-only
  messages) as the Slovak text. Do **not** attempt to translate.
- Do **not** commit `google-services.json` — ship
  `app/google-services.json.placeholder` instead and reference it in
  the README. The real file lives in the git-ignored
  `PD2026_app/temp/` folder.

### Internationalisation
- Two locales: **SK (default)** and **EN**. Use `values/strings.xml` +
  `values-en/strings.xml`. Festival name, stage names, and any value
  from the sheet are **never** translated.
- Put long-form copy that the owner will want to edit by hand (the
  thank-you block, anything similar) under `app/src/main/assets/` as
  flat `.txt` files (`thankyou_sk.txt`, `thankyou_en.txt`).
- Runtime locale switching via
  `AppCompatDelegate.setApplicationLocales` — no app restart.

### Repository hygiene
- Generate a `.gitignore` that excludes Gradle/IDE artifacts and all
  secrets (`local.properties`, `keystore.properties`, `*.jks`,
  `*.keystore`, `app/google-services.json`, `secrets.properties`).
- Generate a `README.md` for `PD2026_app/` that summarises how to build,
  how to drop in the secrets files, and where the placeholders are.
- Generate placeholder versions of every secret file with clearly marked
  `<PLACEHOLDER_*>` values.

### Tests & quality
- Unit tests for sheet parsing, festival-day derivation,
  stale-while-revalidate cache, and Bands sort order.
- One Compose UI smoke test per major screen.
- Add a GitHub Actions workflow (`.github/workflows/android.yml`) that
  runs `./gradlew lint test` on push and pull request.

### Workflow expectations
1. Start by reading the three reference files end-to-end.
2. **Print a short build plan** (modules, key dependencies, open
   placeholders) and **stop for confirmation** before generating code.
3. Then generate the code module by module. Keep commits small and
   reviewable.
4. After the code is generated, list every `<PLACEHOLDER_*>` value the
   human still needs to fill in, where each lives, and what format it
   should take.

If anything in the spec is ambiguous, ask before assuming. If the
hardware doc says something cannot be done on a given PC, mention which
machine the next manual step belongs on.

Begin by listing the files you've read and the build plan.
