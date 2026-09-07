/*
 * SPDX-FileCopyrightText: 2025 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.aurora.store.R
import com.aurora.store.util.Preferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatesScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    var includeBeta by remember {
        mutableStateOf(Preferences.getBoolean(context, Preferences.PREFERENCE_INCLUDE_BETA_UPDATES, false))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("הגדרות עדכונים") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            ListItem(
                headlineContent = { Text("קבלת עדכוני בטא (Pre-release)") },
                supportingContent = { Text("אפשר קבלת עדכוני ניסוי וגרסאות בטא ישירות מ-GitHub") },
                trailingContent = {
                    Switch(
                        checked = includeBeta,
                        onCheckedChange = { checked ->
                            includeBeta = checked
                            Preferences.setBoolean(context, Preferences.PREFERENCE_INCLUDE_BETA_UPDATES, checked)
                        }
                    )
                }
            )
        }
    }
}
