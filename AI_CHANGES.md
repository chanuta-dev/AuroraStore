## 📅 עדכון: 2026-09-09 16:28:12 UTC
**הודעת קומיט:** Update SplashScreen.kt
**קוד שינוי:** `0fbffc98a9346a6279f7b1d5f232a2034d74fd41`

### 📂 קבצים שהושפעו:
M	app/src/main/java/com/aurora/store/compose/ui/splash/SplashScreen.kt

### 📝 פירוט השינויים (Diff):
```diff
diff --git a/app/src/main/java/com/aurora/store/compose/ui/splash/SplashScreen.kt b/app/src/main/java/com/aurora/store/compose/ui/splash/SplashScreen.kt
index 43a7ba0..2e176bd 100644
--- a/app/src/main/java/com/aurora/store/compose/ui/splash/SplashScreen.kt
+++ b/app/src/main/java/com/aurora/store/compose/ui/splash/SplashScreen.kt
@@ -108,8 +108,7 @@ fun SplashScreen(
             googleLoading = false
         }
     }
-
-    LaunchedEffect(authState) {
+LaunchedEffect(authState) {
         when (val state = authState) {
             AuthState.Valid, AuthState.SignedIn -> {
                 anonymousLoading = false
@@ -119,9 +118,26 @@ fun SplashScreen(
                     !deepLinkDevId.isNullOrBlank() -> onNavigateTo(
                         Destination.DevProfile(deepLinkDevId)
                     )
-                    !deepLinkPackageName.isNullOrBlank() -> onNavigateTo(
-                        Destination.AppDetails(deepLinkPackageName)
-                    )
+                    !deepLinkPackageName.isNullOrBlank() -> {
+                        // בדיקת הרשאה הרמטית: האם האפליקציה ברשימה הלבנה?
+                        if (com.aurora.gplayapi.WhitelistManager.isAuthorized(deepLinkPackageName)) {
+                            onNavigateTo(Destination.AppDetails(deepLinkPackageName))
+                        } else {
+                            android.widget.Toast.makeText(
+                                context,
+                                "אפליקציה זו אינה מורשית",
+                                android.widget.Toast.LENGTH_LONG
+                            ).show()
+                            onNavigateTo(
+                                Destination.Main(
+                                    Preferences.getInteger(
+                                        context,
+                                        Preferences.PREFERENCE_DEFAULT_SELECTED_TAB
+                                    )
+                                )
+                            )
+                        }
+                    }
                     else -> onNavigateTo(
                         Destination.Main(
                             Preferences.getInteger(
```

---

## 📅 עדכון: 2026-09-09 16:26:11 UTC
**הודעת קומיט:** Update ComposeActivity.kt
**קוד שינוי:** `ac1d5681e8105975aa25dd22c60416e9106f8c65`

### 📂 קבצים שהושפעו:
M	app/src/main/java/com/aurora/store/ComposeActivity.kt

