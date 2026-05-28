# PD2026 App — Fix Instructions for Claude Code

Each section below describes a bug, its root cause (based on code inspection), and the exact fix required.

---

## 1. Translation — Slovak as forced default

**Problem:** The app displays English when the device is set to English, even though Slovak is intended as the default. `LocaleHelper.currentLocaleTag` returns `"sk"` as a fallback, but `AppCompatDelegate.getApplicationLocales()` is empty on a fresh install, so Android falls back to the device locale (English) for resource resolution.

**Root cause:** `LocaleHelper.applyLocale()` is only called when the user explicitly changes the language in Settings (`SettingsViewModel.setLanguage()`). On first launch it is never called, so no override is registered with the OS.

**Fix:** In `PD2026App.onCreate()`, read the saved language from `UserPreferencesRepository` (DataStore) and immediately call `LocaleHelper.applyLocale()`. Because `DataStore` is async, use `runBlocking` with `first()` here — it is acceptable in `Application.onCreate()` since the value is tiny and always cached. Default to `"sk"` when no value is stored yet.

```kotlin
// PD2026App.kt — inside onCreate(), after super.onCreate()
val userPrefs = EntryPoints.get(this, UserPrefsEntryPoint::class.java).userPrefsRepository()
val savedLang = runBlocking { userPrefs.language.first() }   // defaults to "sk" per repo
LocaleHelper().applyLocale(savedLang)
```

If Hilt entry-point injection in `Application` is inconvenient, an alternative is to add an `AppCompatDelegate.setDefaultNightMode` equivalent by overriding `attachBaseContext` in `MainActivity` and applying the locale there before `super.attachBaseContext(newBase)`.

The `values/strings.xml` (Slovak) and `values-en/strings.xml` (English) files are already correct — no changes needed there.

---

## 2. Schedule — Proportional timeline layout

**Problem:** `TimetableScreen` renders slot cards as a plain vertical list with equal spacing (`Spacer(Modifier.height(spacing.sm))`). Cards should be positioned and sized proportionally to their actual start/end times.

**Rules:**
- Each minute = a fixed pixel height (e.g. `2.dp` per minute → 1 hour = 120dp).
- A card's top offset = `(band.startTime − dayStart).inWholeMinutes * minuteHeight`.
- A card's height = `(band.endTime − band.startTime).inWholeMinutes * minuteHeight`.
- Bands on different stages can overlap in time (different columns), but two bands in the same column must never overlap. If a data error causes overlap in the same column, skip the later card silently.
- Do not show the stage name inside the slot card — it is already in the column header above.
- The horizontal layout (two columns side-by-side) is kept as-is.

**Fix:** Replace the `Column { forEach { SlotCard; Spacer } }` inside each stage column with a custom `Layout` composable (or a `Box` with `Modifier.offset(y = ...)` for each card). Steps:

1. Compute `dayStart` = earliest `startTime` across all bands for the selected day.
2. Compute `dayEnd` = latest `endTime` across all bands.
3. Define `minuteHeight = 2.dp` (adjust to taste).
4. Total timeline height = `(dayEnd − dayStart).inWholeMinutes * minuteHeight`.
5. For each stage column, iterate bands sorted by `startTime`. Before rendering each card, check that its `startTime >= lastRenderedEndTime` for that column; skip it if not.
6. Position each card with `Modifier.absoluteOffset(y = offsetDp)` inside a `Box` of fixed height `totalTimelineHeight`, and set the card height with `Modifier.height(cardHeightDp)`.
7. Wrap the two-column `Box` pair in a `verticalScroll`.

Remove the `Stages.displayName(band.stageCode)` `Text` from `SlotCard` (the last `Text` block in the composable).

---

## 3. Missing icons

**Problem:** Several icons render as `"?"` (codepoint not found in `FaRegularCodes`/`FaBrandsCodes`).

**Affected locations and icon names used in code:**
- Settings footer tab: `"gear"`
- Bands button (Home): `"music"`
- Info button (Home): `"circle-info"`
- Tickets & Eshop button (Home): `"ticket"`
- Back button (BandDetailScreen): `"arrow-left"`
- NowPlayingHeader left icon: `"music"`

**Root cause:** The font files (`fa_regular_400.otf`, `fa_brands_400.otf`) are Font Awesome 7. Some codepoints in `FaRegularCodes.kt` may have changed between FA6 and FA7. Verify each codepoint against the installed font file.

