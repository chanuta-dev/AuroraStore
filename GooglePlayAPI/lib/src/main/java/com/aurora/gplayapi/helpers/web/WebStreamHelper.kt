/*
 * SPDX-FileCopyrightText: 2024 Aurora OSS
 * SPDX-FileCopyrightText: 2024 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.helpers.web

import com.aurora.gplayapi.data.builders.rpc.FeaturedStreamBuilder
import com.aurora.gplayapi.data.builders.rpc.NextBundleBuilder
import com.aurora.gplayapi.data.builders.rpc.NextClusterBuilder
import com.aurora.gplayapi.data.models.StreamBundle
import com.aurora.gplayapi.data.models.StreamCluster
import com.aurora.gplayapi.helpers.contracts.StreamContract
import com.aurora.gplayapi.helpers.contracts.StreamContract.Category
import com.aurora.gplayapi.helpers.contracts.StreamContract.Type
import com.aurora.gplayapi.network.IHttpClient
import com.aurora.gplayapi.utils.dig
import java.util.Locale

class WebStreamHelper : BaseWebHelper(), StreamContract {

    override fun with(locale: Locale?) = apply {
        this.locale = locale
    }

    override fun using(httpClient: IHttpClient) = apply {
        this.httpClient = httpClient
    }

    /**
     * Fetches the stream cluster for the given category.
     * @param category The category to fetch the stream cluster for.
     * @return The stream bundle for the given category.
     * @see StreamBundle
     *
     * Possible categories:
     * - "APPLICATION" for Apps
     * - "GAME" for Games
     * + All known categories supported by Google Play Store
     */
    override fun fetch(type: Type, category: Category): StreamBundle {
        val webCategory = category.value
        val response = execute(FeaturedStreamBuilder.build(webCategory))
        val payload = response.dig<List<Any>>(
            FeaturedStreamBuilder.TAG,
            webCategory
        )

        if (payload.isEmpty()) {
            return StreamBundle.EMPTY
        }

        val id = (type.value + category.value).hashCode()
        return parseBundle(id, webCategory, payload)
    }

    /**
     * Fetches the next stream of the cluster.
     * @param nextPageUrl The URL to fetch the next stream cluster from.
     * @return The next stream cluster.
     * @see StreamCluster
     */
    override fun nextStreamCluster(id: Int, nextPageUrl: String): StreamCluster {
        val response = execute(NextClusterBuilder.build(nextPageUrl))

        val payload = response.dig<List<Any>>(
            NextClusterBuilder.TAG,
            nextPageUrl.hashCode().toString(),
            0 // For next stream cluster, the payload is a list of clusters even if it's just one cluster
        )

        if (payload.isEmpty()) {
            return StreamCluster.EMPTY
        }

        var streamCluster = parseCluster(payload, 21)

        if (streamCluster.clusterAppList.isEmpty()) {
            streamCluster = parseCluster(payload, 22, listOf(0, 0, 0))
        }

        return streamCluster.copy(id = id)
    }

    /**
     * Fetches the next stream of bundle.
     * @param category The category to fetch the next stream bundle for.
     * @param nextPageToken The token to fetch the next stream bundle from.
     * @return The next stream bundle.
     * @see StreamBundle
     */
    override fun nextStreamBundle(
        id: Int,
        category: Category,
        nextPageToken: String
    ): StreamBundle {
        val webCategory = category.value
        val response = execute(NextBundleBuilder.build(webCategory, nextPageToken))

        val payload = response.dig<List<Any>>(
            NextBundleBuilder.TAG,
            webCategory
        )

        if (payload.isEmpty()) {
            return StreamBundle.EMPTY
        }

        return parseBundle(id, webCategory, payload)
    }
}
