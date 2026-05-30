# Google Play Publishing Guide — Punkáči Deťom 2026

**Package:** `sk.punkacidetom.pd2026`  
**Target:** Closed Testing track  

---

## 1. Build a signed release AAB

### 1.1 Generate a release keystore (first time only)
If you already have a keystore from your previous app, you can reuse it. Otherwise:

```bash
keytool -genkey -v \
  -keystore pd2026-release.jks \
  -alias pd2026 \
  -keyalg RSA -keysize 2048 \
  -validity 10000
```

Store the `.jks` file and its passwords somewhere safe — losing them means you can never update the app.

### 1.2 Configure signing in the project

Create or update `app/keystore.properties` (do **not** commit this file to git):

```properties
storeFile=../pd2026-release.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=pd2026
keyPassword=YOUR_KEY_PASSWORD
```

Make sure `app/build.gradle.kts` references it under `signingConfigs`:

```kotlin
val keystoreProperties = Properties().apply {
    load(FileInputStream(rootProject.file("app/keystore.properties")))
}

android {
    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}
```

### 1.3 Build the AAB

In Android Studio: **Build → Generate Signed Bundle / APK → Android App Bundle → release**

Or via command line:

```bash
./gradlew bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`

---

## 2. Prepare store listing assets

Place all assets in `playstore_assets/` (this folder). Required files:

| Asset | Size | Notes |
|---|---|---|
| App icon | 512×512 px, PNG, ≤1 MB | No alpha/transparency |
| Feature graphic | 1024×500 px, JPG or PNG | Shown at top of listing |
| Phone screenshots | 2–8 screenshots, min 320px on short side | JPEG or PNG |
| Short description | Max 80 characters | See `store_listing_sk.md` / `store_listing_en.md` |
| Full description | Max 4000 characters | See same files |

---

## 3. Create the app in Google Play Console

1. Go to [play.google.com/console](https://play.google.com/console)
2. Click **Create app**
3. Fill in:
   - **App name:** Punkáči Deťom 2026
   - **Default language:** Slovak (sk)
   - **App or game:** App
   - **Free or paid:** Free
4. Accept declarations → **Create app**

---

## 4. Complete the store listing

Navigate to **Grow → Store presence → Main store listing**

### 4.1 App details
- **App name:** Punkáči Deťom 2026
- **Short description:** paste from `store_listing_sk.md`
- **Full description:** paste from `store_listing_sk.md`

### 4.2 Add translations
1. Click **Manage translations → Add language → English (en-US)**
2. Paste English texts from `store_listing_en.md`

### 4.3 Graphics
Upload the assets from step 2:
- App icon (512×512)
- Feature graphic (1024×500)
- Phone screenshots (at least 2)

### 4.4 Categorization
- **App category:** Events
- **Tags:** add relevant tags (festival, music, punk)
- **Email:** your contact email
- **Website:** https://punkacidetom.sk/

Click **Save**.

---

## 5. Set up the app content

Navigate to **Policy → App content** and complete each section:

### 5.1 Privacy policy
- **Privacy policy URL:** add your privacy policy URL  
  *(If you don't have one, create a simple page at punkacidetom.sk/privacy or use a generator like privacypolicygenerator.info)*

### 5.2 Ads
- Select **No, my app does not contain ads**

### 5.3 App access
- Select **All functionality is available without special access** (or describe login if applicable)

### 5.4 Content rating
1. Click **Start questionnaire**
2. Category: **Entertainment**
3. Answer the questions (the app contains no violence, sexual content, or substances — mark all No)
4. Submit → you'll receive an **Everyone** rating

### 5.5 Target audience
- Select **18+** (punk festival context — alcohol references likely in content)
- Or **All ages** if the app has no mature content

### 5.6 News apps
- Select **No** (not a news app)

### 5.7 Data safety
Fill in what the app collects. Based on the app's manifest and features:

| Data type | Collected | Shared | Purpose |
|---|---|---|---|
| Approximate location | No | — | — |
| Personal info (name, email) | No | — | — |
| App activity (app interactions) | Yes | No | App functionality |
| Device identifiers | Yes | No | Push notifications (FCM) |

Click **Save** on each section.

---

## 6. Set up the release track

Navigate to **Testing → Closed testing**

### 6.1 Create a track (if none exists)
1. Click **Create track**
2. Name it: `alpha` or `testers`
3. Click **Create track**

### 6.2 Add testers
1. Go to the **Testers** tab
2. Click **Create email list**
3. Name: `PD2026 Testers`
4. Add tester emails (one per line) or upload a CSV
5. Copy the **opt-in URL** — send this to testers so they can join

### 6.3 Upload the AAB
1. Go to the **Releases** tab → **Create new release**
2. Click **Upload** → select `app-release.aab` from step 1.3
3. Google Play App Signing: accept the prompt to let Google manage your signing key (recommended) — or skip if you manage your own

### 6.4 Fill release details
- **Release name:** `1.0.0 (closed test)` (or use the auto-generated versionCode)
- **Release notes:** paste from the relevant section in `store_listing_sk.md`

Click **Next**.

---

## 7. Review and roll out

1. Review the **pre-launch report** warnings (fix any critical issues)
2. Check that all store listing sections show a green checkmark
3. Click **Save** → **Send for review** (first release goes to Google review — usually takes a few hours to 3 days)
4. Once approved, click **Start rollout to Closed testing**

---

## 8. Share with testers

After rollout:
1. Go to **Closed testing → Testers**
2. Copy the **opt-in link** and send it to your testers
3. Testers open the link, join the program, then find the app on Google Play and install it

---

## Checklist before submitting

- [ ] `app-release.aab` built and signed
- [ ] App icon 512×512 uploaded
- [ ] Feature graphic 1024×500 uploaded
- [ ] At least 2 phone screenshots uploaded
- [ ] Short + full description filled (SK + EN)
- [ ] Privacy policy URL set
- [ ] Content rating completed
- [ ] Data safety form completed
- [ ] Tester emails added
- [ ] Release notes written
