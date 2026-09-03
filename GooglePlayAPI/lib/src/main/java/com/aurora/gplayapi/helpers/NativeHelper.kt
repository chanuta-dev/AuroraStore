/*
 * SPDX-FileCopyrightText: 2020-2024 Aurora OSS
 * SPDX-FileCopyrightText: 2023-2025 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.helpers

import com.aurora.gplayapi.BrowseResponse
import com.aurora.gplayapi.DetailsResponse
import com.aurora.gplayapi.GooglePlayApi
import com.aurora.gplayapi.Item
import com.aurora.gplayapi.ListResponse
import com.aurora.gplayapi.Payload
import com.aurora.gplayapi.PayloadApi
import com.aurora.gplayapi.ResponseWrapper
import com.aurora.gplayapi.ResponseWrapperApi
import com.aurora.gplayapi.SearchResponse
import com.aurora.gplayapi.SearchSuggestResponse
import com.aurora.gplayapi.data.builders.AppBuilder
import com.aurora.gplayapi.data.models.App
import com.aurora.gplayapi.data.models.Artwork
import com.aurora.gplayapi.data.models.AuthData
import com.aurora.gplayapi.data.models.PlayResponse
import com.aurora.gplayapi.data.models.StreamBundle
import com.aurora.gplayapi.data.models.StreamCluster
import com.aurora.gplayapi.data.models.editor.EditorChoiceBundle
import com.aurora.gplayapi.data.models.editor.EditorChoiceCluster
import com.aurora.gplayapi.data.providers.HeaderProvider.getDefaultHeaders
import com.aurora.gplayapi.utils.bytesOrThrow
import java.io.IOException

abstract class NativeHelper(protected var authData: AuthData) : BaseHelper() {
    @Throws(IOException::class)
    fun getResponse(
        url: String,
        params: Map<String, String>,
        headers: Map<String, String>
    ): PlayResponse {
        return httpClient.get(url, headers, params)
    }

    /*-------------------------------------------- COMMONS -------------------------------------------------*/
    fun getNextPageUrl(item: Item): String {
        return item.containerMetadata?.nextPageUrl.orEmpty()
    }

    fun getBrowseUrl(item: Item): String {
        return item.containerMetadata?.browseUrl.orEmpty()
    }

    @Throws(Exception::class)
    fun getPayLoadFromBytes(bytes: ByteArray?): Payload {
        val responseWrapper = ResponseWrapper.parseFrom(bytes)
        return responseWrapper.payload ?: Payload.getDefaultInstance()
    }

    @Throws(Exception::class)
    fun getUserProfileResponse(bytes: ByteArray?): PayloadApi {
        val responseWrapper = ResponseWrapperApi.parseFrom(bytes)
        return responseWrapper.payload ?: PayloadApi.getDefaultInstance()
    }

    @Throws(Exception::class)
    inline fun <reified T> getResponseFromBytes(bytes: ByteArray?): T {
        val payload = getPayLoadFromBytes(bytes)

        return when (T::class) {
            BrowseResponse::class -> payload.browseResponse as T
            DetailsResponse::class -> payload.detailsResponse as T
            ListResponse::class -> payload.listResponse as T
            SearchResponse::class -> payload.searchResponse as T
            SearchSuggestResponse::class -> payload.searchSuggestResponse as T
            else -> null as T
        }
    }

    @Throws(Exception::class)
    fun getPrefetchPayLoad(bytes: ByteArray?): Payload {
        val responseWrapper = ResponseWrapper.parseFrom(bytes)

        return when {
            responseWrapper.hasPreFetch() -> {
                responseWrapper.preFetch.response.payload
            }

            responseWrapper.hasPayload() -> {
                responseWrapper.payload
            }

            else -> {
                Payload.getDefaultInstance()
            }
        }
    }

    open fun getAppsFromItem(item: Item): List<App> {
        return item.subItemList
            .filter { it.type == 1 }
            .mapNotNull(AppBuilder::build)
    }

    /*--------------------------------------- GENERIC APP STREAMS --------------------------------------------*/
    @Throws(Exception::class)
    fun getNextStreamResponse(nextPageUrl: String): ListResponse {
        val headers: Map<String, String> = getDefaultHeaders(authData)
        val playResponse = httpClient.get(GooglePlayApi.URL_FDFE + "/" + nextPageUrl, headers)

        return getResponseFromBytes(playResponse.bytesOrThrow())
    }

    @Throws(Exception::class)
    fun getBrowseStreamResponse(browseUrl: String): BrowseResponse {
        val headers: Map<String, String> = getDefaultHeaders(authData)
        val playResponse = httpClient.get(GooglePlayApi.URL_FDFE + "/" + browseUrl, headers)

        return getResponseFromBytes(playResponse.bytesOrThrow())
    }

    @Throws(Exception::class)
    fun getNextStreamCluster(id: Int, nextPageUrl: String): StreamCluster {
        val listResponse = getNextStreamResponse(nextPageUrl)
        return getStreamCluster(id, listResponse)
    }

    fun getStreamCluster(id: Int, item: Item): StreamCluster {
        with(item) {
            return StreamCluster(
                id = id,
                clusterTitle = title.orEmpty(),
                clusterSubtitle = subtitle.orEmpty(),
                clusterBrowseUrl = getBrowseUrl(item),
                clusterNextPageUrl = getNextPageUrl(item),
                clusterAppList = getAppsFromItem(item)
            )
        }
    }

    fun getStreamCluster(id: Int, listResponse: ListResponse): StreamCluster {
        with(listResponse) {
            if (hasItem() and item.subItemList.isNotEmpty()) {
                return getStreamCluster(id, item.subItemList.first())
            }
        }

        return StreamCluster.EMPTY
    }

    fun getStreamClusters(id: Int, listResponse: ListResponse): Map<Int, StreamCluster> {
        val clusters = mutableMapOf<Int, StreamCluster>()

        with(listResponse) {
            if (hasItem() and item.subItemList.isNotEmpty()) {
                for (subItem in item.subItemList) {
                    val cluster = getStreamCluster(id, subItem)
                    clusters[cluster.id] = cluster
                }
            }
        }

        return clusters
    }

    fun getStreamBundle(id: Int, listResponse: ListResponse): StreamBundle {
        with(listResponse) {
            if (hasItem() && item.subItemList.isNotEmpty()) {
                return StreamBundle(
                    id = id,
                    streamTitle = item.title.orEmpty(),
                    streamNextPageUrl = getNextPageUrl(item),
                    streamClusters = getStreamClusters(id, this)
                )
            }
        }

        return StreamBundle.EMPTY
    }

    /*------------------------------------- EDITOR'S CHOICE CLUSTER & BUNDLES ------------------------------------*/
    @Throws(Exception::class)
    private fun getEditorChoiceCluster(item: Item): EditorChoiceCluster {
        val title = if (item.hasTitle()) item.title else String()
        val artworkList: MutableList<Artwork> = ArrayList()
        val browseUrl = getBrowseUrl(item)
        if (item.imageCount > 0) {
            item.imageList.forEach {
                artworkList.add(
                    Artwork(
                        type = it.imageType,
                        url = it.imageUrl,
                        aspectRatio = it.dimension.aspectRatio,
                        width = it.dimension.width,
                        height = it.dimension.height
                    )
                )
            }
        }
        return EditorChoiceCluster(
            id = browseUrl.hashCode(),
            clusterTitle = title,
            clusterBrowseUrl = browseUrl,
            clusterArtwork = artworkList
        )
    }

    private fun getEditorChoiceBundles(item: Item): EditorChoiceBundle {
        val title = if (item.hasTitle()) item.title else String()
        val choiceClusters: MutableList<EditorChoiceCluster> = ArrayList()
        for (subItem in item.subItemList) {
            choiceClusters.add(getEditorChoiceCluster(subItem))
        }

        return EditorChoiceBundle(
            id = title.hashCode(),
            bundleTitle = title,
            bundleChoiceClusters = choiceClusters
        )
    }

    fun getEditorChoiceBundles(listResponse: ListResponse?): List<EditorChoiceBundle> {
        val editorChoiceBundles: MutableList<EditorChoiceBundle> = ArrayList()

        if (listResponse == null || !listResponse.hasItem()) {
            return editorChoiceBundles
        }

        with(listResponse.item) {
            subItemList.forEach { subItem ->
                subItem.let {
                    val bundle = getEditorChoiceBundles(it)
                    if (bundle.bundleChoiceClusters.isNotEmpty()) {
                        editorChoiceBundles.add(bundle)
                    }
                }
            }
        }

        return editorChoiceBundles
    }
}
