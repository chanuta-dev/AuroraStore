# Aurora Store Fork — Custom Whitelist & Patched APK Routing Architecture

## 📌 Overview & Project Purpose
This repository is a customized fork of **Aurora Store** (an open-source Android client for Google Play) and its underlying communication library **`gplayapi`**.

The primary purpose of this fork is to operate as a **curated, filtered, and hybrid app store** tailored for content-filtered environments (such as NetFree) and kosher/custom Android ROMs (e.g., DumberOS).

### Key Objectives:
1. **Strict Whitelist Enforcement**: Only apps explicitly listed in a remote, categorized whitelist are displayed, searchable, or accessible via deep-links.
2. **Selective APK Hijacking (CFOPUSER Integration)**: For specific whitelisted apps (e.g., WhatsApp, Waze, Bit, Spotify), download/update requests bypass Google Play and instead pull modified/patched APK binaries directly from [cfopuser/app-store](https://github.com/cfopuser/app-store) GitHub Releases.
3. **Support for Non-Google Play Apps**: Enable discovery, rich metadata display, and installation of apps that do not exist on Google Play (e.g., MetroList, Meld, Termux) by falling back to external `app.json` descriptors.
4. **Smart Version & Update Management**: Prevent update loops when upstream Google Play versions differ from patched releases, displaying an informative "Patch in progress" state when a patched build is pending.
5. **Resilient Offline/Online Caching**: Seamlessly handle startup without network connectivity and eliminate flicker/re-fetch loops on tab navigation.
6. **Unified Native App Self-Update (Root & Shizuku)**: Checks GitHub Releases (`chanuta-dev/AuroraStore`) on launch and routes self-updates directly through Aurora's native `DownloadHelper` and `AppInstaller` pipeline, allowing completely silent background updates via Root or Shizuku without external helpers.
7. **End-to-End User App Request System**: Allows users to search the unfiltered catalog to request unapproved apps directly from the client (with NetFree-style pixelated icons for unapproved apps) and manage/approve pending requests via a Vercel dashboard.

---

## 🏗 Ecosystem Architecture (3 Interconnected Components)

1. **Aurora Store Client (This Repo)**: The Android app (Kotlin / Jetpack Compose) and `gplayapi` library.
2. **Whitelist Manager Web Dashboard**: A GitHub/Vercel repository maintaining `categorized-whitelist.json`, `whitelist.json`, and `pending-requests.json` using Gemini AI categorization and admin review workflows.
3. **Automated Patcher Factory (`cfopuser/app-store`)**: A GitHub Actions CI/CD repository that patches APKs (bypassing installer/sideload checks, stripping media/unfiltered webviews) and generates `releases.json`.

---

## 🛠 Detailed Breakdown of Modifications by File

### 1. `gplayapi` Library Layer

#### 📁 `gplayapi/SelfUpdateManager.kt` (Singleton)
* **Purpose**: Fetches release updates for Aurora Store directly from GitHub Releases (`chanuta-dev/AuroraStore`).
* **Logic**:
  * Evaluates latest release metadata (version tag, APK URL, release notes body, asset byte `size`).
  * Supports `includeBeta: Boolean` parameter to query `/releases` (evaluating pre-releases) or `/releases/latest` (stable only).
  * Returns structured `ReleaseInfo` containing download URL, size, and version notes.

#### 📁 `gplayapi/WhitelistManager.kt` (Singleton)
* **Purpose**: Manages authorized packages and category trees.
* **Logic**:
  * Centralizes `WHITELIST_URL` (`categorized-whitelist.json`).
  * `fetchRemoteWhitelist()`: Suspends and pulls JSON from GitHub, populating `authorizedPackages` (`CopyOnWriteArrayList`) and `categorizedApps` (`ConcurrentHashMap`).
  * `isAuthorized(packageName)`: Gatekeeper method used across the app.

#### 📁 `gplayapi/PatchedAppManager.kt` (Singleton)
* **Purpose**: Manages selective routing of patched apps from the `cfopuser/app-store` repository.
* **Logic**:
  * Centralizes `RELEASES_URL` (`releases.json`).
  * Maps package names to CFOPUSER app IDs (e.g. `"com.bnhp.payments.paymentsapp" to "bit"`).
  * `parseReleasesJson()`: Parses releases to extract direct APK binary download URLs.
  * Dynamic fallback to `app.json` metadata for non-Play Store apps.

#### 📁 `gplayapi/data/models/StreamCluster.kt` & `helpers/web/WebSearchHelper.kt`
* **Purpose**: Gatekeeping search and cluster results with request-mode bypass.
* **Logic**:
  * `StreamCluster` filters apps against `WhitelistManager.isAuthorized()` during `init` by default.
  * Added `filterWhitelist: Boolean = true` flag to `StreamCluster` and `WebSearchHelper.search()`.
  * Allows raw, unfiltered Google Play catalog queries exclusively when `filterWhitelist = false` (used by `AppRequestViewModel`).

#### 📁 `gplayapi/helpers/PurchaseHelper.kt`
* **Purpose**: Intercepts acquisition and download URLs.
* **Logic**:
  * Hijacks downloads for `PatchedAppManager.isPatchedApp(packageName)` by serving direct GitHub release asset URLs as `PlayFile`.

---

### 2. Client Application Layer (`app/`)

#### 📁 `app/.../ui/request/AppRequestScreen.kt` & `AppRequestViewModel.kt` (New)
* **Purpose**: UI and ViewModel for searching and requesting apps to be whitelisted.
* **Features**:
  * Executes raw searches across Google Play (`filterWhitelist = false`).
  * **NetFree-Style Pixelation**: Icons for unapproved apps are fetched at 8x8 resolution (`=s8`) and rendered with `FilterQuality.None` for chunky pixelation. Letter-based fallback avatars are displayed if network/filter blocks the image.
  * Shows "כבר בחנות" badge for apps already present in `WhitelistManager`.
  * Multi-select checkboxes with a sliding bottom bar showing `"שלח בקשה (X אפליקציות)"`.
  * Dispatches batch POST requests to Vercel API (`/api/request-app`).

#### 📁 `app/.../ui/search/SearchScreen.kt`
* **Purpose**: Integrated discovery of unlisted apps.
* **Features**:
  * When search returns 0 results: Displays actionable placeholder button: *"בקש הוספת אפליקציה לחנות"*.
  * When search returns approved results: Displays `RequestAppFooterCard` at the bottom of the list allowing users to request an app that was not in the approved results.
  * Pre-fills the active search query into `AppRequestScreen`.

#### 📁 `app/.../DeepLinkConfirmActivity.kt`, `SplashScreen.kt` & `ComposeActivity.kt`
* **Purpose**: Strict deep-link security and intent gating.
* **Logic**:
  * `DeepLinkConfirmActivity`: Intercepts `ACTION_VIEW` market and Play Store URLs, immediately checks `WhitelistManager.isAuthorized(target.packageName)`, aborts and toasts *"אפליקציה זו אינה מורשית"* if unapproved.
  * Explicitly forwards `packageName` string in Intent extras to prevent null parcel states.
  * `SplashScreen` & `ComposeActivity`: Route incoming deep links through cold-start validation to guarantee whitelist and session are initialized before opening app details.

#### 📁 `app/.../data/helper/DownloadHelper.kt` & `work/DownloadWorker.kt` (Unified Self-Update Pipeline)
* **Purpose**: Native background downloads and silent installs for Aurora Store updates.
* **Logic**:
  * **Removed `AppSelfUpdater.kt`**: Eliminated redundant `DownloadManager` and manual `Intent.ACTION_VIEW` installer.
  * `DownloadHelper.enqueueSelfUpdate(releaseInfo)`: Enqueues a synthetic `Download` entry for `context.packageName` directly into the Room DB and WorkManager pipeline.
  * `DownloadWorker`:
    * Bypasses Google Play account session and purchase RPCs for `context.packageName`.
    * Validates file presence without SHA-256 mismatch failure for self-update binaries.
    * Hands off downloaded APK to `appInstaller.getPreferredInstaller().install(download)`.
    * Executes silent background updates when **Root** or **Shizuku** is selected in settings.
  * `DownloadHelper.finalizeStaleSelfUpdate()`: Detects committed self-update install on subsequent launch and marks status as `INSTALLED`.

---

## 🔄 Client Workflows

### A. App Request Workflow
1. User searches for an unapproved app in Aurora Store.
2. An empty state button or footer card prompts to request the app.
3. User navigates to `AppRequestScreen` (raw search, NetFree-style pixelated icons).
4. User selects app(s) via checkbox and taps *"שלח בקשה"*.
5. Mobile app sends POST payload to `https://<vercel-domain>/api/request-app`.
6. Vercel serverless function uses `GITHUB_TOKEN` to record/increment requests in `pending-requests.json`.
7. Whitelist Dashboard displays the request with fire count badges (`🔥 X`); admin approves with one click into the Gemini AI categorization board.

### B. App Self-Update Workflow
1. App launches -> `ComposeActivity` checks GitHub Releases via `SelfUpdateManager.checkForUpdates()`.
2. If newer release is available, `UpdateAvailableDialog` is presented with version notes.
3. User taps *"עדכן עכשיו"* -> `downloadHelper.enqueueSelfUpdate(release)` is called.
4. `DownloadWorker` downloads the APK with regular in-app progress notifications.
5. On completion, `AppInstaller` installs the update via user's selected mode (Root / Shizuku / Session).

---

## 📈 Recent Modifications & Changelog
- **App Request System**: Built `AppRequestScreen`, `AppRequestViewModel`, and Vercel API bridge with NetFree icon pixelation.
- **Search UI Integration**: Added empty-state CTA and `RequestAppFooterCard` in `SearchScreen`.
- **Deep Link Gatekeeper**: Sealed deep-link perimeter in `DeepLinkConfirmActivity` and `SplashScreen`.
- **Self-Update Architecture Refactor**: Deleted `AppSelfUpdater.kt` and unified self-update routing through `DownloadHelper` / `DownloadWorker` to support silent Root and Shizuku installs.
- **CI/CD & Automation**: Enhanced `release.yml`, `ai_agent.yml`, and `ai_tracker.yml` workflows.
