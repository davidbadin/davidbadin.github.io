# PD2026 App — Owner's Guide

Everything **outside** what Claude Code will generate: things you (David)
need to do by hand, before or after the code is generated. Both PCs
(Work PC + Steam Deck) are assumed to be set up for Android development
already.

---

## 0. Checklist — quickest path from zero to a build

1. [x] Google Sheet is readable — already done (see §1).
2. [ ] Drop the font `.ttf` files into `app/src/main/res/font/`
       (see §10).
3. [ ] Set up the Firebase project and download `google-services.json`
       (see §3.1).
4. [ ] Register a Spotify Developer App and copy the Client ID +
       Redirect URI (see §4.1).
5. [ ] Run Claude Code from `PD2026_app/` with the prompt in
       `claude_code_prompt.md`.
6. [ ] After generation: drop `google-services.json` into `app/`, fill
       `local.properties` with `SPOTIFY_CLIENT_ID`,
       `SPOTIFY_REDIRECT_URI`, then build on the Steam Deck (Android
       Studio).
7. [ ] Deploy the Apps Script that sends push notifications (see §3.2).
8. [ ] Smoke-test on a real device.
9. [ ] When ready, publish to Google Play (see §7 — outline only).

---

## 1. Google Sheet — already readable, here's what I see

The shared sheet
(`https://docs.google.com/spreadsheets/d/1ClI4BqoEIWRudAnckKxY8hBg768NCVuD6YF-m6EVm24/edit?usp=sharing`,
PD2026 tab `gid=968980279`) is set to "Anyone with the link → Viewer",
so the public CSV export URL works for both me and the Android app
without auth:

```
https://docs.google.com/spreadsheets/d/1ClI4BqoEIWRudAnckKxY8hBg768NCVuD6YF-m6EVm24/export?format=csv&gid=968980279
```

I fetched the sheet and verified the real column layout — the spec
(§3.2) is updated to match.

### Things worth flagging from the live sheet

1. **Underscored column headers.** The headers `SPOTIFY_URL` and
   `SORTING_PRIORITY` use underscores (renamed in the sheet from the
   earlier space-separated versions). The mapper in the app must use
   those exact underscored names.
2. **Date format `D.M.YYYY`, time format `H:mm:ss`.** No leading zeros,
   seconds included. The parser accommodates this; just don't switch
   to `YYYY-MM-DD` halfway through without flagging.
3. **Stage codes `A` / `B`, not full names.** The app maps these to
   *Punk For Children Stage* / *United Stage* (spec §6). If you add a
   third stage in 2026 (say `C`), add it to the mapping in
   `core-model/Stages.kt` and you're done.
4. **DESCRIPTION is Slovak-only** — there is no `DESCRIPTION_EN`
   column. The EN locale shows the Slovak text with a small "Machine
   translation — Slovak original" subtitle. If you later add a
   `DESCRIPTION_EN` column, the mapper picks it up automatically (it's
   defined as optional).
5. **Year is 2025 in the live data.** Confirmed not an issue — you'll
   refresh it for 2026 closer to the festival. The app is year-agnostic
   (festival start/end are derived from the data).
6. **Blank `ID` = the row is hidden.** A row with an empty column A
   is silently skipped by the app — it won't appear in Bands /
   Timetable / search. This is the supported way to temporarily
   disable a row in the sheet without deleting it (band pulled out,
   slot under negotiation, etc.). Numeric IDs are otherwise unique and
   stable.
7. **`PUSH` tab is not loaded by the app.** It's exclusively the
   input/output of the Apps Script push pipeline (§3 below).

---

## 3. Firebase + Push Notifications

The pipeline:

```
Google Form  →  Sheet PUSH tab  →  Apps Script trigger  →  FCM HTTP v1
            →  Android device  →  Notification displayed
```

### 3.1 Firebase console — one-time setup

1. Go to https://console.firebase.google.com and click **Add project**.
   Name it `pd2026-app` (or similar). Disable Google Analytics unless
   you want it.
