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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RootRecommendationDialog(
    isShizukuAvailable: Boolean,
    onEnableRoot: () -> Unit,
    onEnableShizuku: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "זוהתה גישת Root במכשיר")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "במכשיר שלך קיימת הרשאת Root (Superuser). הפעלת מצב התקנת Root מאפשרת לאורורה להתקין ולעדכן אפליקציות ברקע בצורה שקטה לחלוטין ללא צורך באישור ידני.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "💡 המלצה: שימוש ב-Shizuku",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "באפשרותך להפעיל את Shizuku ישירות דרך הרשאת Root. שילוב זה מספק את חוויית ההתקנה היציבה ביותר דרך מנהל החבילות של המערכת.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onEnableRoot,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "הפעל התקנת Root")
                }
                if (isShizukuAvailable) {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = onEnableShizuku,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "הפעל התקנת Shizuku")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "השאר כרגיל")
            }
        }
    )
}
