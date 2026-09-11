## 📅 עדכון: 2026-09-11 07:48:06 UTC
**הודעת קומיט:** Delete app/src/main/java/com/aurora/store/util/AppSelfUpdater.kt
**קוד שינוי:** `16f841fe3b7d6e1f4ae10e9fad93b1c2dff3c9c2`

### 📂 קבצים שהושפעו:
D	app/src/main/java/com/aurora/store/util/AppSelfUpdater.kt

### 📝 פירוט השינויים (Diff):
```diff
diff --git a/app/src/main/java/com/aurora/store/util/AppSelfUpdater.kt b/app/src/main/java/com/aurora/store/util/AppSelfUpdater.kt
deleted file mode 100644
index df9c1ec..0000000
--- a/app/src/main/java/com/aurora/store/util/AppSelfUpdater.kt
+++ /dev/null
@@ -1,84 +0,0 @@
-/*
- * SPDX-FileCopyrightText: 2026 Aurora OSS
- * SPDX-License-Identifier: GPL-3.0-or-later
- */
-
-package com.aurora.store.util
-
-import android.app.DownloadManager
-import android.content.BroadcastReceiver
-import android.content.Context
-import android.content.Intent
-import android.content.IntentFilter
-import android.net.Uri
-import android.os.Build
-import android.os.Environment
-import androidx.core.content.FileProvider
-import com.aurora.gplayapi.ReleaseInfo
-import java.io.File
-
-object AppSelfUpdater {
-    fun downloadAndInstall(context: Context, releaseInfo: ReleaseInfo) {
-        val destinationFile = File(
-            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
-            releaseInfo.fileName
-        )
-
-        if (destinationFile.exists()) {
-            destinationFile.delete()
-        }
-
-        val request = DownloadManager.Request(Uri.parse(releaseInfo.downloadUrl))
-            .setTitle("מוריד עדכון גרסה v${releaseInfo.versionName}")
-            .setDescription(releaseInfo.fileName)
-            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
-            .setDestinationUri(Uri.fromFile(destinationFile))
-            .setAllowedOverMetered(true)
-            .setAllowedOverRoaming(true)
-
-        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
-        val downloadId = downloadManager.enqueue(request)
-
-        val onComplete = object : BroadcastReceiver() {
-            override fun onReceive(ctxt: Context, intent: Intent) {
-                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
-                if (id == downloadId) {
-                    ctxt.unregisterReceiver(this)
-                    installApk(ctxt, destinationFile)
-                }
-            }
-        }
-
-        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
-            context.registerReceiver(
-                onComplete,
-                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
-                Context.RECEIVER_EXPORTED
-            )
-        } else {
-            context.registerReceiver(
-                onComplete,
-                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
-            )
-        }
-    }
-
-    private fun installApk(context: Context, apkFile: File) {
-        if (!apkFile.exists()) return
-
-        val intent = Intent(Intent.ACTION_VIEW)
-        val apkUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
-            FileProvider.getUriForFile(
-                context,
-                "${context.packageName}.selfupdate.fileprovider",
-                apkFile
-            )
-        } else {
-            Uri.fromFile(apkFile)
-        }
-
-        intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
-        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
-        context.startActivity(intent)
-    }
-}
```

---

## 📅 עדכון: 2026-09-11 07:44:57 UTC
**הודעת קומיט:** Update DownloadWorker.kt
**קוד שינוי:** `a386beeab632ebcb942d0651e0f7c4aa82173362`

### 📂 קבצים שהושפעו:
M	app/src/main/java/com/aurora/store/data/work/DownloadWorker.kt

### 📝 פירוט השינויים (Diff):
```diff
diff --git a/app/src/main/java/com/aurora/store/data/work/DownloadWorker.kt b/app/src/main/java/com/aurora/store/data/work/DownloadWorker.kt
index 89931ad..cbf900b 100644
--- a/app/src/main/java/com/aurora/store/data/work/DownloadWorker.kt
+++ b/app/src/main/java/com/aurora/store/data/work/DownloadWorker.kt
@@ -135,7 +135,9 @@ class DownloadWorker @AssistedInject constructor(
         // Fetch required data for download
         try {
             download = downloadDao.getDownload(inputData.getString(DownloadHelper.PACKAGE_NAME)!!)
-            purchaseHelper = resolvePurchaseHelper(download.packageName)
+            if (download.packageName != context.packageName) {
+                purchaseHelper = resolvePurchaseHelper(download.packageName)
+            }
         } catch (exception: Exception) {
             return onFailure(exception)
         }
@@ -159,13 +161,16 @@ class DownloadWorker @AssistedInject constructor(
         // Try to purchase the app if file list is empty. Surface any GPlayApi error
         // (e.g. AppNotPurchased, AppNotSupported, AppRemoved) as the failure cause so
         // the user sees the real reason instead of a generic "files not available".
-        notifyStatus(DownloadStatus.PURCHASING)
-        try {
-            download.fileList = download.fileList.ifEmpty {
-                purchase(download.packageName, download.versionCode, download.offerType)
+        // רק לאפליקציות רגילות פונים לגוגל פליי, לא לעדכון של החנות עצמה
+        if (download.packageName != context.packageName) {
+            notifyStatus(DownloadStatus.PURCHASING)
+            try {
+                download.fileList = download.fileList.ifEmpty {
+                    purchase(download.packageName, download.versionCode, download.offerType)
+                }
+            } catch (exception: Exception) {
+                return onFailure(exception)
             }
-        } catch (exception: Exception) {
-            return onFailure(exception)
         }
 
         // Bail out if file list is empty after purchase
@@ -648,10 +653,9 @@ class DownloadWorker @AssistedInject constructor(
         val file = PathUtil.getLocalFile(context, gFile, download)
         Log.i(TAG, "Verifying $file")
 
-        if (com.aurora.gplayapi.PatchedAppManager.isPatchedApp(download.packageName)) {
+        if (download.packageName == context.packageName || com.aurora.gplayapi.PatchedAppManager.isPatchedApp(download.packageName)) {
             return file.exists() && file.length() > 0
         }
-
         val algorithm = if (gFile.sha256.isBlank()) Algorithm.SHA1 else Algorithm.SHA256
         val expectedSha = if (algorithm == Algorithm.SHA1) gFile.sha1 else gFile.sha256
 
```

---

## 📅 עדכון: 2026-09-11 07:39:43 UTC
**הודעת קומיט:** Update DownloadHelper.kt
**קוד שינוי:** `525461b1218b948b905853915d35548f73d0d44c`

### 📂 קבצים שהושפעו:
M	app/src/main/java/com/aurora/store/data/helper/DownloadHelper.kt

### 📝 פירוט השינויים (Diff):
```diff
diff --git a/app/src/main/java/com/aurora/store/data/helper/DownloadHelper.kt b/app/src/main/java/com/aurora/store/data/helper/DownloadHelper.kt
index 2d65a31..32c8480 100644
--- a/app/src/main/java/com/aurora/store/data/helper/DownloadHelper.kt
+++ b/app/src/main/java/com/aurora/store/data/helper/DownloadHelper.kt
@@ -193,7 +193,38 @@ class DownloadHelper @Inject constructor(
     suspend fun enqueueStandalone(externalApk: ExternalApk) {
         enqueue(Download.fromExternalApk(externalApk))
     }
-
+    
+    /**
+     * Enqueues Aurora Store self-update for download & install via the store's regular pipeline.
+     */
+    suspend fun enqueueSelfUpdate(releaseInfo: com.aurora.gplayapi.ReleaseInfo) {
+        val playFile = com.aurora.gplayapi.data.models.PlayFile(
+            name = releaseInfo.fileName.ifBlank { "base.apk" },
+            url = releaseInfo.downloadUrl,
+            size = releaseInfo.size,
+            type = com.aurora.gplayapi.data.models.PlayFile.Type.BASE
+        )
+        val download = Download(
+            packageName = context.packageName,
+            versionCode = com.aurora.store.BuildConfig.VERSION_CODE.toLong() + 1,
+            offerType = 0,
+            isInstalled = true,
+            displayName = context.getString(com.aurora.store.R.string.app_name),
+            iconURL = "",
+            size = releaseInfo.size,
+            id = context.packageName.hashCode(),
+            status = DownloadStatus.QUEUED,
+            progress = 0,
+            speed = 0L,
+            timeRemaining = 0L,
+            totalFiles = 1,
+            downloadedFiles = 0,
+            fileList = listOf(playFile),
+            sharedLibs = emptyList()
+        )
+        enqueue(download)
+    }
+    
     /**
      * Inserts a new download row, but only when a (re)download is actually needed. For an
      * existing record of the same version this:
```

