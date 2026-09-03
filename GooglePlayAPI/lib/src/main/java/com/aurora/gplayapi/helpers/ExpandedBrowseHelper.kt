/*
 * SPDX-FileCopyrightText: 2020-2024 Aurora OSS
 * SPDX-FileCopyrightText: 2023-2024 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.helpers

import com.aurora.gplayapi.Item
import com.aurora.gplayapi.data.builders.AppBuilder
import com.aurora.gplayapi.data.models.App
import com.aurora.gplayapi.data.models.AuthData
import com.aurora.gplayapi.data.models.StreamCluster
import com.aurora.gplayapi.network.IHttpClient

class ExpandedBrowseHelper(authData: AuthData) : NativeHelper(authData) {

    override fun using(httpClient: IHttpClient) = apply {
        this.httpClient = httpClient
    }

    fun getExpandedBrowseClusters(id: Int, expandedBrowseUrl: String): StreamCluster {
        val listResponse = getNextStreamResponse(expandedBrowseUrl)
        return getStreamCluster(id, listResponse.item)
    }

    override fun getAppsFromItem(item: Item): MutableList<App> {
        return item.subItemList
            .filterNotNull()
            .flatMap { it.subItemList.mapNotNull(AppBuilder::build) }
            .toMutableList()
    }
}
