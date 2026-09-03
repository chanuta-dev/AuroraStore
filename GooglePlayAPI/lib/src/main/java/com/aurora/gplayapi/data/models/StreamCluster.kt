/*
 * SPDX-FileCopyrightText: 2020-2024 Aurora OSS
 * SPDX-FileCopyrightText: 2023-2025 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.data.models

import android.os.Parcelable
import com.aurora.gplayapi.WhitelistManager
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class StreamCluster(
    val id: Int,
    val clusterTitle: String = "",
    val clusterSubtitle: String = "",
    val clusterNextPageUrl: String = "",
    val clusterBrowseUrl: String = "",
    // שינוי 1: הפכנו את ה-val ל-var כדי שנוכל לעדכן את הרשימה
    var clusterAppList: List<App> = emptyList()
) : Parcelable {

    // שינוי 2: הוספת בלוק init שחותך ומסנן את האפליקציות מיד עם קבלתן מהשרת
    init {
        clusterAppList = if (WhitelistManager.authorizedPackages.isNotEmpty()) {
            clusterAppList
                .filter { WhitelistManager.isAuthorized(it.packageName) }
                .distinctBy { it.packageName }
        } else {
            emptyList()
        }
    }

    companion object {
        val EMPTY = StreamCluster(id = -1)
    }

    fun hasNext(): Boolean {
        return clusterNextPageUrl.isNotBlank()
    }
}