---

## 📅 עדכון: 2026-09-11 07:37:55 UTC
**הודעת קומיט:** Add size property to SelfUpdateManager data class
**קוד שינוי:** `35e151fe7b375996f99905f5ea86401b6d0f5619`

### 📂 קבצים שהושפעו:
M	GooglePlayAPI/lib/src/main/java/com/aurora/gplayapi/SelfUpdateManager.kt

### 📝 פירוט השינויים (Diff):
```diff
diff --git a/GooglePlayAPI/lib/src/main/java/com/aurora/gplayapi/SelfUpdateManager.kt b/GooglePlayAPI/lib/src/main/java/com/aurora/gplayapi/SelfUpdateManager.kt
index 989b885..046aef5 100644
--- a/GooglePlayAPI/lib/src/main/java/com/aurora/gplayapi/SelfUpdateManager.kt
+++ b/GooglePlayAPI/lib/src/main/java/com/aurora/gplayapi/SelfUpdateManager.kt
@@ -18,6 +18,7 @@ data class ReleaseInfo(
     val downloadUrl: String,
     val releaseNotes: String,
     val fileName: String,
+    val size: Long = 0L,
     val isPrerelease: Boolean = false
 )
 
@@ -105,13 +106,15 @@ object SelfUpdateManager {
         val assets = json.optJSONArray("assets") ?: return null
         var apkUrl: String? = null
         var apkName: String? = null
-
+        var apkSize: Long = 0L
+        
         for (i in 0 until assets.length()) {
             val asset = assets.getJSONObject(i)
             val name = asset.optString("name", "")
             if (name.endsWith(".apk", ignoreCase = true)) {
                 apkUrl = asset.optString("browser_download_url", "")
                 apkName = name
+                apkSize = asset.optLong("size", 0L)
                 break
             }
         }
@@ -122,6 +125,7 @@ object SelfUpdateManager {
                 downloadUrl = apkUrl,
                 releaseNotes = releaseNotes,
                 fileName = apkName ?: "AuroraStore-$latestVersionName.apk",
+                size = apkSize,
                 isPrerelease = isPrerelease
             )
         }
```

---

## 📅 עדכון: 2026-09-10 16:44:16 UTC
**הודעת קומיט:** Refactor RequestAppFooterCard to use simplified imports
**קוד שינוי:** `725148baa221690cf406b89bc2f31081807eedd8`

### 📂 קבצים שהושפעו:
M	app/src/main/java/com/aurora/store/compose/ui/search/SearchScreen.kt

### 📝 פירוט השינויים (Diff):
```diff
diff --git a/app/src/main/java/com/aurora/store/compose/ui/search/SearchScreen.kt b/app/src/main/java/com/aurora/store/compose/ui/search/SearchScreen.kt
index e3da3eb..65e94be 100644
--- a/app/src/main/java/com/aurora/store/compose/ui/search/SearchScreen.kt
+++ b/app/src/main/java/com/aurora/store/compose/ui/search/SearchScreen.kt
@@ -85,6 +85,15 @@ import kotlin.uuid.Uuid
 import kotlinx.coroutines.flow.MutableStateFlow
 import kotlinx.coroutines.flow.collectLatest
 import kotlinx.coroutines.launch
+import androidx.compose.foundation.clickable
+import androidx.compose.foundation.layout.Row
+import androidx.compose.foundation.shape.RoundedCornerShape
+import androidx.compose.material3.Button
+import androidx.compose.material3.Card
+import androidx.compose.material3.CardDefaults
+import androidx.compose.material3.MaterialTheme
+import androidx.compose.ui.text.font.FontWeight
+import androidx.compose.ui.unit.dp
 
 @Composable
 fun SearchScreen(
@@ -471,17 +480,18 @@ private fun SearchScreenPreview(@PreviewParameter(AppPreviewProvider::class) app
     val results = MutableStateFlow(PagingData.from(apps)).collectAsLazyPagingItems()
     ScreenContent(results = results)
 }
+
 @Composable
 private fun RequestAppFooterCard(onAction: () -> Unit) {
-    androidx.compose.material3.Card(
+    Card(
         modifier = Modifier
             .fillMaxWidth()
             .padding(vertical = 12.dp)
-            .androidx.compose.foundation.clickable { onAction() },
-        colors = androidx.compose.material3.CardDefaults.cardColors(
+            .clickable { onAction() },
+        colors = CardDefaults.cardColors(
             containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
         ),
-        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
+        shape = RoundedCornerShape(12.dp)
     ) {
         Row(
             modifier = Modifier
@@ -493,7 +503,7 @@ private fun RequestAppFooterCard(onAction: () -> Unit) {
             Column(modifier = Modifier.weight(1f)) {
                 Text(
                     text = "לא מצאת את מה שחיפשת?",
-                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
+                    fontWeight = FontWeight.Bold,
                     style = MaterialTheme.typography.bodyMedium
                 )
                 Text(
@@ -502,7 +512,7 @@ private fun RequestAppFooterCard(onAction: () -> Unit) {
                     color = MaterialTheme.colorScheme.onSurfaceVariant
                 )
             }
-            androidx.compose.material3.Button(onClick = onAction) {
+            Button(onClick = onAction) {
                 Text("בקש")
             }
         }
```

---

## 📅 עדכון: 2026-09-10 16:33:49 UTC
**הודעת קומיט:** Implement RequestAppFooterCard in SearchScreen

Added a footer card for app requests in the search results.
**קוד שינוי:** `be25376f60d21f036009a7f779b0a1b687b613d0`

### 📂 קבצים שהושפעו:
M	app/src/main/java/com/aurora/store/compose/ui/search/SearchScreen.kt

