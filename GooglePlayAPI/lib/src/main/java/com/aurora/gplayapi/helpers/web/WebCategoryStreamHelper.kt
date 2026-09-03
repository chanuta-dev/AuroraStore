/*
 * SPDX-FileCopyrightText: 2024 Aurora OSS
 * SPDX-FileCopyrightText: 2024 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.helpers.web

import com.aurora.gplayapi.data.models.StreamBundle
import com.aurora.gplayapi.data.models.StreamCluster
import com.aurora.gplayapi.helpers.contracts.CategoryStreamContract
import com.aurora.gplayapi.helpers.contracts.StreamContract
import com.aurora.gplayapi.network.IHttpClient
import com.aurora.gplayapi.utils.CategoryUtil
import java.util.Locale

class WebCategoryStreamHelper : BaseWebHelper(), CategoryStreamContract {
    private val webStreamHelper: WebStreamHelper by lazy {
        WebStreamHelper()
            .with(locale)
            .using(httpClient)
    }

    override fun with(locale: Locale?) = apply {
        this.locale = locale
    }

    override fun using(httpClient: IHttpClient) = apply {
        this.httpClient = httpClient
    }

    override fun fetch(url: String): StreamBundle {
        val category = CategoryUtil.getCategoryFromUrl(url)
        return webStreamHelper.fetch(StreamContract.Type.HOME, category)
    }

    override fun nextStreamCluster(id: Int, nextPageUrl: String): StreamCluster {
        return webStreamHelper.nextStreamCluster(id, nextPageUrl)
    }

    override fun nextStreamBundle(
        id: Int,
        category: StreamContract.Category,
        nextPageToken: String
    ): StreamBundle {
        return webStreamHelper.nextStreamBundle(id, category, nextPageToken)
    }
}
