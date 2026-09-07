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
6. **Automatic App Self-Update**: Automatically checks GitHub Releases (`chanuta-dev/AuroraStore`) on app launch and prompts the user to update when a newer build is released.

---

## 🏗 Ecosystem Architecture (3 Interconnected Components)

1. **Aurora Store Client (This Repo)**: The Android app (Kotlin / Jetpack Compose) and `gplayapi` library.
2. **Whitelist Manager Web Dashboard**: A GitHub/Vercel repository maintaining `categorized-whitelist.json` and `whitelist.json` using Gemini AI categorization.
3. **Automated Patcher Factory (`cfopuser/app-store`)**: A GitHub Actions CI/CD repository that patches APKs (bypassing installer/sideload checks, stripping media/unfiltered webviews) and generates `releases.json`.

---

## 🛠 Detailed Breakdown of Modifications by File

### 1. `gplayapi` Library Layer

#### 📁 `gplayapi/SelfUpdateManager.kt` (New Singleton)
* **Purpose**: Fetches the latest release of Aurora Store directly from `https://api.github.com/repos/chanuta-dev/AuroraStore/releases/latest`.
* **Logic**: Compares `tag_name` with `BuildConfig.VERSION_NAME` and returns a `ReleaseInfo` object if a newer release with an APK asset exists.

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

---

### 2. Aurora Store App Layer

#### 📁 `app/src/main/AndroidManifest.xml`
* Configured `tools:overrideLibrary="rikka.shizuku.api"` under `<uses-sdk>` to support minSdk 23 with Shizuku 13.1.5+ (which specifies minSdk 24).
* FileProvider configured for self-updates (`.selfupdate.fileprovider`).

#### 📁 `aurora/store/ComposeActivity.kt`
* **Purpose**: Root Activity.
* **Logic**:
  * Intercepts incoming deep links (`Intent.ACTION_VIEW` / market intents) and blocks any package not in `WhitelistManager.isAuthorized()`.
  * On startup, triggers `SelfUpdateManager.checkForUpdates()` and presents `UpdateAvailableDialog` if an update is available.

#### 📁 `aurora/store/util/AppSelfUpdater.kt` & `UpdateAvailableDialog.kt`
* **Purpose**: Download and installer helper for client updates.
* **Logic**: Downloads the latest APK via Android `DownloadManager` and opens `PackageInstaller` / `ACTION_VIEW` intent via `FileProvider`.

---

## 🔄 App Self-Update Workflow

1. App launches -> `ComposeActivity` runs `SelfUpdateManager.checkForUpdates()`.
2. Query `api.github.com/repos/chanuta-dev/AuroraStore/releases/latest`.
3. Compare latest `tag_name` vs local `BuildConfig.VERSION_NAME`.
4. If newer, present Compose `UpdateAvailableDialog` showing release notes.
5. Upon user confirmation, `AppSelfUpdater` downloads the release APK asset and prompts Android package installation.