### 📝 פירוט השינויים (Diff):
```diff
diff --git a/app/src/main/java/com/aurora/store/compose/ui/search/SearchScreen.kt b/app/src/main/java/com/aurora/store/compose/ui/search/SearchScreen.kt
index aa03819..e3da3eb 100644
--- a/app/src/main/java/com/aurora/store/compose/ui/search/SearchScreen.kt
+++ b/app/src/main/java/com/aurora/store/compose/ui/search/SearchScreen.kt
@@ -272,6 +272,18 @@ private fun ScreenContent(
                                             )
                                         }
                                     }
+                                    // --- כרטיס בקשה בסוף תוצאות החיפוש ---
+                                    if (isSearching && results.itemCount > 0) {
+                                        item(key = "request_app_footer") {
+                                            RequestAppFooterCard(
+                                                onAction = {
+                                                    onNavigateTo(
+                                                        Destination.AppRequest(textFieldState.text.toString())
+                                                    )
+                                                }
+                                            )
+                                        }
+                                    }
                                 }
                                 ScrollHint(
                                     listState = listState,
@@ -459,3 +471,40 @@ private fun SearchScreenPreview(@PreviewParameter(AppPreviewProvider::class) app
     val results = MutableStateFlow(PagingData.from(apps)).collectAsLazyPagingItems()
     ScreenContent(results = results)
 }
+@Composable
+private fun RequestAppFooterCard(onAction: () -> Unit) {
+    androidx.compose.material3.Card(
+        modifier = Modifier
+            .fillMaxWidth()
+            .padding(vertical = 12.dp)
+            .androidx.compose.foundation.clickable { onAction() },
+        colors = androidx.compose.material3.CardDefaults.cardColors(
+            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
+        ),
+        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
+    ) {
+        Row(
+            modifier = Modifier
+                .fillMaxWidth()
+                .padding(16.dp),
+            verticalAlignment = Alignment.CenterVertically,
+            horizontalArrangement = Arrangement.SpaceBetween
+        ) {
+            Column(modifier = Modifier.weight(1f)) {
+                Text(
+                    text = "לא מצאת את מה שחיפשת?",
+                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
+                    style = MaterialTheme.typography.bodyMedium
+                )
+                Text(
+                    text = "חפש בכל החנות ובקש הוספה למערכת",
+                    style = MaterialTheme.typography.bodySmall,
+                    color = MaterialTheme.colorScheme.onSurfaceVariant
+                )
+            }
+            androidx.compose.material3.Button(onClick = onAction) {
+                Text("בקש")
+            }
+        }
+    }
+}
```

---

## 📅 עדכון: 2026-09-10 16:07:31 UTC
**הודעת קומיט:** Refactor comments and update image URL handling
**קוד שינוי:** `ac2ff2a76bf5ac97c57519befb95fcebabed01cc`

### 📂 קבצים שהושפעו:
M	app/src/main/java/com/aurora/store/compose/ui/request/AppRequestScreen.kt

### 📝 פירוט השינויים (Diff):
```diff
diff --git a/app/src/main/java/com/aurora/store/compose/ui/request/AppRequestScreen.kt b/app/src/main/java/com/aurora/store/compose/ui/request/AppRequestScreen.kt
index 3639d4a..729c24a 100644
--- a/app/src/main/java/com/aurora/store/compose/ui/request/AppRequestScreen.kt
+++ b/app/src/main/java/com/aurora/store/compose/ui/request/AppRequestScreen.kt
@@ -315,7 +315,7 @@ private fun AppIconWithPixelation(
             .background(MaterialTheme.colorScheme.primaryContainer),
         contentAlignment = Alignment.Center
     ) {
-        // גיבוי תמיד: אות ראשונה יפה במידה והתמונה חסומה ברשת/בנטפרי
+        // גיבוי תמיד: אות ראשונה אם התמונה לא זמינה או חסומה
         Text(
             text = title.take(1).uppercase(),
             fontWeight = FontWeight.Bold,
@@ -324,12 +324,9 @@ private fun AppIconWithPixelation(
         )
 
         if (iconUrl.isNotBlank()) {
-            // אם מפוקסל: נדגום מגוגל תמונה זעירה (s12) ונמתח אותה ללא החלקה לקבלת פסיפס פיקסלים מושלם
-            val finalUrl = if (isPixelated && iconUrl.contains("=")) {
-                iconUrl.substringBeforeLast("=") + "=s12"
-            } else {
-                iconUrl
-            }
+            // חיתוך סיומת קודמת אם קיימת, והוספת =s8 לקבלת 8x8 פיקסלים בלבד!
+            val cleanUrl = if (iconUrl.contains("=")) iconUrl.substringBeforeLast("=") else iconUrl
+            val finalUrl = if (isPixelated) "$cleanUrl=s8" else "$cleanUrl=s128"
 
             AsyncImage(
                 modifier = Modifier.fillMaxSize(),
@@ -339,6 +336,7 @@ private fun AppIconWithPixelation(
                     .build(),
                 contentDescription = null,
                 contentScale = ContentScale.Crop,
+                // ביטול החלקה במתיחה - מייצר קוביות פיקסלים חדות וגדולות
                 filterQuality = if (isPixelated) FilterQuality.None else FilterQuality.Medium
             )
         }
```

---

## 📅 עדכון: 2026-09-10 14:29:05 UTC
**הודעת קומיט:** Update AppRequestScreen.kt
**קוד שינוי:** `8b405d9e93fcaa2cc072e6bee16bba8a96a24f73`

### 📂 קבצים שהושפעו:
M	app/src/main/java/com/aurora/store/compose/ui/request/AppRequestScreen.kt

### 📝 פירוט השינויים (Diff):
```diff
diff --git a/app/src/main/java/com/aurora/store/compose/ui/request/AppRequestScreen.kt b/app/src/main/java/com/aurora/store/compose/ui/request/AppRequestScreen.kt
index db60e5d..3639d4a 100644
--- a/app/src/main/java/com/aurora/store/compose/ui/request/AppRequestScreen.kt
+++ b/app/src/main/java/com/aurora/store/compose/ui/request/AppRequestScreen.kt
@@ -18,12 +18,10 @@ import androidx.compose.foundation.layout.size
 import androidx.compose.foundation.layout.width
 import androidx.compose.foundation.lazy.LazyColumn
 import androidx.compose.foundation.lazy.items
-import androidx.compose.foundation.shape.CircleShape
 import androidx.compose.foundation.shape.RoundedCornerShape
 import androidx.compose.foundation.text.KeyboardActions
 import androidx.compose.foundation.text.KeyboardOptions
 import androidx.compose.material3.Button
-import androidx.compose.material3.ButtonDefaults
 import androidx.compose.material3.Checkbox
 import androidx.compose.material3.CircularProgressIndicator
 import androidx.compose.material3.ExperimentalMaterial3Api
@@ -57,8 +55,9 @@ import androidx.compose.ui.unit.dp
 import androidx.compose.ui.unit.sp
 import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
 import androidx.lifecycle.compose.collectAsStateWithLifecycle
-import coil.compose.SubcomposeAsyncImage
-import coil.request.ImageRequest
+import coil3.compose.AsyncImage
+import coil3.request.ImageRequest
+import coil3.request.crossfade
 import com.aurora.store.R
 import com.aurora.store.viewmodel.request.AppRequestItem
 import com.aurora.store.viewmodel.request.AppRequestViewModel
@@ -301,7 +300,7 @@ private fun AppRequestRow(
 }
 
 /**
- * מציג אייקון מפוקסל אמיתי בסגנון נטפרי על ידי דגימת 10x10 פיקסלים ומתיחה ללא החלקה
+ * מציג אייקון מפוקסל אמיתי בסגנון נטפרי
  */
 @Composable
 private fun AppIconWithPixelation(
@@ -309,54 +308,41 @@ private fun AppIconWithPixelation(
     iconUrl: String,
     isPixelated: Boolean
 ) {
-    val context = LocalContext.current
-
-    val imageRequest = remember(iconUrl, isPixelated) {
-        ImageRequest.Builder(context)
-            .data(iconUrl)
-            .crossfade(true)
-            .apply {
-                if (isPixelated) {
-                    // כיווץ ל-10x10 כדי לייצר פיקסלים גסים ומטושטשים
-                    size(10, 10)
-                }
-            }
-            .build()
-    }
-
-    SubcomposeAsyncImage(
-        model = imageRequest,
-        contentDescription = null,
-        // ביטול החלקה במתיחה ליצירת אפקט פסיפס / פיקסלים חדים
-        filterQuality = if (isPixelated) FilterQuality.None else FilterQuality.Medium,
-        contentScale = ContentScale.Crop,
+    Box(
         modifier = Modifier
             .size(46.dp)
-            .clip(RoundedCornerShape(10.dp)),
-        error = {
-            // גיבוי: אות ראשונה בתוך ריבוע אם התמונה חסומה בנטפרי/ברשת
-            Box(
-                modifier = Modifier
-                    .fillMaxSize()
-                    .background(MaterialTheme.colorScheme.primaryContainer),
-                contentAlignment = Alignment.Center
-            ) {
-                Text(
-                    text = title.take(1).uppercase(),
-                    fontWeight = FontWeight.Bold,
-                    fontSize = 18.sp,
-                    color = MaterialTheme.colorScheme.onPrimaryContainer
-                )
+            .clip(RoundedCornerShape(10.dp))
+            .background(MaterialTheme.colorScheme.primaryContainer),
+        contentAlignment = Alignment.Center
+    ) {
+        // גיבוי תמיד: אות ראשונה יפה במידה והתמונה חסומה ברשת/בנטפרי
+        Text(
+            text = title.take(1).uppercase(),
+            fontWeight = FontWeight.Bold,
+            fontSize = 18.sp,
+            color = MaterialTheme.colorScheme.onPrimaryContainer
+        )
+
+        if (iconUrl.isNotBlank()) {
+            // אם מפוקסל: נדגום מגוגל תמונה זעירה (s12) ונמתח אותה ללא החלקה לקבלת פסיפס פיקסלים מושלם
+            val finalUrl = if (isPixelated && iconUrl.contains("=")) {
+                iconUrl.substringBeforeLast("=") + "=s12"
+            } else {
+                iconUrl
             }
-        },
-        loading = {
-            Box(
-                modifier = Modifier
-                    .fillMaxSize()
-                    .background(MaterialTheme.colorScheme.surfaceVariant)
+
+            AsyncImage(
+                modifier = Modifier.fillMaxSize(),
+                model = ImageRequest.Builder(LocalContext.current)
+                    .data(finalUrl)
+                    .crossfade(true)
+                    .build(),
+                contentDescription = null,
+                contentScale = ContentScale.Crop,
+                filterQuality = if (isPixelated) FilterQuality.None else FilterQuality.Medium
             )
         }
-    )
+    }
 }
 
 @Composable
```