2. In the new project, click the Android icon under "Get started by
   adding your first app".
   - **Package name:** `sk.punkacidetom.pd2026` (must match the value
     in `app/build.gradle.kts` — adjust if you change the package name
     in the tech spec).
   - **App nickname:** PD2026.
   - **Debug signing certificate SHA-1:** optional for FCM; needed if
     you later add Google Sign-In.
3. Download `google-services.json`. Place it at
   `PD2026_app/app/google-services.json`. **Never** commit this file
   — `.gitignore` excludes it.
4. In the Firebase console, go to **Project settings → Service
   accounts** and click **Generate new private key**. Save the JSON
   somewhere safe — you'll paste it into Apps Script in §3.2.
5. (Optional but recommended) In **Cloud Messaging settings**, enable
   the **HTTP v1 API**. The legacy server key is deprecated.

### 3.2 PUSH tab — the schema the Apps Script expects

Push notifications are **Slovak-only**. The English version of the app
will receive the same Slovak text.

The tab is populated by a linked Google Form (see §3.4). Column layout:

| Col | Name | Source | Purpose |
| --- | --- | --- | --- |
| A | `Timestamp` | Google Forms (auto) | When the form was submitted — not used by the script. |
| B | `Email` | Google Forms (auto) | Submitter's e-mail — not used by the script. |
| C | `MESSAGE` | Form field | The Slovak text to broadcast. |
| D | `PASSWORD` | Form field | Must match the master password in cell **G1**. |
| E | `SENT` | Written by script | Result of the send attempt (see below). |

**Cell G1** holds the master password. Keep it short and memorable —
it's typed into the form every time you send a notification.

**SENT column values written by the script:**

| Value | Meaning |
| --- | --- |
| `OK` | Notification sent successfully. |
| `Incorrect password!` | PASSWORD field didn't match G1 — nothing sent. |
| `Error: <details>` | FCM API returned an error — check the details. |

### 3.3 Apps Script — the bridge

Open the Google Sheet, **Extensions → Apps Script**. Replace the
default `Code.gs` with:

```javascript
// === CONFIG ============================================================
// Copy the three values below from your Firebase service-account JSON.
// Open the downloaded JSON file in a text editor and copy:
//   PROJECT_ID   ← value of "project_id"
//   CLIENT_EMAIL ← value of "client_email"
//   PRIVATE_KEY  ← value of "private_key"  (the long -----BEGIN … block)
//
// IMPORTANT — PRIVATE_KEY:
//   Paste the value exactly as it appears in the JSON, including the
//   surrounding double-quotes and all the \n sequences.
//   Example:
//     const PRIVATE_KEY = "-----BEGIN RSA PRIVATE KEY-----\nMIIE...\n-----END RSA PRIVATE KEY-----\n";
//   Do NOT replace \n with real line-breaks — paste the \n literally.
//
// Why not JSON.parse()? The \n sequences in the private key become real
// newline control characters inside a JSON string, which JSON.parse()
// rejects with "Bad control character". Storing the three fields
// separately as JS strings sidesteps this entirely.

const PROJECT_ID    = 'YOUR_PROJECT_ID';     // ← replace
const CLIENT_EMAIL  = 'YOUR_CLIENT_EMAIL';   // ← replace
const PRIVATE_KEY   = 'YOUR_PRIVATE_KEY';    // ← replace (keep the \n sequences)

const TOPIC         = 'pd2026_all';          // all subscribed devices
const SHEET_NAME    = 'PUSH';
const NOTIF_TITLE   = 'Punkáči deťom 15';
const PASSWORD_CELL = 'G1';                 // master password lives here

// Column positions (1-based) — match the form-linked sheet layout:
//   A=1  Timestamp  (auto, Google Forms — ignored)
//   B=2  Email      (auto, Google Forms — ignored)
//   C=3  MESSAGE    ← text to broadcast
//   D=4  PASSWORD   ← must match G1
//   E=5  SENT       ← written back by this script
const COL_MESSAGE  = 3;
const COL_PASSWORD = 4;
const COL_SENT     = 5;

// ======================================================================
// ENTRY POINT — attach as an "On form submit" trigger (see setup below)
// ======================================================================
function onFormSubmit(e) {
  const ss    = SpreadsheetApp.getActiveSpreadsheet();
  const sheet = ss.getSheetByName(SHEET_NAME);
  if (!sheet) throw new Error(`Sheet "${SHEET_NAME}" not found`);

  // Row appended by this form submission
  const lastRow = sheet.getLastRow();

  const message        = String(sheet.getRange(lastRow, COL_MESSAGE).getValue()).trim();
  const formPassword   = String(sheet.getRange(lastRow, COL_PASSWORD).getValue()).trim();
  const masterPassword = String(sheet.getRange(PASSWORD_CELL).getValue()).trim();

  // ── 1. Password check ───────────────────────────────────────────────
  if (formPassword !== masterPassword) {
    sheet.getRange(lastRow, COL_SENT).setValue('Incorrect password!');
    return;
  }

  // ── 2. Send via FCM — any error is caught and written to SENT ───────
  try {
    sendFcm_(message);
    sheet.getRange(lastRow, COL_SENT).setValue('OK');
  } catch (err) {
    sheet.getRange(lastRow, COL_SENT).setValue('Error: ' + err.toString());
  }
}

// ======================================================================
// FCM HTTP v1 — send to topic
// ======================================================================
function sendFcm_(message) {
  const token = getAccessToken_();
  const url   = `https://fcm.googleapis.com/v1/projects/${PROJECT_ID}/messages:send`;

  const payload = {
    message: {
      topic: TOPIC,
      notification: {
        title: NOTIF_TITLE,
        body:  message
      },
      data: {
        message: message,
        sent_at: new Date().toISOString()
      },
      android: {
        priority: 'HIGH',
        notification: { channel_id: 'pd2026_news' }
      }
    }
  };

  const res = UrlFetchApp.fetch(url, {
    method:             'post',
    contentType:        'application/json',
    headers:            { Authorization: 'Bearer ' + token },
    payload:            JSON.stringify(payload),
    muteHttpExceptions: true
  });

  const code = res.getResponseCode();
  if (code >= 300) {
    throw new Error(`FCM ${code}: ${res.getContentText()}`);
  }
}

// ======================================================================
// OAuth2 — exchange service-account JSON for a short-lived access token
// ======================================================================
function getAccessToken_() {
  const now   = Math.floor(Date.now() / 1000);
  const claim = {
    iss:   CLIENT_EMAIL,
    scope: 'https://www.googleapis.com/auth/firebase.messaging',
    aud:   'https://oauth2.googleapis.com/token',
    iat:   now,
    exp:   now + 3600
  };
  const hdr      = Utilities.base64EncodeWebSafe(JSON.stringify({ alg: 'RS256', typ: 'JWT' }));
  const claimB64 = Utilities.base64EncodeWebSafe(JSON.stringify(claim));
  const sig      = Utilities.computeRsaSha256Signature(
    `${hdr}.${claimB64}`,
    PRIVATE_KEY
  );
  const jwt = `${hdr}.${claimB64}.${Utilities.base64EncodeWebSafe(sig)}`;

  const res = UrlFetchApp.fetch('https://oauth2.googleapis.com/token', {
    method:  'post',
    payload: {
      grant_type: 'urn:ietf:params:oauth:grant-type:jwt-bearer',
      assertion:  jwt
    }
  });
  return JSON.parse(res.getContentText()).access_token;
}
```

Then in Apps Script:

1. **Save** the project (name it `PD2026 Push`).
2. **Triggers** (clock icon, left rail) → **Add Trigger**:
   - Function: `onFormSubmit`
   - Event source: **From spreadsheet**
   - Event type: **On form submit**
3. Run the function once manually to authorise the `UrlFetchApp` +
   Sheets scopes. Use a test row (with the correct password in D) to
   verify a real notification arrives on your test device.
4. After the test, confirm the `SENT` column shows `OK` for the test
   row and `Incorrect password!` for any row where you tested a wrong
   password.

### 3.4 Google Form

Create a Google Form with **two** short-answer fields:

- **`MESSAGE`** (required) — the Slovak text to broadcast.
- **`PASSWORD`** (required) — type the master password from cell G1.

Enable **"Collect email addresses"** if you want automatic email
capture in column B (useful for auditing who sent what). Link the form
to the same spreadsheet and choose `PUSH` as the response sheet.

The script reads the last submitted row on every form submission and
writes back to column E (`SENT`). Open the sheet after sending to
confirm the result.

---

## 4. Spotify

### 4.1 Register a Spotify developer app

1. https://developer.spotify.com/dashboard → **Create app**.
2. Name: `Punkáči deťom 15`.
3. Redirect URI: `pd2026://callback` (this matches the
   `SPOTIFY_REDIRECT_URI` placeholder in the spec — adjust together if
   you change it).
