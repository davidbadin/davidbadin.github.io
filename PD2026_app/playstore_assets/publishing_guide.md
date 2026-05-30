# Google Play Publishing Guide — Punkáči Deťom 2026

**Package:** `sk.punkacidetom.pd2026`  
**Target:** Closed Testing track  

---

## 1. Build a signed release AAB

### 1.1 Open the signing wizard in Android Studio

In the menu bar: **Build → Generate Signed Bundle / APK...**

Select **Android App Bundle** and click **Next**.

### 1.2 Keystore

On the "Key store path" screen:

**If you already have a keystore from your previous app:**
- Click **Choose existing...** and locate your `.jks` file
- Fill in the store password, key alias, and key password
- Click **Next**

**If you need a new keystore:**
- Click **Create new...**
- Fill in the form:
  - **Key store path** — save it somewhere safe outside the project folder (e.g. `Documents/keystores/pd2026-release.jks`)
  - **Password** — choose a strong password, write it down
  - **Key alias** — `pd2026`
  - **Key password** — can be the same as store password
  - **Validity** — 25 years is fine
  - **Certificate** — fill in at least First and Last Name (your name or organization)
- Click **OK**, then **Next**

> ⚠️ Back up the `.jks` file and its passwords somewhere secure. Losing them means you can never publish an update to this app.

### 1.3 Build

- **Build Variants:** select `release`
- Click **Create**

Android Studio will build the bundle. When done, a notification appears in the bottom-right corner — click **locate** to open the output folder.

The file you need is: `app/build/outputs/bundle/release/app-release.aab`

---

## 2. Prepare store listing assets

Collect the following files and place them in the `playstore_assets/` folder alongside this guide:

| Asset | Size | Format |
|---|---|---|
| App icon | 512 × 512 px | PNG, no transparency, ≤ 1 MB |
| Feature graphic | 1024 × 500 px | JPG or PNG |
| Phone screenshots | 2–8 images, min 320 px on short side | JPG or PNG |

Texts (short description, full description, release notes) are ready to copy from `store_listing_sk.md` and `store_listing_en.md`.

---

## 3. Create the app in Google Play Console

1. Open [play.google.com/console](https://play.google.com/console) and sign in
2. Click **Create app**
3. Fill in:
   - **App name:** Punkáči Deťom 2026
   - **Default language:** Slovak (sk)
   - **App or game:** App
   - **Free or paid:** Free
4. Accept the declarations at the bottom
5. Click **Create app**

---

## 4. Complete the store listing

In the left sidebar: **Grow → Store presence → Main store listing**

### 4.1 App details
- **App name:** Punkáči Deťom 2026
- **Short description:** paste from `store_listing_sk.md`
- **Full description:** paste from `store_listing_sk.md`

### 4.2 Add English translation
1. Click **Manage translations → Add language**
2. Select **English (en-US)** → **Apply**
3. Fill in the English texts from `store_listing_en.md`

### 4.3 Graphics
Upload in the order the form shows them:
1. **App icon** — 512 × 512 PNG
2. **Feature graphic** — 1024 × 500
3. **Phone screenshots** — drag and drop at least 2 images into the phone section

### 4.4 Categorization & contact
- **App category:** Events
- **Email address:** your contact email
- **Website:** https://punkacidetom.sk/

Click **Save** at the top right.

---

## 5. Set up app content

In the left sidebar: **Policy → App content**

Work through each section top to bottom and click **Save** after each one.

### 5.1 Privacy policy
- Paste the URL of the privacy policy page:
  `https://davidbadin.github.io/PD2026_app/privacy.html`
- Click **Save**

### 5.2 Ads
- Select **No, my app does not contain ads**
- Click **Save**

### 5.3 App access
- Select **All functionality is available without special access**
- Click **Save**

### 5.4 Content rating
1. Click **Start questionnaire**
2. Enter your email address
3. Category: select **Utilities / Productivity** (closest fit — Events is not a standalone option here)
4. Answer all questions — for this app, everything is **No** (no violence, no sexual content, no substances)
5. Click **Submit** — you will receive an **Everyone** or **PEGI 3** rating

### 5.5 Target audience
- Tick **18 and over** (punk festival — alcohol references likely in content)
- Click **Save**

### 5.6 News apps
- Select **No**
- Click **Save**

### 5.7 Data safety
Click **Start** and go through the form:

1. **Location** — No
2. **Personal info** — No
3. **Financial info** — No
4. **Health and fitness** — No
5. **Messages** — No
6. **Photos and videos** — No
7. **Audio files** — No
8. **Files and docs** — No
9. **Calendar** — No
10. **Contacts** — No
11. **App activity** — No
12. **Web browsing** — No
13. **App info and performance** — No
14. **Device and other IDs** — **Yes**
    - Device or other IDs → collected, not shared, purpose: App functionality (push notifications via FCM)

Click **Next**, review the summary, click **Submit**.

---

## 6. Set up the closed testing track

In the left sidebar: **Testing → Closed testing**

### 6.1 Create a track (if none exists)
1. Click **Create track**
2. Name: `Testers` (or `Alpha`)
3. Click **Create track**

### 6.2 Add testers
1. Click the **Testers** tab
2. Click **Create email list**
3. List name: `PD2026 Testers`
4. Paste in the tester email addresses (one per line)
5. Click **Save changes**
6. Tick the checkbox next to the list to activate it
7. Copy the **opt-in URL** shown below the list — you will send this to your testers later

### 6.3 Upload the AAB
1. Click the **Releases** tab → **Create new release**
2. **App signing by Google Play** — click **Continue** to let Google manage signing (recommended)
3. Click **Upload** → select `app-release.aab` built in step 1
4. Wait for the upload and processing to finish

### 6.4 Release details
- **Release name:** leave as auto-generated, or type `1.0.0`
- **Release notes:** paste from `store_listing_sk.md` (the "Poznámky k vydaniu" section)

Click **Next → Save**.

---

## 7. Review and roll out

1. On the release overview page, check for any **Errors** (red) — these must be fixed before you can publish. Warnings (yellow) can usually be ignored for a closed test.
2. In the left sidebar, go to **Publishing overview** — all sections should show a green checkmark. If any are red, click in to fix them.
3. Click **Send changes for review** (Google reviews the first release — typically a few hours, sometimes up to 3 days)
4. Once the status changes to **Approved**, go back to **Testing → Closed testing**, open your release, and click **Start rollout to Closed testing** → **Rollout**

---

## 8. Share with testers

After rollout:
1. Go to **Testing → Closed testing → Testers**
2. Copy the **opt-in link**
3. Send the link to your testers — they open it in a browser on their Android phone, join the programme, then find and install the app on Google Play

---

## Checklist before submitting

- [ ] `app-release.aab` built and signed via Android Studio wizard
- [ ] App icon 512 × 512 uploaded
- [ ] Feature graphic 1024 × 500 uploaded
- [ ] At least 2 phone screenshots uploaded
- [ ] Short + full description filled (SK + EN)
- [ ] Privacy policy URL set
- [ ] Content rating questionnaire completed
- [ ] Data safety form submitted
- [ ] Tester email list created and activated
- [ ] Release notes added
- [ ] No red errors in Publishing overview