**Fix:**
1. Use a font inspection tool (e.g. `ttx` from fonttools, or an online font viewer) to dump the actual codepoints from `app/src/main/res/font/fa_regular_400.otf`.
2. Update `FaRegularCodes.kt` so every entry matches the actual FA7 codepoint. Pay special attention to: `gear`, `circle-info`, `ticket`, `arrow-left`, `music`, `newspaper`, `house`, `heart`, `calendar`, `sliders`.
3. Do the same for `fa_brands_400.otf` vs `FaBrandsCodes.kt` (check `spotify`, `facebook`, `instagram`).
4. After updating codepoints, rebuild and confirm all icons render correctly.

---

## 4. Home button always returns to Home screen

**Problem:** Pressing the Home tab in the footer after navigating Home → Bands → Settings shows the Bands screen instead of Home. This is caused by `restoreState = true` in the Home tab's `navigate` block, which restores Bands' previously saved state.

**Root cause:** In `AppBottomBar.kt`, the Home item's `onClick` uses:
```kotlin
navController.navigate(HomeRoute) {
    popUpTo(navController.graph.startDestinationId) { saveState = true }
    launchSingleTop = true
    restoreState = true
}
```
The `restoreState = true` re-applies the previously saved back-stack state, which included BandsRoute.

**Fix:** For the Home tab only, clear the entire back stack without saving or restoring state:
```kotlin
onClick = {
    navController.navigate(HomeRoute) {
        popUpTo(0) { inclusive = true }
        launchSingleTop = true
    }
},
```
Leave the other tabs (Timetable, Spotify, Settings) unchanged so they continue to save/restore their scroll positions correctly.

---

## 5. News / Facebook — display page directly

**Problem:** `NewsScreen` loads `news_sk.html` (or `news_en.html`) from assets. These files use the Facebook Page Plugin (`fb-page` div + SDK script). The plugin renders a small, poorly-formatted search-result block rather than the full page feed, because it requires a logged-in Facebook session and proper domain whitelisting.

**Fix:** Replace the WebView asset approach with a direct Chrome Custom Tab. When the News screen is shown, immediately open `https://www.facebook.com/punkacidetom` in a Custom Tab (no intermediate HTML file needed). Remove the secondary "Open Facebook" button since the whole screen will now be the Custom Tab.

Alternatively, if an in-app view is preferred: load `https://www.facebook.com/punkacidetom` directly in the `WebView` (not the local HTML file) with JS enabled and a desktop user-agent string. Remove the `loadUrl("file:///android_asset/$assetFile")` call and replace it with `loadUrl("https://www.facebook.com/punkacidetom")`.

The simplest and most reliable option is the Custom Tab approach — trigger it from `NewsNavigation` or the `NewsScreen` LaunchedEffect on first composition, so clicking the News icon opens Facebook immediately without an intermediate screen.

---

## 6. Footer covering screen content

**Problem:** The bottom portion of all screens is hidden behind the footer bar.

**Root cause:** In `MainActivity.kt`, the `PD2026Scaffold` content lambda discards `innerPadding`:
```kotlin
PD2026Scaffold(...) { _ ->          // ← innerPadding ignored
    AppNavHost(modifier = Modifier.fillMaxSize(), ...)
}
```
The Scaffold's `innerPadding` carries the bottom inset equal to the footer height. Without applying it, screens render edge-to-edge and their bottom content sits under the footer.

**Fix in `PD2026Scaffold.kt`:** Apply `innerPadding` to the `Box` that wraps the content:
```kotlin
Box(modifier = Modifier
    .weight(1f)
    .padding(bottom = innerPadding.calculateBottomPadding())
) {
    content(innerPadding)
}
```

Or, equivalently, update `MainActivity` to pass the padding to `AppNavHost`:
```kotlin
PD2026Scaffold(...) { innerPadding ->
    AppNavHost(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        ...
    )
}
```

Pick one approach and apply it consistently — do not apply the bottom padding twice.

---

## 7. Spotify player block height + remove from Home

**Two separate issues:**

### 7a. Block taller than player
`SpotifyPlayerComposable` wraps the WebView in a `Box(Modifier.height(embedHeight.dp))`. When called from `HomeScreen`, `embedHeight = 352` is passed. With no Spotify app installed, the embedded player renders as a compact ~80dp bar, but the Box stays 352dp tall.

