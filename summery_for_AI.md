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
6. **Automatic App Self-Update**: Automatically checks GitHub Releases (`chanuta-dev/AuroraStore`) on app launch and prompts the user to update when a newer build is released. Includes support for opt-in beta/pre-release updates.

---

## 🏗 Ecosystem Architecture (3 Interconnected Components)

1. **Aurora Store Client (This Repo)**: The Android app (Kotlin / Jetpack Compose) and `gplayapi` library.
2. **Whitelist Manager Web Dashboard**: A GitHub/Vercel repository maintaining `categorized-whitelist.json` and `whitelist.json` using Gemini AI categorization.
3. **Automated Patcher Factory (`cfopuser/app-store`)**: A GitHub Actions CI/CD repository that patches APKs (bypassing installer/sideload checks, stripping media/unfiltered webviews) and generates `releases.json`.

---

## 🛠 Detailed Breakdown of Modifications by File

### 1. `gplayapi` Library Layer

#### 📁 `gplayapi/SelfUpdateManager.kt` (Updated Singleton)
* **Purpose**: Fetches release updates for Aurora Store directly from GitHub Releases (`chanuta-dev/AuroraStore`).
* **Logic**:
  * Supports `includeBeta: Boolean` parameter in `checkForUpdates()`.
  * When `includeBeta == true`, queries `/releases` to evaluate both stable and pre-release (beta) versions.
  * When `includeBeta == false`, queries `/releases/latest` for official stable releases only.
  * Compares version strings and returns `ReleaseInfo` with download URL and `isPrerelease` indicator.

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

## 🔄 App Self-Update Workflow

1. App launches -> `ComposeActivity` reads `PREFERENCE_INCLUDE_BETA_UPDATES` and executes `SelfUpdateManager.checkForUpdates(currentVersion, includeBeta)`.
2. Queries GitHub API (`/releases` or `/releases/latest` based on preference).
3. Compares latest available tag vs local `BuildConfig.VERSION_NAME`.
4. If newer version exists, presents Compose `UpdateAvailableDialog` showing release notes and pre-release tag if applicable.
5. Upon user confirmation, `AppSelfUpdater` downloads the release APK asset and prompts Android package installation.