---

## 📅 עדכון: 2026-09-10 14:13:57 UTC
**הודעת קומיט:** Update AppRequestScreen.kt
**קוד שינוי:** `53307c4d3e1992b87fadf6c2d43fe71de177ad19`

### 📂 קבצים שהושפעו:
M	app/src/main/java/com/aurora/store/compose/ui/request/AppRequestScreen.kt

### 📝 פירוט השינויים (Diff):
```diff
diff --git a/app/src/main/java/com/aurora/store/compose/ui/request/AppRequestScreen.kt b/app/src/main/java/com/aurora/store/compose/ui/request/AppRequestScreen.kt
index 3175a04..db60e5d 100644
--- a/app/src/main/java/com/aurora/store/compose/ui/request/AppRequestScreen.kt
+++ b/app/src/main/java/com/aurora/store/compose/ui/request/AppRequestScreen.kt
@@ -166,7 +166,7 @@ fun AppRequestScreen(
                 singleLine = true,
                 leadingIcon = {
                     Icon(
-                        painter = painterResource(R.drawable.ic_search),
+                        painter = painterResource(R.drawable.ic_round_search),
                         contentDescription = null
                     )
                 },
```

---

## 📅 עדכון: 2026-09-10 14:13:07 UTC
**הודעת קומיט:** Fix syntax errors in SearchScreen.kt
**קוד שינוי:** `cbff2f40059b86d62f7a12313c930566855bbc8e`

### 📂 קבצים שהושפעו:
M	app/src/main/java/com/aurora/store/compose/ui/search/SearchScreen.kt

### 📝 פירוט השינויים (Diff):
```diff
diff --git a/app/src/main/java/com/aurora/store/compose/ui/search/SearchScreen.kt b/app/src/main/java/com/aurora/store/compose/ui/search/SearchScreen.kt
index ade8e58..aa03819 100644
--- a/app/src/main/java/com/aurora/store/compose/ui/search/SearchScreen.kt
+++ b/app/src/main/java/com/aurora/store/compose/ui/search/SearchScreen.kt
@@ -104,7 +104,7 @@ fun SearchScreen(
         onSearch = onSearchCallback,
         onFetchSuggestions = onFetchSuggestionsCallback,
         onFilter = { filter -> viewModel.filterResults(filter) },
-        isAnonymous = viewModel.authProvider.isAnonymous
+        isAnonymous = viewModel.authProvider.isAnonymous,
         onNavigateTo = onNavigateTo
     )
 }
@@ -116,8 +116,8 @@ private fun ScreenContent(
     onFetchSuggestions: (String) -> Unit = {},
     onSearch: (String) -> Unit = {},
     onFilter: (filter: SearchFilter) -> Unit = {},
-    isAnonymous: Boolean = true
-    nNavigateTo: (Destination) -> Unit = {}
+    isAnonymous: Boolean = true,
+    onNavigateTo: (Destination) -> Unit = {}
 ) {
     val activity = LocalActivity.current as? ComponentActivity
     val textFieldState = rememberTextFieldState()
```

---

## 📅 עדכון: 2026-09-10 14:03:01 UTC
**הודעת קומיט:** Update SearchScreen.kt
**קוד שינוי:** `334d851a010f13fc8e3c582876142f729c257b01`

### 📂 קבצים שהושפעו:
M	app/src/main/java/com/aurora/store/compose/ui/search/SearchScreen.kt

### 📝 פירוט השינויים (Diff):
```diff
diff --git a/app/src/main/java/com/aurora/store/compose/ui/search/SearchScreen.kt b/app/src/main/java/com/aurora/store/compose/ui/search/SearchScreen.kt
index d51d025..ade8e58 100644
--- a/app/src/main/java/com/aurora/store/compose/ui/search/SearchScreen.kt
+++ b/app/src/main/java/com/aurora/store/compose/ui/search/SearchScreen.kt
@@ -87,7 +87,10 @@ import kotlinx.coroutines.flow.collectLatest
 import kotlinx.coroutines.launch
 
 @Composable
-fun SearchScreen(viewModel: SearchViewModel = hiltViewModel()) {
+fun SearchScreen(
+    viewModel: SearchViewModel = hiltViewModel(),
+    onNavigateTo: (Destination) -> Unit = {}
+) {
     val suggestions by viewModel.suggestions.collectAsStateWithLifecycle()
     val results = viewModel.apps.collectAsLazyPagingItems()
 
@@ -102,6 +105,7 @@ fun SearchScreen(viewModel: SearchViewModel = hiltViewModel()) {
         onFetchSuggestions = onFetchSuggestionsCallback,
         onFilter = { filter -> viewModel.filterResults(filter) },
         isAnonymous = viewModel.authProvider.isAnonymous
+        onNavigateTo = onNavigateTo
     )
 }
 
@@ -113,6 +117,7 @@ private fun ScreenContent(
     onSearch: (String) -> Unit = {},
     onFilter: (filter: SearchFilter) -> Unit = {},
     isAnonymous: Boolean = true
+    nNavigateTo: (Destination) -> Unit = {}
 ) {
     val activity = LocalActivity.current as? ComponentActivity
     val textFieldState = rememberTextFieldState()
@@ -238,7 +243,13 @@ private fun ScreenContent(
                             Placeholder(
                                 modifier = Modifier.padding(paddingValues),
                                 painter = painterResource(R.drawable.ic_disclaimer),
-                                message = stringResource(R.string.no_apps_available)
+                                message = stringResource(R.string.no_apps_available),
+                                actionLabel = "בקש הוספת אפליקציה לחנות",
+                                onAction = {
+                                    onNavigateTo(
+                                        Destination.AppRequest(textFieldState.text.toString())
+                                    )
+                                }
                             )
                         } else {
                             val listState = rememberLazyListState()
```