### 📝 פירוט השינויים (Diff):
```diff
diff --git a/app/src/main/java/com/aurora/store/ComposeActivity.kt b/app/src/main/java/com/aurora/store/ComposeActivity.kt
index b14a9fa..8f0c7b3 100644
--- a/app/src/main/java/com/aurora/store/ComposeActivity.kt
+++ b/app/src/main/java/com/aurora/store/ComposeActivity.kt
@@ -191,17 +191,27 @@ class ComposeActivity : FragmentActivity() {
     }
 
     private fun resolveStartDestination(): Screen {
-        // Parcel-based navigation (e.g. from NotificationUtil or DeepLinkConfirmActivity, which
-        // owns the external ACTION_VIEW market:// and play.google.com deep links)
-        IntentCompat.getParcelableExtra(intent, Screen.PARCEL_KEY, Screen::class.java)
-            ?.let { return it }
+        if (!Preferences.getBoolean(this, Preferences.PREFERENCE_INTRO)) {
+            return Screen.Onboarding
+        }
 
-        // SEND / SHOW_APP_INFO — getPackageName() handles both
-        intent.getPackageName()?.let { return Screen.AppDetails(it) }
+        // 1. קריאת שם החבילה מדיפ-לינק או Intent חיצוני
+        val targetPackage = intent.getPackageName()
+        if (!targetPackage.isNullOrBlank()) {
+            // ניתוב דרך Splash כדי להבטיח שסשן ההתחברות וה-Whitelist נטענו
+            return Screen.Splash(packageName = targetPackage)
+        }
+
+        // 2. ניווט מבוסס Parcel (התראות / מסכים פנימיים)
+        IntentCompat.getParcelableExtra(intent, Screen.PARCEL_KEY, Screen::class.java)?.let { screen ->
+            if (screen is Screen.AppDetails) {
+                return Screen.Splash(packageName = screen.packageName)
+            }
+            return screen
+        }
 
         return defaultStart()
     }
-
     private fun defaultStart(): Screen = when {
         !Preferences.getBoolean(this, Preferences.PREFERENCE_INTRO) -> Screen.Onboarding
         else -> Screen.Splash()
```

---

## 📅 עדכון: 2026-09-09 16:21:24 UTC
**הודעת קומיט:** Update DeepLinkConfirmActivity.kt
**קוד שינוי:** `f36abf548675deeb5f2db6c3a8e4c7d64a71f2ee`

### 📂 קבצים שהושפעו:
M	app/src/main/java/com/aurora/store/DeepLinkConfirmActivity.kt

### 📝 פירוט השינויים (Diff):
```diff
diff --git a/app/src/main/java/com/aurora/store/DeepLinkConfirmActivity.kt b/app/src/main/java/com/aurora/store/DeepLinkConfirmActivity.kt
index d7543fc..a0ec1ef 100644
--- a/app/src/main/java/com/aurora/store/DeepLinkConfirmActivity.kt
+++ b/app/src/main/java/com/aurora/store/DeepLinkConfirmActivity.kt
@@ -1,5 +1,6 @@
 /*
  * SPDX-FileCopyrightText: 2026 Aurora OSS
+ * SPDX-FileCopyrightText: 2025 The Calyx Institute
  * SPDX-License-Identifier: GPL-3.0-or-later
  */
 
@@ -7,21 +8,16 @@ package com.aurora.store
 
 import android.content.Intent
 import android.os.Bundle
+import android.widget.Toast
 import androidx.activity.compose.setContent
 import androidx.activity.enableEdgeToEdge
 import androidx.fragment.app.FragmentActivity
+import com.aurora.gplayapi.WhitelistManager
 import com.aurora.store.compose.navigation.Screen
 import com.aurora.store.compose.theme.AuroraTheme
 import com.aurora.store.compose.ui.sheets.DeepLinkConfirmSheet
 import com.aurora.store.util.Preferences
 
-/**
- * Translucent trampoline that gates external [Intent.ACTION_VIEW] app/developer listing deep links
- * (market:// and play.google.com links). These are the vector ads exploit to launch Aurora into a
- * listing without intent, so a Play Store-style confirmation sheet is shown floating over the
- * launching app before forwarding to [ComposeActivity]. When the user has opted out, or the intent
- * doesn't resolve to a listing, it forwards immediately without prompting.
- */
 class DeepLinkConfirmActivity : FragmentActivity() {
 
     override fun onCreate(savedInstanceState: Bundle?) {
@@ -29,6 +25,19 @@ class DeepLinkConfirmActivity : FragmentActivity() {
         super.onCreate(savedInstanceState)
 
         val target = resolveDeepLink()
+
+        // --- שער אבטחה ראשון: חסימה מיידית של אפליקציה שאינה ברשימה הלבנה ---
+        if (target is Screen.AppDetails) {
+            if (WhitelistManager.authorizedPackages.isNotEmpty() &&
+                !WhitelistManager.isAuthorized(target.packageName)
+            ) {
+                Toast.makeText(this, "אפליקציה זו אינה מורשית", Toast.LENGTH_LONG).show()
+                finish()
+                return
+            }
+        }
+        // ---------------------------------------------------------------------
+
         val shouldConfirm = target != null &&
             Preferences.getBoolean(this, Preferences.PREFERENCE_CONFIRM_EXTERNAL_DEEPLINK, true)
 
@@ -50,13 +59,6 @@ class DeepLinkConfirmActivity : FragmentActivity() {
         }
     }
 
-    /**
-     * Resolves the listing requested by the incoming ACTION_VIEW intent, or null when the intent
-     * carries no id. The action keyword ("details", "dev" or "developer") is the last path segment
-     * for play.google.com links and the host for market:// links. Both "dev" and "developer" links
-     * may carry either a numeric developer id (curated developer stream) or a developer name
-     * (publisher search), so the id is parsed to decide which one to open.
-     */
     private fun resolveDeepLink(): Screen? {
         if (intent.action != Intent.ACTION_VIEW) return null
 
@@ -72,11 +74,6 @@ class DeepLinkConfirmActivity : FragmentActivity() {
         }
     }
 
-    /**
-     * Best-effort human-readable name of the app that fired the intent, derived from the activity
-     * referrer. Resolves an android-app:// referrer to its app label, falling back to the raw host.
-     * Returns null when no referrer is available.
-     */
     private fun resolveReferrerLabel(): String? {
         val ref = referrer ?: return null
         val pkg = if (ref.scheme == "android-app") ref.host else null
@@ -94,9 +91,10 @@ class DeepLinkConfirmActivity : FragmentActivity() {
         startActivity(
             Intent(this, ComposeActivity::class.java).apply {
                 target?.let { putExtra(Screen.PARCEL_KEY, it) }
-                // Start ComposeActivity fresh so the parcel is honoured even when Aurora is already
-                // running; without this a reused instance keeps its current screen. Mirrors the
-                // deep-link PendingIntents in NotificationUtil.
+                // העברה מפורשת של ה-packageName כמחרוזת למניעת כשלים בסריאליזציה
+                if (target is Screen.AppDetails) {
+                    putExtra("packageName", target.packageName)
+                }
                 flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
             }
         )
```