4. Under **Which API/SDKs are you planning to use** select
   **Android**.
5. Save the **Client ID**. There is no client secret needed for the
   Spotify Android SDK auth flow.
6. The Spotify Android SDK verifies that the app connecting to it is
   really yours by checking the signing certificate. You need to
   register your APK's SHA-1 fingerprint in the Spotify dashboard,
   otherwise the SDK silently refuses to connect.

   **Do this on the Steam Deck** (where Android Studio is installed).
   First make sure you've run at least one build so Android Studio has
   created the debug keystore, then run:

   ```
   keytool -list -v -keystore ~/.android/debug.keystore \
           -alias androiddebugkey -storepass android -keypass android
   ```

   In the output, find the line that reads `SHA1:` and copy the
   fingerprint (looks like `AB:12:CD:34:…`).

   Back in the Spotify dashboard → your app → **Edit settings** →
   scroll to **Android packages** → add an entry:
   - **Package name:** `sk.punkacidetom.pd2026`
   - **SHA-1 fingerprint:** paste the value you just copied

   Save. The SDK will now accept connections from your debug build.

   When you later create a release signing key for Google Play (§7),
   come back here and add the release SHA-1 the same way — both can
   coexist in the dashboard.

### 4.2 Where to paste the values

In `PD2026_app/local.properties` (gitignored):

```
SPOTIFY_CLIENT_ID=<your-spotify-client-id>
SPOTIFY_REDIRECT_URI=pd2026://callback
```

These get exposed to the code via `BuildConfig`.

### 4.3 Premium vs. free

The Spotify Android SDK requires the user to have the Spotify app
installed. **Full playback** of a track requires the user to be a
Spotify Premium subscriber; non-Premium users see metadata and get
tap-through into the Spotify app/web. This is a Spotify constraint,
not an app bug.

---

## 5. News feed (Facebook)

Default approach (matches spec §5.5 / §11.4): the News screen is a
`WebView` that loads a tiny static HTML file embedding the official
**Facebook Page Plugin** pointed at
`https://www.facebook.com/punkacidetom`. This works **without** any
Meta developer registration, access tokens, or App Review — the Page
Plugin is a public widget.

### 5.1 No setup required (default path)

Nothing for you to do — Claude Code will generate the HTML stub
locally. The first time the screen opens, the Facebook SDK loads from
`connect.facebook.net` and renders the latest posts. The current app
locale is passed to the SDK URL (`sk_SK` or `en_US`).

If the user is logged into Facebook on their device, the feed will
look richer (avatars, reactions, etc.). If not, they'll still see the
posts.

### 5.2 If the Page Plugin proves unreliable

Two fallbacks documented for later, only if needed:

1. **WebView pointing straight at the public Facebook Page URL**
   (`https://www.facebook.com/punkacidetom`). Simplest but Facebook's
   m-site nags for login.
