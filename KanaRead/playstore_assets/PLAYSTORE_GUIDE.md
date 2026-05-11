# Publishing KanaRead to Google Play — step by step

This guide takes your existing Android Studio project (`com.davidbadin.kanaread`,
versionCode 1, Compose / Kotlin) and walks you through every step needed to ship
it to the Google Play Store.

The order is important. Steps 1–3 happen on your Steam Deck (Distrobox + Android
Studio). Steps 4 onwards happen in the Google Play Console (web).

> **Time budget — read this first.** The actual work is ~3–4 hours. But Google
> *requires* new personal developer accounts to run a closed test with **at least
> 12 testers for at least 14 consecutive days** before you can apply for
> production access. So real wall-clock time from "I start now" to "live on the
> store" is around **3 weeks**. Plan around it.

---

## 0. What you already have, and what I've already done for you

Pre-existing project state (verified by reading your files):

- `applicationId = "com.davidbadin.kanaread"` ✅
- `versionCode = 1`, `versionName = "1.0"` ✅
- `minSdk = 26`, `targetSdk = 34`, `compileSdk = 34` ✅ (targetSdk 34 is required for new submissions)
- No `INTERNET` or other permissions in `AndroidManifest.xml` ✅ (matches the privacy policy)
- `app/src/main/res/mipmap-*` icons are generated ✅

Changes I've already applied to your workspace (you commit + push these with
GitHub Desktop as a single commit, e.g. "Prepare KanaRead for Play Store release"):

| File | What changed |
|---|---|
| `KanaRead/.gitignore` | **Created** — excludes build output, IDE files, keystore, secrets. |
| `KanaRead/app/build.gradle.kts` | **Edited** — added Properties loader, `signingConfigs.release`, enabled minify + shrinkResources, attached signing config to release. |
| `KanaRead/app/proguard-rules.pro` | **Replaced** — added safe R8 keep rules for Room entities, KanaReadApplication, MainActivity, and Kotlin metadata. |
| `KanaRead/keystore.properties.example` | **Created** — template you copy to `keystore.properties` on each PC (the real file stays gitignored, see step 1.3). |
| `KanaRead/playstore_assets/playstore_icon_512.png` | Created — 512×512 Play Store icon. |
| `KanaRead/playstore_assets/playstore_feature_graphic_1024x500.png` | Created — Play Store feature graphic. |
| `KanaRead/playstore_assets/privacy.html` | Created — privacy policy ready to host on GitHub Pages. |
| `KanaRead/playstore_assets/store_listing.md` | Created — SK + EN store descriptions. |
| `KanaRead/playstore_assets/PLAYSTORE_GUIDE.md` | This document. |

All of these are safe to commit. None of them contain secrets or local paths.

So the work that remains is, in order: commit & push the above → generate the
keystore (you do this, needs passwords) → fill in `keystore.properties` locally
→ build the signed AAB → Play Console paperwork → testing → release.

---

## 1. Add a signing keystore (do this once, keep forever)

A keystore is the cryptographic identity of your app. **If you ever lose it, you
can never publish updates to KanaRead again.** Back it up to two separate places.

### 1.1 👉 YOU DO NOW: commit & push everything I prepared

The KanaRead project sits inside your `davidbadin.github.io` GitHub Pages
repo, so anything not gitignored eventually becomes a public URL. We want the
safety net committed *before* any sensitive files exist on disk — and right
now no sensitive files exist yet, so this is the perfect moment.

**Using GitHub Desktop:**

1. Open GitHub Desktop with the `davidbadin.github.io` repository selected.
2. The "Changes" tab should now show these new / modified files under
   `KanaRead/`:
   - `.gitignore` (new)
   - `app/build.gradle.kts` (modified)
   - `app/proguard-rules.pro` (modified)
   - `keystore.properties.example` (new)
   - `playstore_assets/` (new folder with all the listing files)
3. **Sanity check before you commit** — scan the file list and confirm you do
   **NOT** see any of these:
   - `keystore.properties` (without `.example`)
   - any `*.jks` file
   - `local.properties`
   - `app/build/`, `.gradle/`, `.idea/` folders

   If you see any of these in the change list, **stop and don't commit yet**.
   It means the `.gitignore` isn't being picked up correctly (most likely
   because it was committed in the wrong location or those files were already
   tracked from a previous commit). Tell me what you see and I'll walk you
   through the cleanup before you push.