---

## 📅 עדכון: 2026-09-09 15:43:56 UTC
**הודעת קומיט:** Merge pull request #21 from chanuta-dev/update-summary-docs

🤖 AI Update: docs: update summery_for_AI.md with latest changes from AI_CHANGES.md
**קוד שינוי:** `e75935342804635f9fc9988e79d0b9d82e0ffba3`

### 📂 קבצים שהושפעו:
M	summery_for_AI.md

### 📝 פירוט השינויים (Diff):
```diff
diff --git a/summery_for_AI.md b/summery_for_AI.md
index 8a5715a..c991770 100644
--- a/summery_for_AI.md
+++ b/summery_for_AI.md
@@ -62,4 +62,8 @@ The primary purpose of this fork is to operate as a **curated, filtered, and hyb
 5. Upon user confirmation, `AppSelfUpdater` downloads the release APK asset and prompts Android package installation.
 
 ---
-test
+
+## 📈 Progress & Recent Modifications
+- Updated build & workflow automation (`release.yml`, `ai_agent.yml`, `ai_tracker.yml`).
+- Added support for auto-tracking project tree and AI changes.
+- Integrated beta and production release options in GitHub Actions workflows.
```

---

## 📅 עדכון: 2026-09-09 14:35:02 UTC
**הודעת קומיט:** Fix permission type usage in AppDetailsScreen
**קוד שינוי:** `191f37e977b956acde7f4c6af51d3c4d07bb8dd1`

### 📂 קבצים שהושפעו:
M	app/src/main/java/com/aurora/store/compose/ui/details/AppDetailsScreen.kt

