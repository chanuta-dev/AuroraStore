package com.aurora.store.compose.composable.updater

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aurora.store.BuildConfig
import com.aurora.store.data.updater.SelfUpdateManager

@Composable
fun SelfUpdateDialog() {
    val updateInfo by SelfUpdateManager.updateAvailable.collectAsState()
    val context = LocalContext.current

    val release = updateInfo ?: return

    AlertDialog(
        onDismissRequest = { SelfUpdateManager.dismissUpdate() },
        title = {
            Text(
                text = "עדכון חדש זמין עבור החנות",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "גרסה חדשה: ${release.versionName} (נוכחית: ${BuildConfig.VERSION_NAME})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                if (release.changelog.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "מה חדש:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = release.changelog,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    SelfUpdateManager.openDownload(context, release.downloadUrl)
                    SelfUpdateManager.dismissUpdate()
                }
            ) {
                Text("הורד ועדכן")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { SelfUpdateManager.dismissUpdate() }
            ) {
                Text("מאוחר יותר")
            }
        }
    )
}