2. **Facebook Graph API** — official, but heavy:
   - Verify the Facebook Page belongs to you (it does).
   - Create a Meta developer app at
     https://developers.facebook.com/apps, add the **Pages** product,
     go through App Review for `pages_read_engagement` +
     `pages_show_list`.
   - Generate a long-lived Page Access Token and inject it into the
     Android app via `BuildConfig`. Refresh the token every ~60 days.
   - Fetch `/<page-id>/posts` and render a native list.
3. **News tab in the Google Sheet** — add a `NEWS` tab with columns
   `TIMESTAMP | TITLE_SK | TITLE_EN | BODY_SK | BODY_EN | IMAGE_URL |
   LINK_URL` and render native cards from it. Most flexible, but means
   double-posting (Facebook + sheet) for every announcement.

Stick with the Page Plugin for v1 unless it visibly breaks.

---

## 6. Translations workflow

Two locales: **SK** (default) and **EN**.

### 6.1 Where the strings live

After Claude Code generates the app:

- `PD2026_app/app/src/main/res/values/strings.xml` — Slovak (default).
- `PD2026_app/app/src/main/res/values-en/strings.xml` — English.

Both are **plain XML**, editable in VSCode (no Android Studio needed).
Convention:

```xml
<resources>
    <string name="home_title">Domov</string>
    <string name="bands">Kapely</string>
    <!-- … -->
</resources>
```

### 6.2 How to edit safely

- Edit values **inside** the `>…<` only. Don't change the `name="…"`
  attribute — that's the key referenced from code.
- If a string contains a single quote `'`, escape it as `\'`.
  If it contains a `&`, escape as `&amp;`.
- After editing, Android Studio will pick up the change next build —
  no codegen needed.
- For long-form copy (thank-you message, etc.) edit the plain `.txt`
  files in `app/src/main/assets/` (`thankyou_sk.txt`,
  `thankyou_en.txt`). No escaping rules — pure text, free-form,
  10–20 words.

### 6.3 Suggested process

1. Edit `strings.xml` (SK) and `values-en/strings.xml` (EN) in VSCode.
2. Commit to GitHub from the Work PC.
3. Pull on the Steam Deck, rebuild in Android Studio.

### 6.4 If a translation feels wrong

You can fix a single string by editing `values-en/strings.xml` only;
Slovak stays untouched. No code changes needed. Reinstall the app to
see the new text.

---

## 7. Publishing to Google Play — outline

(Not detailed yet — placeholder for the future.)

Rough order of operations:

1. Create a **Google Play Console** account ($25 one-time, on a PC with
   admin rights — this is a **Steam Deck** task because of Work PC
   restrictions).
2. Configure a **release signing key** (`keystore.jks`); store it
   somewhere you won't lose it. Put credentials in
   `keystore.properties` (gitignored).
3. Build a signed App Bundle: **Android Studio → Build → Generate
   Signed Bundle / APK**.
4. In Play Console, create the app, fill in store listing (icon,
   feature graphic, screenshots, privacy policy URL), and upload the
   `.aab` to the **Internal testing** track first.
5. Submit for review. Once approved, promote to Closed → Open →
   Production tracks.

Detailed steps to be written closer to release.

---

## 8. Where each task should be done

| Task | Work PC | Steam Deck |
| --- | :-: | :-: |
| Edit spec / prompt / guide / strings / Apps Script | yes | yes |
| Run Claude Desktop (this assistant) | yes | – |
| Run Claude Code (generation) | yes | yes |
| Build the APK / AAB in Android Studio | – | yes |
| Run on a real Android device via ADB | – | yes |
| Manage Firebase / Spotify dashboards (browser) | yes | yes |
| Google Play Console signup & uploads | – | yes |

---

## 9. Open placeholders — track these

After Claude Code generation, search the codebase for `<PLACEHOLDER_`
and resolve each one:

- `YOUR_PROJECT_ID`, `YOUR_CLIENT_EMAIL`, `YOUR_PRIVATE_KEY` — in Apps Script (§3.3). Copy from the downloaded Firebase service-account JSON.
- `SPOTIFY_CLIENT_ID` — Spotify dashboard (§4.1).
- `SPOTIFY_REDIRECT_URI` — Spotify dashboard (§4.1).
- Application ID `sk.punkacidetom.pd2026` — confirm before Play Store.
- Typography font files in `app/src/main/res/font/` (§10).
- Body-text font family (§10) — currently falls back to system sans.

---

## 10. Fonts

The app uses three families (spec §9.2). They currently live in
**`PD2026_app/temp/fonts/`** under their original distribution names —
`info.html` loads them from there for preview. The whole `temp/`
folder is git-ignored (it also holds the Firebase service-account
JSON), so nothing in there is pushed to GitHub.

### 10.1 Current contents of `temp/fonts/`

```
3rd Man.otf
BebasNeue-Regular.ttf
Poppins-Regular.ttf
Font Awesome 7 Free-Regular-400.otf
Font Awesome 7 Brands-Regular-400.otf
```

### 10.2 Move + rename for the Android build

Android resource filenames must be **lowercase**, contain only
`[a-z0-9_]`, and start with a letter:

| From `temp/fonts/` | To `app/src/main/res/font/` |
| --- | --- |
| `3rd Man.otf` | `third_man_regular.ttf` |
| `BebasNeue-Regular.ttf` | `bebas_neue_regular.ttf` |
| `Poppins-Regular.ttf` | `poppins_regular.ttf` |
| `Font Awesome 7 Free-Regular-400.otf` | `fa_regular_400.ttf` |
| `Font Awesome 7 Brands-Regular-400.otf` | `fa_brands_400.ttf` |

(Android accepts both `.ttf` and `.otf`; just change the extension if
you keep `.otf`.)

### 10.3 Role per family

- **3rd Man Font Family** — big elements: page titles, the big home
  buttons, day-tab labels, the festival lockup.
- **Bebas Neue** — mid tier: sub-titles, labels, ribbons, captions.
- **Poppins (Regular)** — body / regular text everywhere: paragraphs,
  settings labels, band descriptions, list rows. Matches the typeface
  used on punkacidetom.sk.
- **Font Awesome 7 Free Regular** — outlined UI icons (heart, cog,
  calendar, map-pin, arrow, etc.).
- **Font Awesome 7 Brands Regular** — third-party / social logos
  (Facebook, Instagram, Spotify, YouTube, …).

Use Font Awesome whenever possible for icons — drawn either via a
small `FaIcon(name, …)` composable (preferred) or inline via
`Text(text = "<glyph>", fontFamily = FontAwesomeBrands)`. For anything
FA doesn't cover, or where we want a multi-colour / animated icon,
fall back to vector drawables in `app/src/main/res/drawable/`.

> Only the **Regular** weight is included for FA Free (no Solid). The
> outlined look is intentional — it matches Bebas Neue's lighter
> mid-tier feel better than the heavier Solid weight would.

### 10.4 After Android-build generation: serving fonts from `info.html`

`info.html` is served from GitHub Pages, but `PD2026_app/temp/` is
git-ignored and `app/src/main/res/font/` lives inside the APK — so
neither location is reachable from a browser hitting the published
site. The simplest fix is to keep a **second** committed copy of the
three `.ttf`s at `PD2026_app/fonts/` (note: no `_temp`, no leading
slash) and update the three `@font-face url(...)` lines in
`info.html` to point at `fonts/3rd Man.otf` etc.

### 10.5 File-naming gotchas

- ✅ `third_man_regular.ttf`
- ❌ `ThirdMan-Regular.ttf` (uppercase, hyphen)
- ❌ `3rd-man-regular.ttf` (starts with digit, hyphens)

A bad filename fails the build with a clear `invalid resource name`
error.