### 📝 פירוט השינויים (Diff):
```diff
diff --git a/app/src/main/java/com/aurora/store/compose/ui/details/AppDetailsScreen.kt b/app/src/main/java/com/aurora/store/compose/ui/details/AppDetailsScreen.kt
index 6de02e0..5c3229a 100644
--- a/app/src/main/java/com/aurora/store/compose/ui/details/AppDetailsScreen.kt
+++ b/app/src/main/java/com/aurora/store/compose/ui/details/AppDetailsScreen.kt
@@ -396,7 +396,7 @@ private fun ScreenContentApp(
         } else {
             isChecking = false
             val requiredPermissions = setOfNotNull(
-                PermissionType.INSTALL_UNKNOWNAPPS ?: PermissionType.INSTALL_UNKNOWN_APPS,
+                PermissionType.INSTALL_UNKNOWN_APPS,
                 if (app.fileList.requiresObbDir()) PermissionType.STORAGE_MANAGER else null,
                 if (app.fileList.requiresObbDir()) PermissionType.EXTERNAL_STORAGE else null
             )
```

---

## 📅 עדכון: 2026-09-09 14:28:10 UTC
**הודעת קומיט:** Update AppDetailsScreen.kt
**קוד שינוי:** `ef6256eed0daa8b05a48d1e14d7991c3245476ba`

### 📂 קבצים שהושפעו:
M	app/src/main/java/com/aurora/store/compose/ui/details/AppDetailsScreen.kt

### 📝 פירוט השינויים (Diff):
```diff
diff --git a/app/src/main/java/com/aurora/store/compose/ui/details/AppDetailsScreen.kt b/app/src/main/java/com/aurora/store/compose/ui/details/AppDetailsScreen.kt
index a5bacd7..6de02e0 100644
--- a/app/src/main/java/com/aurora/store/compose/ui/details/AppDetailsScreen.kt
+++ b/app/src/main/java/com/aurora/store/compose/ui/details/AppDetailsScreen.kt
@@ -303,7 +303,7 @@ private fun ScreenContentApp(
     onNavigateTo: (Destination) -> Unit = {},
     onLoadMoreCluster: (cluster: StreamCluster) -> Unit = {},
     accounts: List<Account> = emptyList(),
-    onDownload = { requestedApp: App -> viewModelEnqueue(requestedApp) },
+    onDownload: (requestedApp: App) -> Unit = {},
     onDownloadWith: (requestedApp: App, accountId: String) -> Unit = { _, _ -> },
     onFavorite: () -> Unit = {},
     onCancelDownload: () -> Unit = {},
@@ -396,7 +396,7 @@ private fun ScreenContentApp(
         } else {
             isChecking = false
             val requiredPermissions = setOfNotNull(
-                PermissionType.INSTALL_UNKNOWN_APPS,
+                PermissionType.INSTALL_UNKNOWNAPPS ?: PermissionType.INSTALL_UNKNOWN_APPS,
                 if (app.fileList.requiresObbDir()) PermissionType.STORAGE_MANAGER else null,
                 if (app.fileList.requiresObbDir()) PermissionType.EXTERNAL_STORAGE else null
             )
@@ -860,13 +860,13 @@ private fun AppDetailsScreenPreview(@PreviewParameter(AppPreviewProvider::class)
 @PreviewWrapper(ThemePreviewProvider::class)
 @Preview
 @Composable
-private fun ScreenContentLoading() {
+private fun AppDetailsScreenPreviewLoading() {
     ScreenContentLoading()
 }
 
 @PreviewWrapper(ThemePreviewProvider::class)
 @Preview
 @Composable
-private fun ScreenContentError() {
+private fun AppDetailsScreenPreviewError() {
     ScreenContentError()
 }
```

---

## 📅 עדכון: 2026-09-09 14:21:06 UTC
**הודעת קומיט:** Update AppDetailsScreen.kt
**קוד שינוי:** `c1c323d4536a2ec1199991f778a144f8e1b3b057`