---

## 📅 עדכון: 2026-09-10 13:55:19 UTC
**הודעת קומיט:** Add AppRequest screen navigation and update SearchScreen
**קוד שינוי:** `95f584eac3eb32f5e27ffe96ec714c120a7066e9`

### 📂 קבצים שהושפעו:
M	app/src/main/java/com/aurora/store/compose/navigation/NavDisplay.kt

### 📝 פירוט השינויים (Diff):
```diff
diff --git a/app/src/main/java/com/aurora/store/compose/navigation/NavDisplay.kt b/app/src/main/java/com/aurora/store/compose/navigation/NavDisplay.kt
index 8aff138..5334324 100644
--- a/app/src/main/java/com/aurora/store/compose/navigation/NavDisplay.kt
+++ b/app/src/main/java/com/aurora/store/compose/navigation/NavDisplay.kt
@@ -186,6 +186,9 @@ fun NavDisplay(startDestination: NavKey) {
             is Destination.AppUpdate -> Unit
             is Destination.StreamBrowse -> backstack.add(Screen.StreamBrowse(destination.cluster))
             is Destination.GoogleLogin -> backstack.add(Screen.GoogleLogin(destination.addAccount))
+            is Destination.AppRequest -> backstack.add(
+                Screen.AppRequest(destination.initialQuery)
+            )
 
             Destination.Search -> backstack.add(Screen.Search)
             Destination.Downloads -> backstack.add(Screen.Downloads)
@@ -304,7 +307,7 @@ fun NavDisplay(startDestination: NavKey) {
                             slideOutVertically(navSlideSpec) { it }
                     }
                 }
-            ) { SearchScreen() }
+            ) { SearchScreen(onNavigateTo = ::navigate) }
 
             entry<Screen.Splash> { screen ->
                 SplashScreen(
@@ -320,6 +323,13 @@ fun NavDisplay(startDestination: NavKey) {
                 )
             }
 
+            entry<Screen.AppRequest> { screen ->
+                com.aurora.store.compose.ui.request.AppRequestScreen(
+                    initialQuery = screen.initialQuery,
+                    onNavigateBack = { backstack.removeLastOrNull() }
+                )
+            }
+
             entry<Screen.Onboarding> { OnboardingScreen() }
             entry<Screen.Blacklist> { BlacklistScreen() }
             entry<Screen.Downloads> { DownloadsScreen(onNavigateTo = ::navigate) }
```

---

## 📅 עדכון: 2026-09-10 13:49:23 UTC
**הודעת קומיט:** Create AppRequestScreen.kt
**קוד שינוי:** `ff31bdb20742c1a10921ff3a27a8840631eba204`

### 📂 קבצים שהושפעו:
A	app/src/main/java/com/aurora/store/compose/ui/request/AppRequestScreen.kt