4. Summary line for the commit:
   `Prepare KanaRead for Play Store release`
5. Description (optional but recommended):
   ```
   - Add .gitignore for Android project (excludes keystore, build output, IDE files)
   - Wire signing config into app/build.gradle.kts (loads from keystore.properties)
   - Enable R8 minify + resource shrinking on release builds
   - Add ProGuard keep rules for Room and app entry points
   - Add keystore.properties.example template
   - Add playstore_assets/ (icon, feature graphic, privacy policy, store listing copy, guide)
   ```
6. Click **Commit to main** → then **Push origin**.

After the push, on **every other PC** you use for KanaRead: open GitHub
Desktop there and click **Fetch origin → Pull**. That brings the new
`.gitignore` to the other machine before any keystore work happens there.

> **If a previous commit already leaked a secret.** This shouldn't apply to
> you (you haven't generated a keystore yet), but for the record: if a
> sensitive file is already in your git history on GitHub, removing it from
> the working folder is not enough — it's still in history. The fix is
> `git rm --cached <file>`, then rewrite history with `git filter-repo`, then
> force-push. Easier in your case: just don't put real secrets in the repo
> in the first place. The current `.gitignore` ensures that.

### 1.2 👉 YOU DO NOW: generate the keystore (in your Distrobox terminal)

Android Studio bundles `keytool` inside its JDK. Run this command — it creates a
keystore file outside the project directory so it can never accidentally get
committed:

```bash
mkdir -p ~/.android-keystores
keytool -genkey -v \
  -keystore ~/.android-keystores/kanaread-upload.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias kanaread-upload
```

You'll be prompted for:

- A **keystore password** (write it down — you cannot recover it).
- A **key password** (use the same one as the keystore password — simpler).
- "First and last name" — put `David Badin`. The rest can be `KanaRead`,
  `Bratislava`, `SK` etc.

The result is a single file: `~/.android-keystores/kanaread-upload.jks`.

> **Back up that file right now.** Copy it to a USB stick, an encrypted cloud
> folder, or both. If you lose it you will not be able to ship updates.
>
> If you build KanaRead on more than one PC, you also need to copy this `.jks`
> manually to each PC (it intentionally won't sync via GitHub). See section 1.5.

### 1.3 👉 YOU DO NOW: create your local `keystore.properties`

I've already added `keystore.properties.example` to the project. Copy it to
the real filename and fill in your values:

```bash
cd /path/to/KanaRead
cp keystore.properties.example keystore.properties
# then open keystore.properties in your editor of choice (VS Code, nano…)
# and replace the placeholders with the real path + passwords from step 1.2.
```

The final file should look like this (with your real values):

```
storeFile=/home/deck/.android-keystores/kanaread-upload.jks
storePassword=THE_PASSWORD_YOU_TYPED_IN_KEYTOOL
keyAlias=kanaread-upload
keyPassword=THE_PASSWORD_YOU_TYPED_IN_KEYTOOL
```

Adjust `/home/deck/...` to your real home directory inside Distrobox
(`echo $HOME` will tell you).

**Verification in GitHub Desktop:** after creating `keystore.properties`,
switch to GitHub Desktop and look at the "Changes" tab. It should **NOT**
show `keystore.properties` as a change. If it does, the `.gitignore` rule
isn't matching — most likely the file ended up outside the `KanaRead/` folder.
Double-check it's in the project root next to `settings.gradle.kts`.

### 1.4 ✅ DONE: signing config wired into `app/build.gradle.kts`

I've already applied this edit for you. Below is the resulting file content for
reference — there is nothing you need to do here unless something looks wrong
in your copy after `git pull`.

```kotlin
// app/build.gradle.kts

import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

// Load keystore.properties from the project root if it exists
val keystoreProps = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) load(FileInputStream(f))
}

android {
    namespace = "com.davidbadin.kanaread"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.davidbadin.kanaread"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    signingConfigs {
        create("release") {
            if (keystoreProps.isNotEmpty()) {
                storeFile = file(keystoreProps["storeFile"] as String)
                storePassword = keystoreProps["storePassword"] as String
                keyAlias = keystoreProps["keyAlias"] as String
                keyPassword = keystoreProps["keyPassword"] as String
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true                // R8: shrink + obfuscate
            isShrinkResources = true              // strip unused resources too
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Only attach signing if keystore.properties is present, so a
            // fresh-clone debug build still works without the keystore.
            if (keystoreProps.isNotEmpty()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

// dependencies { ... }   // leave your existing dependencies block as is
```

Note that `signingConfig = signingConfigs.getByName("release")` is wrapped in
an `if (keystoreProps.isNotEmpty())` check, so anyone cloning the repo without
a `keystore.properties` (e.g. a future contributor, or you on a freshly-set-up
2nd PC before you copy the keystore) can still build a *debug* APK. Only the
release build needs the keystore.

> **Heads-up about minify.** `isMinifyEnabled = true` runs R8 to shrink and
> obfuscate your code. I've added safe keep rules in `app/proguard-rules.pro`
> covering Room, Compose, and KanaRead's own classes — these should be enough
> for v1.0. If you ever see a release-only crash after launch, the first thing
> to check is logcat for `ClassNotFoundException` or `NoSuchMethodException` —
> that means R8 stripped something. Easy fix in that case: set
> `isMinifyEnabled = false` and rebuild.

### 1.5 👉 YOU DO NOW: build the signed Android App Bundle (.aab)

In Android Studio: **Build → Generate Signed App Bundle / APK → Android App
Bundle → use existing key store → Release**.

Or from the terminal, inside Distrobox, in the project root:

```bash
./gradlew clean bundleRelease
```

The signed bundle ends up at:

```
app/build/outputs/bundle/release/app-release.aab
```

This `.aab` is what you upload to Google Play.

> Why AAB and not APK? Google Play requires AAB for new apps since 2021. The
> store generates per-device APKs from the bundle, which makes downloads
> smaller for users.

### 1.6 What `.gitignore` actually does — and how it affects your 2-PC sync

`.gitignore` is a list of file patterns that git **completely ignores**. Files
matching those patterns are never added to commits, never pushed to GitHub,
and never pulled to other PCs. They live only on the machine where they were
created.

That's the right behaviour for everything in your `.gitignore`, but it does
mean a few files **won't** auto-sync between your two PCs. Here's the full
picture:

| File | Synced via GitHub? | Why ignored | What you do |
|---|---|---|---|
| Source code (`.kt`, `.xml`, `build.gradle.kts`, etc.) | ✅ Yes | not in `.gitignore` | Nothing — `git pull` brings it. |
| `.gitignore` itself | ✅ Yes | not in `.gitignore` | Nothing — `git pull` brings it. |
| `app/build/`, `/build/`, `/.gradle/` | ❌ No | Build cache, regenerated locally on each PC | Nothing — Gradle rebuilds them on first run. |
| `/.idea/`, `*.iml` | ❌ No | IDE settings are per-PC (different SDK paths, themes…) | Nothing — Android Studio recreates them. |
| `local.properties` | ❌ No | Holds the absolute path to the Android SDK on *this* PC | Android Studio creates it automatically when you open the project. |
| `*.jks` (your **upload keystore**) | ❌ No | **Secret** — must never be on GitHub | **Manual copy required** between PCs (see below). |
| `keystore.properties` | ❌ No | Contains your keystore passwords | **Manual copy required** between PCs (see below). |

**Bottom line:** `.gitignore` does not break your 2-PC sync for anything that
matters. Code and `.gitignore` itself sync normally. Build output and IDE
settings don't sync, but they regenerate themselves the first time you open
or build the project on the other PC. The only files that need manual
attention are the keystore (`.jks`) and `keystore.properties` — and that's
intentional, because those are the secrets you specifically don't want on
GitHub.

### 1.7 Setting up the second PC

When you sit down at your second PC for the first time:

```bash
# 1. Pull the latest source code (including the .gitignore)
cd /path/to/davidbadin.github.io/KanaRead
git pull
```

Then transfer the keystore manually. Pick **one** of these:

- **USB stick** — copy `kanaread-upload.jks` and `keystore.properties`
  from PC 1, paste into the same locations on PC 2.
- **Encrypted cloud folder** — e.g. a Cryptomator vault, or a password-protected
  7-Zip in OneDrive/Dropbox. Avoid plain unencrypted cloud folders for the
  keystore.
- **Secure file transfer** — `scp`, Syncthing with an encrypted folder, etc.

On PC 2, place the files like this:

```
~/.android-keystores/kanaread-upload.jks    # the binary keystore
KanaRead/keystore.properties                # the passwords + path
```

Edit `keystore.properties` on PC 2 if the absolute path to the keystore
differs (e.g. `/home/deck/...` on PC 1 vs `/home/david/...` on PC 2). The
path on each line is local to that PC and won't sync — that's by design.

After that, both PCs can run `./gradlew bundleRelease` and produce identical,
correctly-signed AABs. Just remember: only build a *release* AAB on one PC at
a time and upload that single file to Play Console. Building twice with
different `versionCode` values would conflict on upload.

---

## 2. Create your Google Play Console developer account

### 2.1 Decide: Personal or Organization

Pick **Personal** if KanaRead is a hobby project under your own name.
Pick **Organization** only if you're publishing on behalf of a registered
company — that path requires a D-U-N-S number (free, ~7 days to obtain).

Personal is right for KanaRead. Continue with that.

### 2.2 Sign up

1. Go to **<https://play.google.com/console/signup>**.
2. Sign in with the Google account you want tied to the developer account
   forever (changing it later is painful — use a stable personal Gmail or a
   dedicated Gmail you control).
3. Choose **"Myself"** when asked who you're building apps for.
4. Pay the **one-time $25 USD registration fee**.
5. Provide identity verification documents:
   - Government photo ID (passport / national ID).
   - Proof of address (utility bill, bank statement) **less than one year old**.
   - Phone number that can receive SMS.

Verification typically takes **1–3 business days**. Until it completes you can
fill in some of the Play Console paperwork but you cannot publish.

### 2.3 Important: the 12-tester / 14-day rule

For **new personal developer accounts**, before Google grants you access to the
production track, you must:

- Run a **Closed Testing** track,
- with **at least 12 opted-in testers**,
- for at least **14 consecutive days**, then
- submit an "apply for production" form, which is reviewed by a human.

Start lining up testers now. Friends, family, your Discord — anyone with a
Google account on Android. They join via an opt-in link you'll get from the
console in step 5. Aim for 15+ to cushion drop-offs.

---

## 3. Store-listing assets (already prepared for you)

Everything below is in `KanaRead/playstore_assets/` — that folder is your
single source of truth for the listing.

| Asset | File | Status |
|---|---|---|
| App icon (512×512 PNG, ≤1 MB) | `playstore_icon_512.png` | ✅ Ready |
| Feature graphic (1024×500 PNG, ≤15 MB) | `playstore_feature_graphic_1024x500.png` | ✅ Ready |
| Privacy policy (HTML for GitHub Pages) | `privacy.html` | ✅ Ready, needs hosting (3.2) |
| Store listing copy SK + EN | `store_listing.md` | ✅ Ready |
| Phone screenshots | needs you to capture (3.1) | ⏳ Required |

### 3.1 Phone screenshots (you need to capture these)

Google requires **at least 2** phone screenshots, max 8. KanaRead has a clean
look, so 4–6 is the sweet spot.

Specs Google accepts:

- PNG or JPEG, no alpha channel for JPEG.
- Min 320 px on the short side, max 3840 px on the long side.
- Aspect ratio between 16:9 and 9:16. Portrait phone screenshots (e.g.
  1080×2400) are the obvious choice.

**Best way to capture them — from inside Distrobox:**

```bash
# 1. Start an emulator (or plug in a real phone with USB debugging)
adb devices

# 2. Run the app from Android Studio in release mode (or install your AAB
#    via bundletool — but for screenshots, debug build is fine)
# 3. Navigate to a screen, then:
adb exec-out screencap -p > kanaread_01_selection.png
adb exec-out screencap -p > kanaread_02_practice.png
adb exec-out screencap -p > kanaread_03_correct.png
adb exec-out screencap -p > kanaread_04_records.png
```

Suggested screens to capture for KanaRead:

1. **Selection screen** showing Hiragana / Katakana / Both options.
2. **Practice screen** mid-question (kana word + input field).
3. **Practice screen** showing a "Correct ✓" feedback state.
4. **Best records dialog**.
5. **Help dialog** (optional).
6. The same set in **dark theme** if you want to highlight Material You.

Save all phone screenshots into `playstore_assets/screenshots/phone/`.

> Quality tip: take screenshots on a tall device profile (e.g. Pixel 7,
> 1080×2400). Avoid the ugly Pixel 3a aspect ratio.

### 3.2 Host the privacy policy

The privacy policy URL must be publicly reachable. Since your project lives
inside `davidbadin.github.io`, two options:

**Option A — Put it inside the KanaRead folder (recommended):**

1. Copy `playstore_assets/privacy.html` to the repo root of your KanaRead
   web subpath, e.g. `davidbadin.github.io/KanaRead/privacy.html`.
2. Commit and push.
3. The public URL becomes:
   `https://davidbadin.github.io/KanaRead/privacy.html`

**Option B — Host on the parent davidbadin.github.io site:**

1. Copy it to `davidbadin.github.io/kanaread-privacy.html`.
2. Public URL: `https://davidbadin.github.io/kanaread-privacy.html`.

Either works. Pick one URL and use the same string everywhere in Play Console.

> ⚠️ Reminder: your Android Studio project lives inside a GitHub Pages folder,
> so any file you commit ends up at a public URL. The `.gitignore` from step 1.1
> already excludes build outputs, IDE files, and (most importantly) your
> keystore — but verify with `git status --ignored` before any push.

---

## 4. Create the app in Play Console

When your developer account is verified, go to **<https://play.google.com/console>** → **Create app**.

### 4.1 App details

- **App name:** KanaRead
- **Default language:** Slovak — Slovakia (sk-SK)
- **App or game:** App
- **Free or paid:** Free
- Tick all three declarations:
  - "It's free and will stay free" *(you can change later)*
  - "App content adheres to Developer Program Policies"
  - "Comply with US export laws"

### 4.2 Dashboard tasks — fill these out before uploading

The console shows a checklist. Work through it top to bottom:

1. **App access** — choose "All functionality is available without restrictions". KanaRead has no login.
2. **Ads** — select "No, my app does not contain ads".
3. **Content rating** — start the questionnaire. For KanaRead all answers are
   **No**: no violence, no nudity, no gambling, no user-generated content,
   no location sharing, etc. You will get a rating equivalent to **PEGI 3 / ESRB Everyone / IARC 3+**.
4. **Target audience and content** — target age group **13+** (or 5+ if you want;
   targeting under-13 forces extra COPPA paperwork — 13+ is simplest). Confirm
   the app is not designed for children specifically.
5. **News apps** — No.
6. **COVID-19 contact tracing** — No.
7. **Data safety** — see 4.3 below, very important.
8. **Government apps** — No.
9. **Financial features** — None.
10. **Health** — None.
11. **Privacy policy URL** — paste the URL you set up in step 3.2.

### 4.3 Data safety form — exact answers for KanaRead

Click **Start** on the Data safety form and answer:

- *Does your app collect or share any of the required user data types?* → **No**
- *Does your app process user data?* → **No**
- *Are all of the user data collected by your app encrypted in transit?* → not
  applicable, since you collect nothing. Mark accordingly.
- *Do you provide a way for users to request that their data be deleted?* →
  **Yes** — via "Settings → Apps → KanaRead → Clear data" or by uninstalling.

If the form insists you list at least one data type, you can declare:

- *Type:* App activity → App interactions (best score / progress).
- *Collected?* No (it's stored locally only, not collected).
- *Shared?* No.

### 4.4 Main store listing

Use `store_listing.md` for the copy. Upload the assets:

- App icon → `playstore_icon_512.png`
- Feature graphic → `playstore_feature_graphic_1024x500.png`
- Phone screenshots → from `playstore_assets/screenshots/phone/`

You can fill in Slovak first (default), save, then add an English translation
under **Listings → Manage translations → Add translation → English (United
States)** and paste the EN copy from the same file.

---

## 5. Upload your AAB → Internal testing → Closed testing

### 5.1 Upload to Internal testing first

This track is instant (no Google review delay) and great for sanity-checking
the install on real devices.

1. Play Console → **Testing → Internal testing → Create new release**.
2. Drop in `app-release.aab` from `app/build/outputs/bundle/release/`.
3. **Release name** auto-fills as `1 (1.0)`. Keep it.
4. **Release notes** (sk-SK):

   ```
   <sk-SK>
   Prvé vydanie KanaRead.
   </sk-SK>
   <en-US>
   First release of KanaRead.
   </en-US>
   ```

5. **Save → Review release → Start rollout to Internal testing.**
6. Add yourself as a tester (Testers tab → create email list with your
   Gmail), then open the opt-in link Google provides on your phone and
   install via the Play Store. Verify the app launches and behaves correctly.

### 5.2 Closed testing — the 12/14 requirement

After internal testing looks good:

1. Play Console → **Testing → Closed testing → Create track** (e.g. "alpha").
2. Promote the same release from internal **or** upload a fresh AAB.
3. Add testers — they must be Google accounts. You need **12 or more** to
   actually install and keep the app installed for 14 days.
4. Send testers the opt-in link (it looks like
   `https://play.google.com/apps/testing/com.davidbadin.kanaread`).
5. Ask them to install KanaRead via the Play Store using that link.
6. **Wait 14 calendar days** while the testers actively keep the app installed.

> Reality check: testers drop off fast. Aim for **15–20 invites** to keep 12
> active. Send a friendly reminder on day 7 and day 12.

### 5.3 Apply for production access

Once the 14 days are up:

1. Play Console → **Publishing → Production → Apply for production access**.
2. Fill in the form: explain that KanaRead is a personal hobby project for
   learning hiragana/katakana, link the closed testing track, summarise the
   test feedback (e.g. "12 testers used the app over 14 days, no crash
   reports, minor UX feedback addressed in v1.0").
3. Submit. Google reviews this request; turnaround is typically 2–7 days.

---

## 6. Production rollout

After production access is granted:

1. Play Console → **Production → Create new release**.
2. Promote from your closed testing track (recommended) so the AAB doesn't
   change between test and prod.
3. Confirm release notes (same as 5.1) and countries (default = all).
4. **Review release → Start rollout to production**.
5. Submit. Google's content review takes anywhere from a few hours to ~7 days
   for a first-time app. Once approved, the listing goes live.

The first time the listing appears in Play Store search can take an extra
12–24 hours after approval. Direct link by package id works immediately:
`https://play.google.com/store/apps/details?id=com.davidbadin.kanaread`.

---

## 7. After launch — small things that matter

- **Set up Play App Signing** automatically (Play Console offers this on first
  upload — accept). Google then holds the *app signing key*, while your local
  keystore is just an *upload key*. If you ever lose your upload key you can
  request a reset, which would otherwise be game over.
- **Crash & ANR reports** appear under **Quality → Android vitals**. Check
  them weekly for the first month.
- **Updates**: bump `versionCode` (integer, must always increase) and
  optionally `versionName` in `app/build.gradle.kts`, rebuild AAB, upload.
- Google requires updating `targetSdk` annually. As of 2026 you must target
  API 34 or higher; you already do. Watch for the next bump (~Aug 2026).

---

## 8. Quick reference — all the URLs

| What | Where |
|---|---|
| Play Console | <https://play.google.com/console> |
| Sign-up | <https://play.google.com/console/signup> |
| Your live listing (after release) | `https://play.google.com/store/apps/details?id=com.davidbadin.kanaread` |
| Closed-test opt-in | `https://play.google.com/apps/testing/com.davidbadin.kanaread` |
| Privacy policy (after hosting) | `https://davidbadin.github.io/KanaRead/privacy.html` |

---

## 9. Cheat sheet — the order of operations

1. Commit `.gitignore` and run the leak check, then push (step 1.1).
2. Generate keystore + back it up to a USB stick / encrypted vault (step 1.2).
3. Create `keystore.properties` and verify it shows up as ignored (step 1.3).
4. Wire signing config into Gradle (step 1.4).
5. Build signed AAB (step 1.5).
6. (If using a 2nd PC) Manually copy `.jks` + `keystore.properties` to it (step 1.7).
7. Sign up for Play Console + pay $25 (step 2.2).
8. Wait for identity verification (1–3 days).
9. Host privacy policy on GitHub Pages (step 3.2).
10. Capture phone screenshots (step 3.1).
11. Create the app in Play Console + fill all dashboard tasks (step 4).
12. Upload AAB to Internal testing, smoke-test on your phone (step 5.1).
13. Move to Closed testing, line up 12+ testers, **wait 14 days** (step 5.2).
14. Apply for production access (step 5.3) → wait 2–7 days.
15. Roll out to production (step 6) → wait for Google review.
16. Monitor crashes, plan v1.1 (step 7).

You've got everything you need in `playstore_assets/` to do steps 3–4 today.
The long pole is the 14-day closed test window — start that as soon as your
account is verified.

がんばって！