### 📂 קבצים שהושפעו:
M	app/src/main/java/com/aurora/store/compose/ui/details/AppDetailsScreen.kt

### 📝 פירוט השינויים (Diff):
```diff
diff --git a/app/src/main/java/com/aurora/store/compose/ui/details/AppDetailsScreen.kt b/app/src/main/java/com/aurora/store/compose/ui/details/AppDetailsScreen.kt
index 2cfd35c..a5bacd7 100644
--- a/app/src/main/java/com/aurora/store/compose/ui/details/AppDetailsScreen.kt
+++ b/app/src/main/java/com/aurora/store/compose/ui/details/AppDetailsScreen.kt
@@ -88,11 +88,8 @@ import com.aurora.store.compose.ui.details.composable.DataSafety
 import com.aurora.store.compose.ui.details.composable.Details
 import com.aurora.store.compose.ui.details.composable.DeveloperDetails
 import com.aurora.store.compose.ui.details.composable.Privacy
-import com.aurora.store.compose.ui.details.composable.RatingAndReviews
-import com.aurora.store.compose.ui.details.composable.Screenshots
 import com.aurora.store.compose.ui.details.composable.Tags
 import com.aurora.store.compose.ui.details.composable.Testing
-import com.aurora.store.compose.ui.details.composable.UserReview
 import com.aurora.store.compose.ui.details.menu.AppDetailsMenu
 import com.aurora.store.compose.ui.details.menu.MenuItem
 import com.aurora.store.compose.ui.details.navigation.ExtraScreen
@@ -306,7 +303,7 @@ private fun ScreenContentApp(
     onNavigateTo: (Destination) -> Unit = {},
     onLoadMoreCluster: (cluster: StreamCluster) -> Unit = {},
     accounts: List<Account> = emptyList(),
-    onDownload: (requestedApp: App) -> Unit = {},
+    onDownload = { requestedApp: App -> viewModelEnqueue(requestedApp) },
     onDownloadWith: (requestedApp: App, accountId: String) -> Unit = { _, _ -> },
     onFavorite: () -> Unit = {},
     onCancelDownload: () -> Unit = {},
@@ -637,32 +634,6 @@ private fun ScreenContentApp(
                         )
                     }
 
-                    item {
-                        Screenshots(
-                            screenshots = app.screenshots,
-                            onNavigateToScreenshot = { showExtraPane(ExtraScreen.Screenshot(it)) }
-                        )
-                    }
-
-                    item {
-                        RatingAndReviews(
-                            rating = app.rating,
-                            featuredReviews = featuredReviews,
-                            onNavigateToDetailsReview = { showExtraPane(ExtraScreen.Review) }
-                        )
-                    }
-
-                    item {
-                        // Reviews can only be submitted by personal accounts for installed apps.
-                        if (!isAnonymous && app.isInstalled) {
-                            UserReview(
-                                review = userReview,
-                                onSubmit = onSubmitReview,
-                                onDelete = onDeleteReview
-                            )
-                        }
-                    }
-
                     item {
                         if (!isAnonymous && app.testingProgram?.isAvailable == true) {
                             Testing(
@@ -889,13 +860,13 @@ private fun AppDetailsScreenPreview(@PreviewParameter(AppPreviewProvider::class)
 @PreviewWrapper(ThemePreviewProvider::class)
 @Preview
 @Composable
-private fun AppDetailsScreenPreviewLoading() {
+private fun ScreenContentLoading() {
     ScreenContentLoading()
 }
 
 @PreviewWrapper(ThemePreviewProvider::class)
 @Preview
 @Composable
-private fun AppDetailsScreenPreviewError() {
+private fun ScreenContentError() {
     ScreenContentError()
 }
```

---

## 📅 עדכון: 2026-09-09 14:17:22 UTC
**הודעת קומיט:** Update summery_for_AI.md
**קוד שינוי:** `aef4e99b225eb1796b6ec8a57e9fdf26a74ab457`

