/*
 * SPDX-FileCopyrightText: 2024 Aurora OSS
 * SPDX-FileCopyrightText: 2024-2025 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.helpers.web

import com.aurora.gplayapi.SearchSuggestEntry
import com.aurora.gplayapi.data.builders.rpc.SearchQueryBuilder
import com.aurora.gplayapi.data.builders.rpc.SearchSuggestionQueryBuilder
import com.aurora.gplayapi.data.models.StreamBundle
import com.aurora.gplayapi.data.models.StreamCluster
import com.aurora.gplayapi.helpers.contracts.SearchContract
import com.aurora.gplayapi.network.IHttpClient
import com.aurora.gplayapi.utils.dig
import java.util.Locale
import java.util.UUID

class WebSearchHelper : BaseWebHelper(), SearchContract {

    override fun with(locale: Locale?) = apply {
        this.locale = locale
    }

    override fun using(httpClient: IHttpClient) = apply {
        this.httpClient = httpClient
    }

    override fun searchSuggestions(query: String): List<SearchSuggestEntry> {
        val response = execute(SearchSuggestionQueryBuilder.build(query))

        val payload = response.dig<List<Any>>(
            SearchSuggestionQueryBuilder.TAG,
            query,
            0
        )

        if (payload.isEmpty()) {
            return emptyList()
        }

        val suggestions = payload.map {
            SearchSuggestEntry.newBuilder().apply {
                title = it.dig<String>(0)
            }.build()
        }

        return suggestions
    }

    override fun searchResults(query: String, nextPageUrl: String): StreamBundle {
        val cluster = search(query)

        return StreamBundle(
            id = UUID.randomUUID().hashCode(),
            streamTitle = query,
            streamClusters = mapOf(
                cluster.id to cluster
            )
        )
    }

    override fun nextStreamBundle(query: String, nextPageUrl: String): StreamBundle {
        // Web does not support pagination in the same way as native API, there is only one stream.
        return StreamBundle.EMPTY
    }

    override fun nextStreamCluster(query: String, nextPageUrl: String): StreamCluster {
        return search(query, nextPageUrl)
    }

    fun search(query: String, nextPageUrl: String = ""): StreamCluster {
        val response = execute(SearchQueryBuilder.build(query, nextPageUrl))

        var payload = response.dig<List<Any>>(
            SearchQueryBuilder.TAG,
            query,
            0
        )

        if (payload.isEmpty()) {
            return StreamCluster.EMPTY
        }

        // First stream is search stream, following are app streams (made-up names :p)
        if (payload.dig<String>(0, 1) != "Apps") {
            payload = payload.dig(1, 0)
        }

        // Find only the package names, complete app info is fetched via AppDetailsHelper
        val packageNames: List<String> = payload.dig<List<Any>>(0, 0).let { entry ->
            entry.mapNotNull {
                it.dig(12, 0)
            }
        }

        if (packageNames.isEmpty()) {
            return StreamCluster.EMPTY
        }

        val nextPageToken: String = payload.dig<String>(0, 7, 1)

        return StreamCluster(
            id = UUID.randomUUID().hashCode(),
            clusterTitle = query,
            clusterNextPageUrl = nextPageToken,
            clusterAppList = getAppDetails(packageNames)
        )
    }
}