### 📝 פירוט השינויים (Diff):
```diff
diff --git a/app/src/main/java/com/aurora/store/compose/ui/request/AppRequestScreen.kt b/app/src/main/java/com/aurora/store/compose/ui/request/AppRequestScreen.kt
new file mode 100644
index 0000000..3175a04
--- /dev/null
+++ b/app/src/main/java/com/aurora/store/compose/ui/request/AppRequestScreen.kt
@@ -0,0 +1,377 @@
+package com.aurora.store.compose.ui.request
+
+import android.widget.Toast
+import androidx.compose.animation.AnimatedVisibility
+import androidx.compose.animation.slideInVertically
+import androidx.compose.animation.slideOutVertically
+import androidx.compose.foundation.background
+import androidx.compose.foundation.layout.Arrangement
+import androidx.compose.foundation.layout.Box
+import androidx.compose.foundation.layout.Column
+import androidx.compose.foundation.layout.Row
+import androidx.compose.foundation.layout.Spacer
+import androidx.compose.foundation.layout.fillMaxSize
+import androidx.compose.foundation.layout.fillMaxWidth
+import androidx.compose.foundation.layout.height
+import androidx.compose.foundation.layout.padding
+import androidx.compose.foundation.layout.size
+import androidx.compose.foundation.layout.width
+import androidx.compose.foundation.lazy.LazyColumn
+import androidx.compose.foundation.lazy.items
+import androidx.compose.foundation.shape.CircleShape
+import androidx.compose.foundation.shape.RoundedCornerShape
+import androidx.compose.foundation.text.KeyboardActions
+import androidx.compose.foundation.text.KeyboardOptions
+import androidx.compose.material3.Button
+import androidx.compose.material3.ButtonDefaults
+import androidx.compose.material3.Checkbox
+import androidx.compose.material3.CircularProgressIndicator
+import androidx.compose.material3.ExperimentalMaterial3Api
+import androidx.compose.material3.Icon
+import androidx.compose.material3.IconButton
+import androidx.compose.material3.MaterialTheme
+import androidx.compose.material3.OutlinedTextField
+import androidx.compose.material3.Scaffold
+import androidx.compose.material3.Surface
+import androidx.compose.material3.Text
+import androidx.compose.material3.TopAppBar
+import androidx.compose.runtime.Composable
+import androidx.compose.runtime.LaunchedEffect
+import androidx.compose.runtime.getValue
+import androidx.compose.runtime.mutableStateOf
+import androidx.compose.runtime.remember
+import androidx.compose.runtime.setValue
+import androidx.compose.ui.Alignment
+import androidx.compose.ui.Modifier
+import androidx.compose.ui.draw.clip
+import androidx.compose.ui.graphics.Color
+import androidx.compose.ui.graphics.FilterQuality
+import androidx.compose.ui.layout.ContentScale
+import androidx.compose.ui.platform.LocalContext
+import androidx.compose.ui.platform.LocalSoftwareKeyboardController
+import androidx.compose.ui.res.painterResource
+import androidx.compose.ui.text.font.FontWeight
+import androidx.compose.ui.text.input.ImeAction
+import androidx.compose.ui.text.style.TextOverflow
+import androidx.compose.ui.unit.dp
+import androidx.compose.ui.unit.sp
+import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
+import androidx.lifecycle.compose.collectAsStateWithLifecycle
+import coil.compose.SubcomposeAsyncImage
+import coil.request.ImageRequest
+import com.aurora.store.R
+import com.aurora.store.viewmodel.request.AppRequestItem
+import com.aurora.store.viewmodel.request.AppRequestViewModel
+
+@OptIn(ExperimentalMaterial3Api::class)
+@Composable
+fun AppRequestScreen(
+    initialQuery: String = "",
+    onNavigateBack: () -> Unit,
+    viewModel: AppRequestViewModel = hiltViewModel()
+) {
+    val context = LocalContext.current
+    val keyboardController = LocalSoftwareKeyboardController.current
+
+    val items by viewModel.items.collectAsStateWithLifecycle()
+    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
+    val isSubmitting by viewModel.isSubmitting.collectAsStateWithLifecycle()
+
+    var query by remember { mutableStateOf(initialQuery) }
+
+    LaunchedEffect(Unit) {
+        viewModel.toastEvent.collect { msg ->
+            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
+        }
+    }
+
+    LaunchedEffect(initialQuery) {
+        if (initialQuery.isNotBlank()) {
+            viewModel.search(initialQuery)
+        }
+    }
+
+    val selectedCount = items.count { it.isSelected && !it.isAuthorized && !it.isSubmitted }
+
+    Scaffold(
+        topBar = {
+            TopAppBar(
+                title = { Text("בקשת אפליקציות") },
+                navigationIcon = {
+                    IconButton(onClick = onNavigateBack) {
+                        Icon(
+                            painter = painterResource(R.drawable.ic_arrow_back),
+                            contentDescription = "חזרה"
+                        )
+                    }
+                }
+            )
+        },
+        bottomBar = {
+            AnimatedVisibility(
+                visible = selectedCount > 0,
+                enter = slideInVertically { it },
+                exit = slideOutVertically { it }
+            ) {
+                Surface(
+                    shadowElevation = 8.dp,
+                    color = MaterialTheme.colorScheme.surface
+                ) {
+                    Box(
+                        modifier = Modifier
+                            .fillMaxWidth()
+                            .padding(16.dp),
+                        contentAlignment = Alignment.Center
+                    ) {
+                        Button(
+                            onClick = { viewModel.submitSelected() },
+                            enabled = !isSubmitting,
+                            modifier = Modifier
+                                .fillMaxWidth()
+                                .height(50.dp)
+                        ) {
+                            if (isSubmitting) {
+                                CircularProgressIndicator(
+                                    modifier = Modifier.size(24.dp),
+                                    color = Color.White,
+                                    strokeWidth = 2.dp
+                                )
+                            } else {
+                                Text(
+                                    text = "שלח בקשה ($selectedCount ${if (selectedCount > 1) "אפליקציות" else "אפליקציה"})",
+                                    fontSize = 16.sp,
+                                    fontWeight = FontWeight.Bold
+                                )
+                            }
+                        }
+                    }
+                }
+            }
+        }
+    ) { paddingValues ->
+        Column(
+            modifier = Modifier
+                .fillMaxSize()
+                .padding(paddingValues)
+                .padding(horizontal = 16.dp)
+        ) {
+            // שורת חיפוש
+            OutlinedTextField(
+                value = query,
+                onValueChange = { query = it },
+                modifier = Modifier
+                    .fillMaxWidth()
+                    .padding(vertical = 8.dp),
+                placeholder = { Text("חפש שם אפליקציה לבקשה...") },
+                singleLine = true,
+                leadingIcon = {
+                    Icon(
+                        painter = painterResource(R.drawable.ic_search),
+                        contentDescription = null
+                    )
+                },
+                trailingIcon = {
+                    if (query.isNotEmpty()) {
+                        IconButton(onClick = {
+                            query = ""
+                            viewModel.search("")
+                        }) {
+                            Icon(
+                                painter = painterResource(R.drawable.ic_cancel),
+                                contentDescription = "נקה"
+                            )
+                        }
+                    }
+                },
+                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
+                keyboardActions = KeyboardActions(
+                    onSearch = {
+                        keyboardController?.hide()
+                        viewModel.search(query)
+                    }
+                ),
+                shape = RoundedCornerShape(12.dp)
+            )
+
+            // הסבר עדין
+            Text(
+                text = "סמן ב-✓ את האפליקציות שברצונך לבקש, והן יישלחו לבדיקת מנהל המערכת.",
+                style = MaterialTheme.typography.bodySmall,
+                color = MaterialTheme.colorScheme.onSurfaceVariant,
+                modifier = Modifier.padding(bottom = 12.dp)
+            )
+
+            if (isLoading) {
+                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
+                    CircularProgressIndicator()
+                }
+            } else if (items.isEmpty() && query.isNotBlank()) {
+                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
+                    Text(
+                        text = "לא נמצאו תוצאות. נסה לחפש מילות מפתח אחרות.",
+                        color = MaterialTheme.colorScheme.onSurfaceVariant
+                    )
+                }
+            } else {
+                LazyColumn(
+                    modifier = Modifier.fillMaxSize(),
+                    verticalArrangement = Arrangement.spacedBy(8.dp)
+                ) {
+                    items(items, key = { it.app.packageName }) { item ->
+                        AppRequestRow(
+                            item = item,
+                            onToggle = { viewModel.toggleSelection(item.app.packageName) }
+                        )
+                    }
+                    item { Spacer(modifier = Modifier.height(80.dp)) }
+                }
+            }
+        }
+    }
+}
+
+@Composable
+private fun AppRequestRow(
+    item: AppRequestItem,
+    onToggle: () -> Unit
+) {
+    val isActionable = !item.isAuthorized && !item.isSubmitted
+
+    Row(
+        modifier = Modifier
+            .fillMaxWidth()
+            .clip(RoundedCornerShape(12.dp))
+            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
+            .padding(12.dp),
+        verticalAlignment = Alignment.CenterVertically
+    ) {
+        // 1. Checkbox לבחירה
+        Checkbox(
+            checked = item.isSelected,
+            onCheckedChange = { if (isActionable) onToggle() },
+            enabled = isActionable
+        )
+
+        Spacer(modifier = Modifier.width(8.dp))
+
+        // 2. אייקון (מפוקסל כמו נטפרי אם האפליקציה לא מאושרת!)
+        AppIconWithPixelation(
+            title = item.app.displayName,
+            iconUrl = item.app.iconArtwork.url,
+            isPixelated = !item.isAuthorized
+        )
+
+        Spacer(modifier = Modifier.width(12.dp))
+
+        // 3. פרטי האפליקציה
+        Column(modifier = Modifier.weight(1f)) {
+            Text(
+                text = item.app.displayName,
+                fontWeight = FontWeight.SemiBold,
+                fontSize = 15.sp,
+                maxLines = 1,
+                overflow = TextOverflow.Ellipsis
+            )
+            Text(
+                text = item.app.developerName,
+                style = MaterialTheme.typography.bodySmall,
+                color = MaterialTheme.colorScheme.onSurfaceVariant,
+                maxLines = 1,
+                overflow = TextOverflow.Ellipsis
+            )
+            Text(
+                text = item.app.packageName,
+                fontSize = 11.sp,
+                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
+                maxLines = 1,
+                overflow = TextOverflow.Ellipsis
+            )
+        }
+
+        // 4. תגית סטטוס
+        when {
+            item.isAuthorized -> {
+                StatusBadge(text = "כבר בחנות", color = Color(0xFF16A34A), bg = Color(0xFFDCFCE7))
+            }
+            item.isSubmitted -> {
+                StatusBadge(text = "נשלח ✓", color = Color(0xFF2563EB), bg = Color(0xFFDBEAFE))
+            }
+        }
+    }
+}
+
+/**
+ * מציג אייקון מפוקסל אמיתי בסגנון נטפרי על ידי דגימת 10x10 פיקסלים ומתיחה ללא החלקה
+ */
+@Composable
+private fun AppIconWithPixelation(
+    title: String,
+    iconUrl: String,
+    isPixelated: Boolean
+) {
+    val context = LocalContext.current
+
+    val imageRequest = remember(iconUrl, isPixelated) {
+        ImageRequest.Builder(context)
+            .data(iconUrl)
+            .crossfade(true)
+            .apply {
+                if (isPixelated) {
+                    // כיווץ ל-10x10 כדי לייצר פיקסלים גסים ומטושטשים
+                    size(10, 10)
+                }
+            }
+            .build()
+    }
+
+    SubcomposeAsyncImage(
+        model = imageRequest,
+        contentDescription = null,
+        // ביטול החלקה במתיחה ליצירת אפקט פסיפס / פיקסלים חדים
+        filterQuality = if (isPixelated) FilterQuality.None else FilterQuality.Medium,
+        contentScale = ContentScale.Crop,
+        modifier = Modifier
+            .size(46.dp)
+            .clip(RoundedCornerShape(10.dp)),
+        error = {
+            // גיבוי: אות ראשונה בתוך ריבוע אם התמונה חסומה בנטפרי/ברשת
+            Box(
+                modifier = Modifier
+                    .fillMaxSize()
+                    .background(MaterialTheme.colorScheme.primaryContainer),
+                contentAlignment = Alignment.Center
+            ) {
+                Text(
+                    text = title.take(1).uppercase(),
+                    fontWeight = FontWeight.Bold,
+                    fontSize = 18.sp,
+                    color = MaterialTheme.colorScheme.onPrimaryContainer
+                )
+            }
+        },
+        loading = {
+            Box(
+                modifier = Modifier
+                    .fillMaxSize()
+                    .background(MaterialTheme.colorScheme.surfaceVariant)
+            )
+        }
+    )
+}
+
+@Composable
+private fun StatusBadge(text: String, color: Color, bg: Color) {
+    Box(
+        modifier = Modifier
+            .clip(RoundedCornerShape(6.dp))
+            .background(bg)
+            .padding(horizontal = 8.dp, vertical = 4.dp)
+    ) {
+        Text(
+            text = text,
+            color = color,
+            fontSize = 11.sp,
+            fontWeight = FontWeight.Bold
+        )
+    }
+}
```