### 📂 קבצים שהושפעו:
M	summery_for_AI.md

### 📝 פירוט השינויים (Diff):
```diff
diff --git a/summery_for_AI.md b/summery_for_AI.md
index aac8787..8a5715a 100644
--- a/summery_for_AI.md
+++ b/summery_for_AI.md
@@ -60,3 +60,6 @@ The primary purpose of this fork is to operate as a **curated, filtered, and hyb
 3. Compares latest available tag vs local `BuildConfig.VERSION_NAME`.
 4. If newer version exists, presents Compose `UpdateAvailableDialog` showing release notes and pre-release tag if applicable.
 5. Upon user confirmation, `AppSelfUpdater` downloads the release APK asset and prompts Android package installation.
+
+---
+test
```

---

## 📅 עדכון: 2026-09-08 00:25:04 UTC
**הודעת קומיט:** Update build.gradle.kts
**קוד שינוי:** `04fc17ff9a7d544b2bf5439d6328e3263d09a82f`

### 📂 קבצים שהושפעו:
M	app/build.gradle.kts

### 📝 פירוט השינויים (Diff):
```diff
diff --git a/app/build.gradle.kts b/app/build.gradle.kts
index 34e2394..7448d14 100644
--- a/app/build.gradle.kts
+++ b/app/build.gradle.kts
@@ -64,8 +64,12 @@ configure<ApplicationExtension> {
             version = release(37)
         }
 
-        versionCode = 76
-        versionName = "4.8.4"
+        // קריאה חכמה מ-GitHub Actions עם fallback לפיתוח מקומי
+        val envVersionCode = (System.getenv("APP_VERSION_CODE") ?: (project.findProperty("versionCode") as? String))?.toIntOrNull()
+        val envVersionName = System.getenv("APP_VERSION_NAME") ?: (project.findProperty("versionName") as? String)
+
+        versionCode = envVersionCode ?: 76
+        versionName = envVersionName ?: "4.8.4"
 
         buildConfigField("String", "EXODUS_API_KEY", "\"bbe6ebae4ad45a9cbacb17d69739799b8df2c7ae\"")
         buildConfigField("long", "BUILD_TIMESTAMP", "${lastCommitTimestamp.get()}L")
@@ -227,4 +231,4 @@ dependencies {
 
     // LeakCanary
     debugImplementation(libs.squareup.leakcanary.android)
-}
\ No newline at end of file
+}
```

---

## 📅 עדכון: 2026-09-07 22:12:38 UTC
**הודעת קומיט:** Update UpdatesPreferenceScreen.kt
**קוד שינוי:** `852b0886d506192b877bd6b8e564ff11dae89e6f`

### 📂 קבצים שהושפעו:
M	app/src/main/java/com/aurora/store/compose/ui/preferences/updates/UpdatesPreferenceScreen.kt

