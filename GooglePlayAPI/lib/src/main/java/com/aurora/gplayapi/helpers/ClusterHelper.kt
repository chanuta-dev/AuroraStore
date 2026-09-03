/*
 * SPDX-FileCopyrightText: 2020-2024 Aurora OSS
 * SPDX-FileCopyrightText: 2023 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.helpers

import com.aurora.gplayapi.GooglePlayApi
import com.aurora.gplayapi.ListResponse
import com.aurora.gplayapi.data.models.AuthData
import com.aurora.gplayapi.data.models.StreamCluster
import com.aurora.gplayapi.data.providers.HeaderProvider.getDefaultHeaders
import com.aurora.gplayapi.network.IHttpClient
import com.aurora.gplayapi.utils.bytesOrThrow

class ClusterHelper(authData: AuthData) : NativeHelper(authData) {

    override fun using(httpClient: IHttpClient) = apply {
        this.httpClient = httpClient
    }

    @Throws(Exception::class)
    fun next(id: Int, nextPageUrl: String): StreamCluster {
        val listResponse = getNextStreamResponse(nextPageUrl)
        return getStreamCluster(id, listResponse)
    }

    @Throws(Exception::class)
    fun getCluster(id: Int, type: Type): StreamCluster {
        val headers: MutableMap<String, String> = getDefaultHeaders(authData)
        val params: MutableMap<String, String> = HashMap()
        params["n"] = "15"
        params["tab"] = type.value

        val playResponse = httpClient.get(GooglePlayApi.URL_FDFE + "/myAppsStream", headers, params)

        val listResponse = getResponseFromBytes<ListResponse>(playResponse.bytesOrThrow())
        val streamCluster = getStreamCluster(id, listResponse)

        return streamCluster
    }

    enum class Type(var value: String) {
        MY_APPS_INSTALLED("INSTALLED"),
        MY_APPS_LIBRARY("LIBRARY"),
        MY_APPS_UPDATES("UPDATES");
    }
}