---

## 📅 עדכון: 2026-09-10 13:46:40 UTC
**הודעת קומיט:** Add AppRequestViewModel for app request handling
**קוד שינוי:** `99c77ee8494eb95adb4139dde2e401fc7bc9a1f8`

### 📂 קבצים שהושפעו:
A	app/src/main/java/com/aurora/store/viewmodel/request/AppRequestViewModel.kt

### 📝 פירוט השינויים (Diff):
```diff
diff --git a/app/src/main/java/com/aurora/store/viewmodel/request/AppRequestViewModel.kt b/app/src/main/java/com/aurora/store/viewmodel/request/AppRequestViewModel.kt
new file mode 100644
index 0000000..a5a0219
--- /dev/null
+++ b/app/src/main/java/com/aurora/store/viewmodel/request/AppRequestViewModel.kt
@@ -0,0 +1,145 @@
+package com.aurora.store.viewmodel.request
+
+import androidx.lifecycle.ViewModel
+import androidx.lifecycle.viewModelScope
+import com.aurora.gplayapi.WhitelistManager
+import com.aurora.gplayapi.data.models.App
+import com.aurora.gplayapi.helpers.web.WebSearchHelper
+import dagger.hilt.android.lifecycle.HiltViewModel
+import java.net.HttpURLConnection
+import java.net.URL
+import javax.inject.Inject
+import kotlinx.coroutines.Dispatchers
+import kotlinx.coroutines.flow.MutableSharedFlow
+import kotlinx.coroutines.flow.MutableStateFlow
+import kotlinx.coroutines.flow.asSharedFlow
+import kotlinx.coroutines.flow.asStateFlow
+import kotlinx.coroutines.launch
+import kotlinx.coroutines.withContext
+import org.json.JSONArray
+import org.json.JSONObject
+
+data class AppRequestItem(
+    val app: App,
+    val isAuthorized: Boolean,
+    val isSelected: Boolean = false,
+    val isSubmitted: Boolean = false
+)
+
+@HiltViewModel
+class AppRequestViewModel @Inject constructor(
+    private val webSearchHelper: WebSearchHelper
+) : ViewModel() {
+
+    private val VERCEL_API_URL = "https://aurora-whitelist-chi.vercel.app/api/request-app"
+
+    private val _items = MutableStateFlow<List<AppRequestItem>>(emptyList())
+    val items = _items.asStateFlow()
+
+    private val _isLoading = MutableStateFlow(false)
+    val isLoading = _isLoading.asStateFlow()
+
+    private val _isSubmitting = MutableStateFlow(false)
+    val isSubmitting = _isSubmitting.asStateFlow()
+
+    private val _toastEvent = MutableSharedFlow<String>()
+    val toastEvent = _toastEvent.asSharedFlow()
+
+    fun search(query: String) {
+        val trimmed = query.trim()
+        if (trimmed.isEmpty()) {
+            _items.value = emptyList()
+            return
+        }
+
+        viewModelScope.launch(Dispatchers.IO) {
+            _isLoading.value = true
+            try {
+                // חיפוש גולמי ללא סינון ה-Whitelist
+                val cluster = webSearchHelper.search(trimmed, filterWhitelist = false)
+                val mapped = cluster.clusterAppList.map { app ->
+                    AppRequestItem(
+                        app = app,
+                        isAuthorized = WhitelistManager.isAuthorized(app.packageName),
+                        isSelected = false,
+                        isSubmitted = false
+                    )
+                }
+                _items.value = mapped
+            } catch (_: Exception) {
+                _items.value = emptyList()
+            } finally {
+                _isLoading.value = false
+            }
+        }
+    }
+
+    fun toggleSelection(packageName: String) {
+        _items.value = _items.value.map { item ->
+            if (item.app.packageName == packageName && !item.isAuthorized && !item.isSubmitted) {
+                item.copy(isSelected = !item.isSelected)
+            } else {
+                item
+            }
+        }
+    }
+
+    fun submitSelected() {
+        val selectedItems = _items.value.filter { it.isSelected && !itemAlreadySubmitted(it) }
+        if (selectedItems.isEmpty()) return
+
+        viewModelScope.launch(Dispatchers.IO) {
+            _isSubmitting.value = true
+            try {
+                val jsonBody = JSONObject().apply {
+                    val appsArray = JSONArray()
+                    selectedItems.forEach { item ->
+                        val appObj = JSONObject().apply {
+                            put("packageName", item.app.packageName)
+                            put("title", item.app.displayName)
+                            put("iconUrl", item.app.iconArtwork.url)
+                        }
+                        appsArray.put(appObj)
+                    }
+                    put("apps", appsArray)
+                }
+
+                val url = URL(VERCEL_API_URL)
+                val conn = (url.openConnection() as HttpURLConnection).apply {
+                    requestMethod = "POST"
+                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
+                    setRequestProperty("Accept", "application/json")
+                    doOutput = true
+                    connectTimeout = 10000
+                    readTimeout = 10000
+                }
+
+                conn.outputStream.use { os ->
+                    os.write(jsonBody.toString().toByteArray(Charsets.UTF_8))
+                }
+
+                val responseCode = conn.responseCode
+                if (responseCode in 200..299) {
+                    val selectedPackages = selectedItems.map { it.app.packageName }.toSet()
+                    _items.value = _items.value.map { item ->
+                        if (selectedPackages.contains(item.app.packageName)) {
+                            item.copy(isSelected = false, isSubmitted = true)
+                        } else {
+                            item
+                        }
+                    }
+                    _toastEvent.emit("הבקשות נשלחו בהצלחה לבדיקת מנהל המערכת!")
+                } else {
+                    _toastEvent.emit("שגיאה בשליחת הבקשות. קוד: $responseCode")
+                }
+            } catch (e: Exception) {
+                _toastEvent.emit("שגיאת רשת בשליחת הבקשות")
+            } finally {
+                _isSubmitting.value = false
+            }
+        }
+    }
+
+    private fun itemAlreadySubmitted(item: AppRequestItem): Boolean =
+        item.isAuthorized || item.isSubmitted
+}
```

---

## 📅 עדכון: 2026-09-10 13:42:29 UTC
**הודעת קומיט:** Add AppRequest data class to Screen
**קוד שינוי:** `ac5d521f0f6eb240677815bea3ce393209f285f5`

### 📂 קבצים שהושפעו:
M	app/src/main/java/com/aurora/store/compose/navigation/Screen.kt