### 📝 פירוט השינויים (Diff):
```diff
diff --git a/app/src/main/java/com/aurora/store/compose/ui/preferences/updates/UpdatesPreferenceScreen.kt b/app/src/main/java/com/aurora/store/compose/ui/preferences/updates/UpdatesPreferenceScreen.kt
index 7d8634c..1d03b21 100644
--- a/app/src/main/java/com/aurora/store/compose/ui/preferences/updates/UpdatesPreferenceScreen.kt
+++ b/app/src/main/java/com/aurora/store/compose/ui/preferences/updates/UpdatesPreferenceScreen.kt
@@ -63,6 +63,7 @@ import com.aurora.store.util.Preferences.PREFERENCES_UPDATES_RESTRICTIONS_METERE
 import com.aurora.store.util.Preferences.PREFERENCE_FILTER_AURORA_ONLY
 import com.aurora.store.util.Preferences.PREFERENCE_FILTER_FDROID
 import com.aurora.store.util.Preferences.PREFERENCE_FILTER_INSTALLERS
+import com.aurora.store.util.Preferences.PREFERENCE_INCLUDE_BETA_UPDATES
 import com.aurora.store.util.Preferences.PREFERENCE_SELF_UPDATE_ENABLED
 import com.aurora.store.util.Preferences.PREFERENCE_UPDATES_AUTO
 import com.aurora.store.util.Preferences.PREFERENCE_UPDATES_CHECK_INTERVAL
@@ -117,6 +118,9 @@ private fun ScreenContent(
     var warnTrackers by remember {
         mutableStateOf(Preferences.getBoolean(context, PREFERENCE_UPDATES_WARN_TRACKERS, false))
     }
+    var includeBeta by remember {
+        mutableStateOf(Preferences.getBoolean(context, PREFERENCE_INCLUDE_BETA_UPDATES, false))
+    }
     val selfUpdateSupported = remember { PackageUtil.isSelfUpdateSupported(context) }
     var selfUpdateEnabled by remember {
         mutableStateOf(Preferences.getBoolean(context, PREFERENCE_SELF_UPDATE_ENABLED, true))
@@ -370,6 +374,29 @@ private fun ScreenContent(
                     }
                 )
             }
+            item {
+                ListItem(
+                    modifier = Modifier.clickable {
+                        includeBeta = !includeBeta
+                        context.save(PREFERENCE_INCLUDE_BETA_UPDATES, includeBeta)
+                    },
+                    headlineContent = {
+                        Text("קבלת עדכוני בטא (Pre-release)")
+                    },
+                    supportingContent = {
+                        Text("אפשר קבלת עדכוני ניסוי וגרסאות בטא ישירות מ-GitHub")
+                    },
+                    trailingContent = {
+                        Switch(
+                            checked = includeBeta,
+                            onCheckedChange = { checked ->
+                                includeBeta = checked
+                                context.save(PREFERENCE_INCLUDE_BETA_UPDATES, checked)
+                            }
+                        )
+                    }
+                )
+            }
             if (selfUpdateSupported) {
                 item {
                     fun onSelfUpdateChanged(enabled: Boolean) {
```

---

## 📅 עדכון: 2026-09-07 22:11:59 UTC
**הודעת קומיט:** Delete app/src/main/java/com/aurora/store/compose/ui/settings/UpdatesScreen.kt
**קוד שינוי:** `9f91bd20b9da26f9de1f4b125379480c3935dd19`

### 📂 קבצים שהושפעו:
D	app/src/main/java/com/aurora/store/compose/ui/settings/UpdatesScreen.kt

