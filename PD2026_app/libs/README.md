# libs/ — Local AAR dependencies

## Spotify Android App Remote SDK

The Spotify Android App Remote SDK is not published on Maven Central and must be
obtained manually.

**Steps:**

1. Go to https://github.com/spotify/android-sdk/releases
2. Download `spotify-app-remote-release-0.8.0.aar` (or the latest version)
3. Place it in this directory: `PD2026_app/libs/spotify-app-remote-release-0.8.0.aar`

The `feature-spotify` module references this AAR via:
```kotlin
implementation(files("${rootProject.projectDir}/libs/spotify-app-remote-release-0.8.0.aar"))
```

**Without the AAR:** The app still compiles. The `SpotifyPlayerComposable` falls back
to a Chrome Custom Tabs link to the Spotify playlist/artist URL. No runtime crash.

**This directory is gitignored** — do not commit the AAR file.
