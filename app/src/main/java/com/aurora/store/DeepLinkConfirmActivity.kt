/*
 * SPDX-FileCopyrightText: 2026 Aurora OSS
 * SPDX-FileCopyrightText: 2025 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.aurora.gplayapi.WhitelistManager
import com.aurora.store.compose.navigation.Screen
import com.aurora.store.compose.theme.AuroraTheme
import com.aurora.store.compose.ui.sheets.DeepLinkConfirmSheet
import com.aurora.store.util.Preferences

class DeepLinkConfirmActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val target = resolveDeepLink()

        // --- שער אבטחה ראשון: חסימה מיידית של אפליקציה שאינה ברשימה הלבנה ---
        if (target is Screen.AppDetails) {
            if (WhitelistManager.authorizedPackages.isNotEmpty() &&
                !WhitelistManager.isAuthorized(target.packageName)
            ) {
                Toast.makeText(this, "אפליקציה זו אינה מורשית", Toast.LENGTH_LONG).show()
                finish()
                return
            }
        }
        // ---------------------------------------------------------------------

        val shouldConfirm = target != null &&
            Preferences.getBoolean(this, Preferences.PREFERENCE_CONFIRM_EXTERNAL_DEEPLINK, true)

        if (!shouldConfirm) {
            forwardToAurora(target)
            return
        }

        val referrerLabel = resolveReferrerLabel()
        setContent {
            AuroraTheme {
                DeepLinkConfirmSheet(
                    targetLabel = target.deepLinkLabel(),
                    sourceLabel = referrerLabel,
                    onOpen = { forwardToAurora(target) },
                    onDismiss = { finish() }
                )
            }
        }
    }

    private fun resolveDeepLink(): Screen? {
        if (intent.action != Intent.ACTION_VIEW) return null

        val data = intent.data ?: return null
        val id = data.getQueryParameter("id") ?: return null
        return when (data.lastPathSegment ?: data.host) {
            "dev", "developer" -> when {
                id.toLongOrNull() != null -> Screen.DevProfile(id)
                else -> Screen.PublisherProfile(id)
            }

            else -> Screen.AppDetails(id)
        }
    }

    private fun resolveReferrerLabel(): String? {
        val ref = referrer ?: return null
        val pkg = if (ref.scheme == "android-app") ref.host else null
        if (pkg != null) {
            return runCatching {
                packageManager.getApplicationLabel(
                    packageManager.getApplicationInfo(pkg, 0)
                ).toString()
            }.getOrDefault(pkg)
        }
        return ref.host ?: ref.toString()
    }

    private fun forwardToAurora(target: Screen?) {
        startActivity(
            Intent(this, ComposeActivity::class.java).apply {
                target?.let { putExtra(Screen.PARCEL_KEY, it) }
                // העברה מפורשת של ה-packageName כמחרוזת למניעת כשלים בסריאליזציה
                if (target is Screen.AppDetails) {
                    putExtra("packageName", target.packageName)
                }
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        finish()
    }

    private fun Screen.deepLinkLabel(): String = when (this) {
        is Screen.AppDetails -> packageName
        is Screen.DevProfile -> developerId
        is Screen.PublisherProfile -> publisherId
        else -> ""
    }
}