**Fix:** Make the WebView height dynamic by injecting a JavaScript interface that reads `document.body.scrollHeight` after the page finishes loading and updates a `mutableStateOf<Int>` height in the composable. Minimum height should be `80.dp` to avoid a zero-height view during load. Example approach:
```kotlin
var webHeight by remember { mutableStateOf(80) }
// In WebView factory:
addJavascriptInterface(object {
    @JavascriptInterface fun reportHeight(h: Int) { webHeight = h }
}, "Android")
webViewClient = object : WebViewClient() {
    override fun onPageFinished(view: WebView, url: String) {
        view.evaluateJavascript(
            "(function() { Android.reportHeight(document.body.scrollHeight); })();"
        ) {}
    }
}
```
Use `Modifier.height(webHeight.dp.coerceAtLeast(80.dp))` on the Box.

### 7b. Remove Spotify player from Home screen
In `HomeScreen.kt`, remove the entire Spotify section:
- The `Text` label (`home_spotify_playlist`)
- The `SpotifyPlayerComposable(...)` call
- The `Spacer` before/after it
- The `FESTIVAL_PLAYLIST_ID` constant (if unused elsewhere)
- The imports `SpotifyPlayerComposable`, `spotifyPlaylistEmbedUrl`, `SpotifyLauncher` (if unused after removal)

Also remove the `home_spotify_playlist` and `home_spotify_open` string keys from all `strings.xml` files if they are no longer used anywhere.

---

## 8. App icon and splash background

**Source file:** `pd_resources/favicon180.png` (180×180, white logo on black, transparent edges)

**Fix:**

1. **Generate mipmap PNGs** from `pd_resources/favicon180.png` at all required densities and copy them into the appropriate `mipmap-*` folders:
   - `mipmap-mdpi/ic_launcher.png` — 48×48
   - `mipmap-hdpi/ic_launcher.png` — 72×72
   - `mipmap-xhdpi/ic_launcher.png` — 96×96
   - `mipmap-xxhdpi/ic_launcher.png` — 144×144
   - `mipmap-xxxhdpi/ic_launcher.png` — 192×192
   - Also create round variants (`ic_launcher_round.png`) with the same image.

2. **Update `mipmap-anydpi-v26/ic_launcher.xml`** to use the PNG directly (or keep adaptive icon but set the background layer to solid black `#000000` and the foreground to the PNG).

3. **Update `themes.xml`** — change the splash/launch window background from navy to black:
   ```xml
   <item name="android:windowBackground">@color/pd_black</item>
   ```
   Add `<color name="pd_black">#000000</color>` to `app/src/main/res/values/colors.xml` (create the file if it doesn't exist).

Note: If 180×180 is too low resolution for `xxxhdpi` (requires 192×192), let the user know and ask for a higher-resolution source file.

---

## 9. Band images not displaying

**Problem:** Band detail shows only the initial letter fallback instead of the band image.

**Root cause:** `Band.bandImageUrl` constructs the URL as:
```kotlin
"https://davidbadin.github.io/PD2026_app/pd_resources/bands/$imageName"
```
But the test image `tamgasti-cz.jpg` exists at `bands/tamgasti-cz.jpg` in the repository root, not under `pd_resources/bands/`. GitHub Pages serves it at:
```
https://davidbadin.github.io/PD2026_app/bands/tamgasti-cz.jpg
```
not
```
https://davidbadin.github.io/PD2026_app/pd_resources/bands/tamgasti-cz.jpg
```

**Fix (option A — move images, preferred):** Move all band images into `pd_resources/bands/` (e.g. `pd_resources/bands/tamgasti-cz.jpg`). Commit and push so GitHub Pages serves them at the URL the app expects. No code change needed.

**Fix (option B — change URL):** Update `Band.bandImageUrl` in `core-model/Band.kt`:
```kotlin
val bandImageUrl: String
    get() = if (imageName.isNotBlank())
        "https://davidbadin.github.io/PD2026_app/bands/$imageName"
    else ""
```
This matches the existing file locations but requires all future images to be placed in `bands/` at the repo root.

Go with option A unless there is a reason to keep images outside `pd_resources/`.

---

## 10. Base font size increase (10–15%)

**Problem:** Normal font size is too small.

**Root cause:** In `MainActivity.kt`:
```kotlin
val fontScale = if (isFontLarge) 1.30f else 1.0f
```
Normal mode uses `1.0f` (no scaling).

**Fix:** Change the normal-mode multiplier to `1.12f` (12% increase, within the requested 10–15% range):
```kotlin
val fontScale = if (isFontLarge) 1.30f else 1.12f
```

This applies uniformly to all text via `PD2026Theme(fontScaleMultiplier = fontScale)` without touching individual text styles.
