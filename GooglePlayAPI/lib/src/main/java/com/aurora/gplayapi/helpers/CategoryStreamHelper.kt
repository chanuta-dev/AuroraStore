/*
 * SPDX-FileCopyrightText: 2020-2024 Aurora OSS
 * SPDX-FileCopyrightText: 2023-2025 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.helpers

import com.aurora.gplayapi.GooglePlayApi
import com.aurora.gplayapi.Payload
import com.aurora.gplayapi.ResponseWrapper
import com.aurora.gplayapi.data.models.AuthData
import com.aurora.gplayapi.data.models.StreamBundle
import com.aurora.gplayapi.data.models.StreamCluster
import com.aurora.gplayapi.data.providers.HeaderProvider
import com.aurora.gplayapi.helpers.contracts.CategoryStreamContract
import com.aurora.gplayapi.helpers.contracts.StreamContract
import com.aurora.gplayapi.network.IHttpClient
import com.aurora.gplayapi.utils.bytesOrThrow

class CategoryStreamHelper(authData: AuthData) : NativeHelper(authData), CategoryStreamContract {

    override fun using(httpClient: IHttpClient) = apply {
        this.httpClient = httpClient
    }

    @Throws(Exception::class)
    override fun fetch(url: String): StreamBundle {
        val headers = HeaderProvider.getDefaultHeaders(authData)
        val playResponse = httpClient.get(GooglePlayApi.URL_FDFE + "/" + url, headers)
        return getSubCategoryBundle(url.hashCode(), playResponse.bytesOrThrow())
    }

    override fun nextStreamCluster(id: Int, nextPageUrl: String): StreamCluster {
        return getNextStreamCluster(id, nextPageUrl)
    }

    override fun nextStreamBundle(
        id: Int,
        category: StreamContract.Category,
        nextPageToken: String
    ): StreamBundle {
        return fetch(nextPageToken)
    }

    @Throws(Exception::class)
    private fun getSubCategoryBundle(id: Int, bytes: ByteArray?): StreamBundle {
        val responseWrapper = ResponseWrapper.parseFrom(bytes)
        return when {
            responseWrapper.hasPreFetch() -> {
                val streamClusters = mutableMapOf<Int, StreamCluster>()

                with(responseWrapper.preFetch) {
                    if (hasResponse() && response.hasPayload()) {
                        val payload = response.payload
                        val currentStreamBundle = getSubCategoryBundle(id, payload)
                        streamClusters.putAll(currentStreamBundle.streamClusters)
                    }
                }

                StreamBundle(id, streamClusters = streamClusters)
            }

            responseWrapper.hasPayload() -> getSubCategoryBundle(id, responseWrapper.payload)
            else -> StreamBundle.EMPTY
        }
    }

    private fun getSubCategoryBundle(id: Int, payload: Payload): StreamBundle {
        with(payload) {
            if (hasListResponse()) {
                return getStreamBundle(id, payload.listResponse)
            }
        }

        return StreamBundle.EMPTY
    }
}
