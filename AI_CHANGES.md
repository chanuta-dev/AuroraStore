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

