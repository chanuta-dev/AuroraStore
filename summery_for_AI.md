# Aurora Store Fork — Custom Whitelist & Patched APK Routing Architecture

## 📋 הנחיות ופרוטוקול עבודה לסוכן AI (Agent Protocol)
> **חובה לכל מופע / Issue חדש:**
> 1. בתחילת כל משימה, קרא את קובץ זה כדי להבין את הארכיטקטורה ואת הסטטוס העדכני של הפרויקט.
> 2. בכל קומיט או שינוי קוד, **עדכן תמיד** את סעיף [📌 סטטוס ומעקב משימות](#-סטטוס-ומעקב-משימות-roadmap--progress) בסוף הקובץ כדי לשמר רציפות וזיכרון בין סשנים ו-Issues.

---

## 📌 Overview & Project Purpose
This repository is a customized fork of **Aurora Store** (an open-source Android client for Google Play) and its underlying communication library **`gplayapi`**.

The primary purpose of this fork is to operate as a **curated, filtered, and hybrid app store** tailored for content-filtered environments (such as NetFree) and kosher/custom Android ROMs (e.g., DumberOS).

### Key Objectives:
1. **Strict Whitelist Enforcement**: Only apps explicitly listed in a remote, categorized whitelist are displayed, searchable, or accessible via deep-links.
2. **Selective APK Hijacking (CFOPUSER Integration)**: For specific whitelisted apps (e.g., WhatsApp, Waze, Bit, Spotify), download/update requests bypass Google Play and instead pull modified/patched APK binaries directly from [cfopuser/app-store](https://github.com/cfopuser/app-store) GitHub Releases.
3. **Support for Non-Google Play Apps**: Enable discovery, rich metadata display, and installation of apps that do not exist on Google Play (e.g., MetroList, Meld, Termux) by falling back to external `app.json` descriptors.
4. **Smart Version & Update Management**: Prevent update loops when upstream Google Play versions differ from patched releases, displaying an informative "Patch in progress" state when a patched build is pending.
5. **Resilient Offline/Online Caching**: Seamlessly handle startup without network connectivity and eliminate flicker/re-fetch loops on tab navigation.

---

## 🏗 Ecosystem Architecture (3 Interconnected Components)

1. **Aurora Store Client (This Repo)**: The Android app (Kotlin / Jetpack Compose) and `gplayapi` library.
2. **Whitelist Manager Web Dashboard**: A GitHub/Vercel repository maintaining `categorized-whitelist.json` and `whitelist.json` using Gemini AI categorization.
3. **Automated Patcher Factory (`cfopuser/app-store`)**: A GitHub Actions CI/CD repository that patches APKs (bypassing installer/sideload checks, stripping media/unfiltered webviews) and generates `releases.json`.

---

## 🛠 Detailed Breakdown of Modifications by File

### 1. `gplayapi` Library Layer

#### 📁 `gplayapi/WhitelistManager.kt` (New Singleton)
* **Purpose**: Manages authorized packages and category trees.
* **Logic**:
  * Centralizes `WHITELIST_URL` (`categorized-whitelist.json`).
  * `fetchRemoteWhitelist()`: Suspends and pulls the JSON from GitHub, populating `authorizedPackages` (`CopyOnWriteArrayList`) and `categorizedApps` (`ConcurrentHashMap`).
  * `isAuthorized(packageName)`: Gatekeeper method used across the app.

#### 📁 `gplayapi/PatchedAppManager.kt` (New Singleton)
* **Purpose**: Manages selective routing of patched apps from the `cfopuser/app-store` repository.
* **Logic**:
  * Centralizes `RELEASES_URL` (`releases.json`).
  * `selectedPatchedApps`: Explicit mapping of package names to CFOPUSER app IDs (e.g., `"com.bnhp.payments.paymentsapp" to "bit"`).
  * `parseReleasesJson(jsonString)`: Parses GitHub Releases to extract `downloadUrl`, `versionName`, `size`, and `fileName` for selected apps only.
  * `isPatchedUpdateReady(packageName, installedVersionName)`: Compares installed version vs GitHub release version to prevent stale re-download loops.
  * `getAppMetadata(packageName)`: Dynamic fallback that fetches `https://raw.githubusercontent.com/cfopuser/app-store/main/apps/<appId>/app.json` when an app does not exist on Google Play (populating titles, descriptions in Hebrew/English, icons, categories, etc.).

#### 📁 `gplayapi/data/models/StreamCluster.kt`
* **Purpose**: Core data model for app clusters/carousels.
* **Logic**:
  * In `init {}`, filters `clusterAppList` against `WhitelistManager.isAuthorized(packageName)`. Prevents unauthorized apps from surfacing in any UI stream.

#### 📁 `gplayapi/helpers/PurchaseHelper.kt`
* **Purpose**: Obtains download links from Google Play protocols.
* **Logic**:
  * In `purchase(...)`, intercepts the call if `PatchedAppManager.isPatchedApp(packageName)`.
  * Instead of issuing `acquire` and `delivery` RPCs to Google, returns a single `PlayFile(type = BASE)` containing the direct GitHub `browser_download_url`.

#### 📁 `gplayapi/helpers/AppDetailsHelper.kt` & `WebAppDetailsHelper.kt`
* **Purpose**: Metadata resolution for details screen and home clusters.
* **Logic**:
  * If Google Play returns 404 / empty payload (for apps like MetroList or Meld not present on Play Store) and `PatchedAppManager.isPatchedApp(packageName)` is true, falls back to `PatchedAppManager.getAppMetadata(packageName)`.

---

### 2. Aurora Store App Layer

#### 📁 `aurora/store/AuroraApp.kt`
* **Purpose**: Application lifecycle initialization.
* **Logic**:
  * In `onCreate()`, launches an asynchronous coroutine calling `WhitelistManager.fetchRemoteWhitelist()` and `PatchedAppManager.fetchReleases()`.

#### 📁 `aurora/store/ComposeActivity.kt`
* **Purpose**: Root Activity.
* **Logic**:
  * Intercepts incoming deep links (`Intent.ACTION_VIEW` / market intents) and blocks any package not in `WhitelistManager.isAuthorized()`.

#### 📁 `aurora/store/viewmodel/homestream/StreamViewModel.kt`
* **Purpose**: Powers the "For You" (בשבילך) home tab.
* **Logic**:
  * Replaces default Play Store home clusters with clusters derived from `WhitelistManager.categorizedApps`.
  * **Memory Caching**: Checks `stash[category]?.hasCluster() == true` before dispatching `ViewState.Loading`, ensuring instant (0 ms) tab switching without re-fetching.
  * **On-Demand Loading**: If `categorizedApps` is empty (e.g. cold start with no network), attempts `fetchRemoteWhitelist()` before building clusters, posting `ViewState.Error` if offline.
  * **Batch Resolution**: Uses batch `webAppDetailsHelper.getAppByPackageName(packageList)` for fast category loading.

#### 📁 `aurora/store/compose/ui/apps/ForYouPage.kt`
* **Purpose**: Compose UI for home screen.
* **Logic**:
  * Observes `LocalNetworkStatus.current` and auto-refreshes the stream when connectivity is restored.
  * Renders a `Placeholder` with an explicit "Try Again" retry action on `ViewState.Error`.

#### 📁 `aurora/store/data/work/DownloadWorker.kt`
* **Purpose**: Background download and verification execution.
* **Logic**:
  * In `verifyFile(gFile)`, checks if `PatchedAppManager.isPatchedApp(download.packageName)`. If true, bypasses strict SHA-256 hash checking against Google Play manifests (as re-signed patched APKs have different hashes) and validates basic file existence/size.

#### 📁 `aurora/store/data/room/update/Update.kt`
* **Purpose**: Room Entity representing available updates.
* **Logic**:
  * In `fromApp()`, sets `hasValidCert = true` for apps matched by `PatchedAppManager.isPatchedApp()`. Prevents Aurora from flagging custom-keystore-signed APKs as untrusted or incompatible.

#### 📁 `aurora/store/viewmodel/details/AppDetailsViewModel.kt`
* **Purpose**: ViewModel for app listing / details page.
* **Logic**:
  * Injected with `UpdateHelper`.
  * `hasValidUpdate`: Checks both `isUpdateQueuedInDatabase` and `PatchedAppManager.isPatchedUpdateReady(pkg, installedVer)`. Changes the primary action from "Open" to "Update" when a newer patched build is ready on GitHub.

#### 📁 `aurora/store/compose/composable/app/AppUpdateItem.kt` & `AppDetailsScreen.kt`
* **Purpose**: Update listing row & details action buttons.
* **Logic**:
  * **Version Discrepancy Gate**: If an upstream Google Play update exists ($V_{Google} > V_{Installed}$), but the patched build has not been published yet ($V_{Patched} \le V_{Installed}$), the action button is replaced with a disabled button labeled **"פאצ' בהכנה"** (Patch in progress / Pending build). This prevents re-downloading stale APKs or overriding modified apps with unpatched binaries.

---

## 🚦 Three-Way Version Resolution Logic

For every patched package in `selectedPatchedApps`:
* **State 1 (Update Ready)**: $V_{Google} > V_{Installed}$ **AND** $V_{Patched} > V_{Installed}$  
  ➡️ Active **"Update"** button. Downloads the latest patched APK from GitHub Releases.
* **State 2 (Patch Pending)**: $V_{Google} > V_{Installed}$ **BUT** $V_{Patched} \le V_{Installed}$  
  ➡️ App is listed in Updates tab, but button shows disabled **"פאצ' בהכנה"**. Prevents download loops.
* **State 3 (Up to Date)**: $V_{Installed} == V_{Google}$ (or $V_{Installed} == V_{Patched}$)  
  ➡️ Normal "Open" / "Uninstall" state.

---

## 🔒 Security & Extensibility Notes
* **No Direct GitHub Actions Triggers**: The client does not embed GitHub PAT tokens. Any automated dispatching must go through a serverless proxy with rate limiting and deduplication.
* **Extending Selected Patched Apps**: Add new mappings directly to `PatchedAppManager.selectedPatchedApps` in `gplayapi/PatchedAppManager.kt`.
* **Adding Purely Custom/Private Apps**: Add the package name to `categorized-whitelist.json`. Ensure a corresponding descriptor or handling exists for metadata and direct APK downloads.

---

## 📌 סטטוס ומעקב משימות (Roadmap & Progress)

### ✅ משימות שהושלמו:
- [x] אינטגרציה מלאה של `WhitelistManager` ו-`PatchedAppManager` ב-`gplayapi`.
- [x] מנגנון Three-Way Version Resolution ומניעת לולאות עדכון ("פאצ' בהכנה").
- [x] שמירת מטמון לקטגוריות בדף הבית למניעת הבהובים.
- [x] הגדרת פרוטוקול מעקב וזיכרון בין סשנים ב-`summery_for_AI.md`.

### 🔄 משימות בהמתנה / שלבים הבאים:
- [ ] הגדרת משימה ראשונה עם המשתמש.
