/*
 * SPDX-FileCopyrightText: 2026 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.ui.dialog

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aurora.gplayapi.ReleaseInfo

@Composable
fun UpdateAvailableDialog(
    releaseInfo: ReleaseInfo,
    onUpdate: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "עדכון גרסה זמין (v${releaseInfo.versionName})")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "גרסה חדשה של האפליקציה זמינה להורדה.",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (releaseInfo.releaseNotes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "מה חדש:",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = releaseInfo.releaseNotes,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onUpdate) {
                Text(text = "עדכן עכשיו")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "מאוחר יותר")
            }
        }
    )
}