### 📝 פירוט השינויים (Diff):
```diff
diff --git a/app/src/main/java/com/aurora/store/compose/navigation/Screen.kt b/app/src/main/java/com/aurora/store/compose/navigation/Screen.kt
index 7025155..e52dddc 100644
--- a/app/src/main/java/com/aurora/store/compose/navigation/Screen.kt
+++ b/app/src/main/java/com/aurora/store/compose/navigation/Screen.kt
@@ -37,7 +37,10 @@ sealed class Screen : NavKey, Parcelable {
 
     @Serializable
     data object Search : Screen()
-
+    
+    @Serializable
+    data class AppRequest(val initialQuery: String = "") : Screen()
+    
     @Serializable
     data class PermissionRationale(val requiredPermissions: Set<PermissionType>) : Screen()
 
```

---

## 📅 עדכון: 2026-09-10 13:41:35 UTC
**הודעת קומיט:** Update Destination.kt
**קוד שינוי:** `91d6982c9b80ba48849917a67687b28b9b483adf`

### 📂 קבצים שהושפעו:
M	app/src/main/java/com/aurora/store/compose/navigation/Destination.kt

### 📝 פירוט השינויים (Diff):
```diff
diff --git a/app/src/main/java/com/aurora/store/compose/navigation/Destination.kt b/app/src/main/java/com/aurora/store/compose/navigation/Destination.kt
index e6948c7..19126de 100644
--- a/app/src/main/java/com/aurora/store/compose/navigation/Destination.kt
+++ b/app/src/main/java/com/aurora/store/compose/navigation/Destination.kt
@@ -23,6 +23,7 @@ sealed class Destination {
     data class AppUpdate(val update: Update) : Destination()
 
     data object Search : Destination()
+    data class AppRequest(val initialQuery: String = "") : Destination()
     data object Downloads : Destination()
 
     data class StreamBrowse(val cluster: StreamCluster) : Destination()
```

---

## 📅 עדכון: 2026-09-10 13:01:56 UTC
**הודעת קומיט:** Add filterWhitelist parameter to search methods
**קוד שינוי:** `e5a10ef18e59762c1c65bb3787642fe5c957c340`

### 📂 קבצים שהושפעו:
M	GooglePlayAPI/lib/src/main/java/com/aurora/gplayapi/helpers/web/WebSearchHelper.kt

### 📝 פירוט השינויים (Diff):
```diff
diff --git a/GooglePlayAPI/lib/src/main/java/com/aurora/gplayapi/helpers/web/WebSearchHelper.kt b/GooglePlayAPI/lib/src/main/java/com/aurora/gplayapi/helpers/web/WebSearchHelper.kt
index 6b9448e..89c449d 100644
--- a/GooglePlayAPI/lib/src/main/java/com/aurora/gplayapi/helpers/web/WebSearchHelper.kt
+++ b/GooglePlayAPI/lib/src/main/java/com/aurora/gplayapi/helpers/web/WebSearchHelper.kt
@@ -50,7 +50,12 @@ class WebSearchHelper : BaseWebHelper(), SearchContract {
     }
 
     override fun searchResults(query: String, nextPageUrl: String): StreamBundle {
-        val cluster = search(query)
+        return searchResults(query, nextPageUrl, filterWhitelist = true)
+    }
+
+    // גרסה עם תמיכה בעקיפת סינון עבור מסך בקשת אפליקציות
+    fun searchResults(query: String, nextPageUrl: String = "", filterWhitelist: Boolean = true): StreamBundle {
+        val cluster = search(query, nextPageUrl, filterWhitelist)
 
         return StreamBundle(
             id = UUID.randomUUID().hashCode(),
@@ -62,15 +67,14 @@ class WebSearchHelper : BaseWebHelper(), SearchContract {
     }
 
     override fun nextStreamBundle(query: String, nextPageUrl: String): StreamBundle {
-        // Web does not support pagination in the same way as native API, there is only one stream.
         return StreamBundle.EMPTY
     }
 
     override fun nextStreamCluster(query: String, nextPageUrl: String): StreamCluster {
-        return search(query, nextPageUrl)
+        return search(query, nextPageUrl, filterWhitelist = true)
     }
 
-    fun search(query: String, nextPageUrl: String = ""): StreamCluster {
+    fun search(query: String, nextPageUrl: String = "", filterWhitelist: Boolean = true): StreamCluster {
         val response = execute(SearchQueryBuilder.build(query, nextPageUrl))
 
         var payload = response.dig<List<Any>>(
@@ -83,12 +87,10 @@ class WebSearchHelper : BaseWebHelper(), SearchContract {
             return StreamCluster.EMPTY
         }
 
-        // First stream is search stream, following are app streams (made-up names :p)
         if (payload.dig<String>(0, 1) != "Apps") {
             payload = payload.dig(1, 0)
         }
 
-        // Find only the package names, complete app info is fetched via AppDetailsHelper
         val packageNames: List<String> = payload.dig<List<Any>>(0, 0).let { entry ->
             entry.mapNotNull {
                 it.dig(12, 0)
@@ -105,7 +107,8 @@ class WebSearchHelper : BaseWebHelper(), SearchContract {
             id = UUID.randomUUID().hashCode(),
             clusterTitle = query,
             clusterNextPageUrl = nextPageToken,
-            clusterAppList = getAppDetails(packageNames)
+            clusterAppList = getAppDetails(packageNames),
+            filterWhitelist = filterWhitelist
         )
     }
 }
```

---

## 📅 עדכון: 2026-09-10 13:01:12 UTC
**הודעת קומיט:** Modify StreamCluster to support app list updates

Updated clusterAppList to allow modification and added filterWhitelist flag for controlling app filtering.
**קוד שינוי:** `96c118a8298aaab4fd4b149cdbecff6245b5f8b9`

### 📂 קבצים שהושפעו:
M	GooglePlayAPI/lib/src/main/java/com/aurora/gplayapi/data/models/StreamCluster.kt

### 📝 פירוט השינויים (Diff):
```diff
diff --git a/GooglePlayAPI/lib/src/main/java/com/aurora/gplayapi/data/models/StreamCluster.kt b/GooglePlayAPI/lib/src/main/java/com/aurora/gplayapi/data/models/StreamCluster.kt
index ecde7fb..0046f0e 100644
--- a/GooglePlayAPI/lib/src/main/java/com/aurora/gplayapi/data/models/StreamCluster.kt
+++ b/GooglePlayAPI/lib/src/main/java/com/aurora/gplayapi/data/models/StreamCluster.kt
@@ -19,18 +19,20 @@ data class StreamCluster(
     val clusterSubtitle: String = "",
     val clusterNextPageUrl: String = "",
     val clusterBrowseUrl: String = "",
-    // שינוי 1: הפכנו את ה-val ל-var כדי שנוכל לעדכן את הרשימה
-    var clusterAppList: List<App> = emptyList()
+    var clusterAppList: List<App> = emptyList(),
+    // דגל שמאפשר לכבות את הסינון אך ורק במסך הבקשות (ברירת מחדל: תמיד מסונן!)
+    val filterWhitelist: Boolean = true
 ) : Parcelable {
 
-    // שינוי 2: הוספת בלוק init שחותך ומסנן את האפליקציות מיד עם קבלתן מהשרת
     init {
-        clusterAppList = if (WhitelistManager.authorizedPackages.isNotEmpty()) {
-            clusterAppList
-                .filter { WhitelistManager.isAuthorized(it.packageName) }
-                .distinctBy { it.packageName }
-        } else {
-            emptyList()
+        if (filterWhitelist) {
+            clusterAppList = if (WhitelistManager.authorizedPackages.isNotEmpty()) {
+                clusterAppList
+                    .filter { WhitelistManager.isAuthorized(it.packageName) }
+                    .distinctBy { it.packageName }
+            } else {
+                emptyList()
+            }
         }
     }
 
@@ -41,4 +43,4 @@ data class StreamCluster(
     fun hasNext(): Boolean {
         return clusterNextPageUrl.isNotBlank()
     }
-}
\ No newline at end of file
+}
```

---

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