### 📝 פירוט השינויים (Diff):
```diff
diff --git a/app/src/main/java/com/aurora/store/compose/ui/settings/UpdatesScreen.kt b/app/src/main/java/com/aurora/store/compose/ui/settings/UpdatesScreen.kt
deleted file mode 100644
index b83c336..0000000
--- a/app/src/main/java/com/aurora/store/compose/ui/settings/UpdatesScreen.kt
+++ /dev/null
@@ -1,76 +0,0 @@
-/*
- * SPDX-FileCopyrightText: 2025 Aurora OSS
- * SPDX-License-Identifier: GPL-3.0-or-later
- */
-
-package com.aurora.store.compose.ui.settings
-
-import androidx.compose.foundation.layout.Column
-import androidx.compose.foundation.layout.fillMaxSize
-import androidx.compose.foundation.layout.padding
-import androidx.compose.foundation.rememberScrollState
-import androidx.compose.foundation.verticalScroll
-import androidx.compose.material3.ExperimentalMaterial3Api
-import androidx.compose.material3.Icon
-import androidx.compose.material3.IconButton
-import androidx.compose.material3.ListItem
-import androidx.compose.material3.Scaffold
-import androidx.compose.material3.Switch
-import androidx.compose.material3.Text
-import androidx.compose.material3.TopAppBar
-import androidx.compose.runtime.Composable
-import androidx.compose.runtime.getValue
-import androidx.compose.runtime.mutableStateOf
-import androidx.compose.runtime.remember
-import androidx.compose.runtime.setValue
-import androidx.compose.ui.Modifier
-import androidx.compose.ui.platform.LocalContext
-import androidx.compose.ui.res.painterResource
-import com.aurora.store.R
-import com.aurora.store.util.Preferences
-
-@OptIn(ExperimentalMaterial3Api::class)
-@Composable
-fun UpdatesScreen(onNavigateUp: () -> Unit) {
-    val context = LocalContext.current
-    var includeBeta by remember {
-        mutableStateOf(Preferences.getBoolean(context, Preferences.PREFERENCE_INCLUDE_BETA_UPDATES, false))
-    }
-
-    Scaffold(
-        topBar = {
-            TopAppBar(
-                title = { Text("הגדרות עדכונים") },
-                navigationIcon = {
-                    IconButton(onClick = onNavigateUp) {
-                        Icon(
-                            painter = painterResource(id = R.drawable.ic_arrow_back),
-                            contentDescription = "Back"
-                        )
-                    }
-                }
-            )
-        }
-    ) { innerPadding ->
-        Column(
-            modifier = Modifier
-                .fillMaxSize()
-                .padding(innerPadding)
-                .verticalScroll(rememberScrollState())
-        ) {
-            ListItem(
-                headlineContent = { Text("קבלת עדכוני בטא (Pre-release)") },
-                supportingContent = { Text("אפשר קבלת עדכוני ניסוי וגרסאות בטא ישירות מ-GitHub") },
-                trailingContent = {
-                    Switch(
-                        checked = includeBeta,
-                        onCheckedChange = { checked ->
-                            includeBeta = checked
-                            Preferences.setBoolean(context, Preferences.PREFERENCE_INCLUDE_BETA_UPDATES, checked)
-                        }
-                    )
-                }
-            )
-        }
-    }
-}
```

---

## 📅 עדכון: 2026-09-07 20:50:30 UTC
**הודעת קומיט:** Update summery_for_AI.md
**קוד שינוי:** `6201049aedc2564aae3a8f8fb0bebd637dfb4a1d`

### 📂 קבצים שהושפעו:
M	summery_for_AI.md

### 📝 פירוט השינויים (Diff):
```diff
diff --git a/summery_for_AI.md b/summery_for_AI.md
index ef28a2b..aac8787 100644
--- a/summery_for_AI.md
+++ b/summery_for_AI.md
@@ -60,4 +60,3 @@ The primary purpose of this fork is to operate as a **curated, filtered, and hyb
 3. Compares latest available tag vs local `BuildConfig.VERSION_NAME`.
 4. If newer version exists, presents Compose `UpdateAvailableDialog` showing release notes and pre-release tag if applicable.
 5. Upon user confirmation, `AppSelfUpdater` downloads the release APK asset and prompts Android package installation.
-ניסוי
```

---

## 📅 עדכון: $(date -u +"%Y-%m-%d %H:%M:%S UTC")
**הודעת קומיט:** Update summery_for_AI.md
**קוד שינוי:** `a33c188764fc2745aa140a78afabbc8ef479abd1`

### 📂 קבצים שהושפעו:
M	summery_for_AI.md

### 📝 פירוט השינויים (Diff):
```diff
diff --git a/summery_for_AI.md b/summery_for_AI.md
index aac8787..ef28a2b 100644
--- a/summery_for_AI.md
+++ b/summery_for_AI.md
@@ -60,3 +60,4 @@ The primary purpose of this fork is to operate as a **curated, filtered, and hyb
 3. Compares latest available tag vs local `BuildConfig.VERSION_NAME`.
 4. If newer version exists, presents Compose `UpdateAvailableDialog` showing release notes and pre-release tag if applicable.
 5. Upon user confirmation, `AppSelfUpdater` downloads the release APK asset and prompts Android package installation.
+ניסוי
```

---

