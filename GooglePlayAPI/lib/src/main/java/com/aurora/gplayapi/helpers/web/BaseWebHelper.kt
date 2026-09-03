/*
 * SPDX-FileCopyrightText: 2024 Aurora OSS
 * SPDX-FileCopyrightText: 2024 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.helpers.web

import com.aurora.gplayapi.data.builders.rpc.RpcBuilder
import com.aurora.gplayapi.data.models.App
import com.aurora.gplayapi.data.models.StreamBundle
import com.aurora.gplayapi.data.models.StreamCluster
import com.aurora.gplayapi.helpers.BaseHelper
import com.aurora.gplayapi.utils.dig
import java.util.Locale

abstract class BaseWebHelper : BaseHelper() {

    var locale: Locale? = null

    abstract fun with(locale: Locale?): BaseWebHelper

    fun execute(freq: String): HashMap<String, HashMap<String, Any>> {
        val response = WebClient(httpClient, locale).fetch(listOf(freq))

        return RpcBuilder.wrapResponse(response)
    }

    fun parseBundle(id: Int, category: String, payload: Any): StreamBundle {
        val clusters = payload.dig<List<Any>>(0, 1)
            .mapNotNull { entry ->
                val events = entry.dig<List<Any>>(34, 0)
                val topCharts = entry.dig<List<Any>>(27, 1)

                // Skip events and top charts clusters
                // TODO: Add support for events in the future
                when {
                    events.isNotEmpty() || topCharts.isNotEmpty() -> null
                    entry.dig<List<Any>>(21, 0).isNotEmpty() -> parseCluster(entry, 21)
                    entry.dig<List<Any>>(29, 0).isNotEmpty() -> parseCluster(entry, 29)
                    entry.dig<List<Any>>(22, 0).isNotEmpty() -> parseCluster(
                        entry,
                        22,
                        listOf(0, 0, 0)
                    )

                    else -> StreamCluster.EMPTY
                }
            }
            .filter { it.clusterAppList.isNotEmpty() }

        return StreamBundle(
            id = id,
            streamTitle = category,
            streamNextPageUrl = payload.dig<String>(0, 3, 1),
            streamClusters = clusters.associateBy { it.id }.toMutableMap()
        )
    }

    fun parseCluster(
        payload: Any,
        clusterIndex: Int,
        appIndex: List<Int> = listOf(0, 0)
    ): StreamCluster {
        return StreamCluster(
            // Use GSR id as cluster#id
            id = payload.dig<String>(3, 0).hashCode(),
            clusterTitle = payload.dig<String>(clusterIndex, 1, 0),
            clusterSubtitle = payload.dig<String>(clusterIndex, 1, 1),
            clusterNextPageUrl = payload.dig<String>(clusterIndex, 1, 3, 1),
            clusterBrowseUrl = payload.dig<String>(clusterIndex, 1, 2, 4, 2),
            clusterAppList = getAppDetails(
                payload.dig<List<Any>>(clusterIndex, 0).mapNotNull {
                    it.dig(
                        *appIndex.toTypedArray()
                    )
                }
            )
        )
    }

    fun getAppDetails(packageNames: List<String>): MutableList<App> {
        return WebAppDetailsHelper()
            .with(locale)
            .using(httpClient)
            .getAppByPackageName(packageNames)
            .toMutableList()
    }
}
