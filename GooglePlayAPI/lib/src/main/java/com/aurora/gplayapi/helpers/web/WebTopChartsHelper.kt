/*
 * SPDX-FileCopyrightText: 2024 Aurora OSS
 * SPDX-FileCopyrightText: 2024 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.helpers.web

import com.aurora.gplayapi.data.builders.rpc.TopChartsBuilder
import com.aurora.gplayapi.data.models.StreamCluster
import com.aurora.gplayapi.helpers.contracts.TopChartsContract
import com.aurora.gplayapi.network.IHttpClient
import com.aurora.gplayapi.utils.dig
import java.util.Locale

class WebTopChartsHelper : BaseWebHelper(), TopChartsContract {

    override fun with(locale: Locale?) = apply {
        this.locale = locale
    }

    override fun using(httpClient: IHttpClient) = apply {
        this.httpClient = httpClient
    }

    /**
     * Fetches the top charts for a given category and chart
     * @param category The category of the top charts
     * @param chart The chart of the top charts
     * @return The stream cluster of the top charts
     * @throws Exception If an error occurs while fetching the top charts
     * @see StreamCluster
     *
     * Possible values for chart:
     * - topselling_paid
     * - topselling_free
     * - topselling_new_free
     * - topselling_new_paid
     * - topgrossing
     * - movers_shakers
     * */
    @Throws(Exception::class)
    override fun getCluster(category: String, chart: String): StreamCluster {
        var webChart = chart

        // Remove apps_ prefix, web charts don't have it
        if (chart.startsWith("apps_")) {
            webChart = chart.substring(5)
        }

        val response = execute(TopChartsBuilder.build(category, webChart))

        val payload = response.dig<List<Any>>(
            TopChartsBuilder.TAG,
            "$category$webChart"
        )

        if (payload.isEmpty()) {
            return StreamCluster.EMPTY
        }

        val packageNames: List<String> =
            payload.dig<List<Any>>(0, 1, 0, 28, 0).let { entry ->
                entry.mapNotNull {
                    it.dig(0, 0, 0)
                }
            }

        if (packageNames.isEmpty()) {
            return StreamCluster.EMPTY
        }

        val apps = getAppDetails(packageNames)

        return StreamCluster(
            id = category.hashCode(),
            clusterTitle = category,
            clusterSubtitle = chart,
            clusterAppList = apps
        )
    }

    /**
     * Irrelevant for web client as it doesn't support pagination
     */
    override fun getNextStreamCluster(id: Int, nextPageUrl: String): StreamCluster {
        return StreamCluster.EMPTY
    }
}
