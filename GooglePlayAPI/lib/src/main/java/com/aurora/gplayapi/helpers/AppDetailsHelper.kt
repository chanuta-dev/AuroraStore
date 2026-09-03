/*
 * SPDX-FileCopyrightText: 2020-2024 Aurora OSS
 * SPDX-FileCopyrightText: 2023-2025 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.helpers

import com.aurora.gplayapi.BulkDetailsRequest
import com.aurora.gplayapi.DetailsResponse
import com.aurora.gplayapi.GooglePlayApi
import com.aurora.gplayapi.ListResponse
import com.aurora.gplayapi.Payload
import com.aurora.gplayapi.TestingProgramRequest
import com.aurora.gplayapi.data.builders.AppBuilder
import com.aurora.gplayapi.data.models.App
import com.aurora.gplayapi.data.models.AuthData
import com.aurora.gplayapi.data.models.StreamBundle
import com.aurora.gplayapi.data.models.details.DevStream
import com.aurora.gplayapi.data.models.details.TestingProgramStatus
import com.aurora.gplayapi.data.providers.HeaderProvider.getDefaultHeaders
import com.aurora.gplayapi.helpers.contracts.AppDetailsContract
import com.aurora.gplayapi.network.IHttpClient
import com.aurora.gplayapi.utils.bytesOrThrow
import java.io.IOException

class AppDetailsHelper(authData: AuthData) : NativeHelper(authData), AppDetailsContract {

    override fun using(httpClient: IHttpClient) = apply {
        this.httpClient = httpClient
    }

    private fun getDevStream(id: Int, payload: Payload): DevStream {
        val listResponse: ListResponse = payload.listResponse

        with(listResponse) {
            if (hasItem()) {
                with(item) {
                    if (hasAnnotations() && annotations.hasOverlayMetaData()) {
                        with(annotations.overlayMetaData) {
                            return DevStream(
                                id = id,
                                title = overlayTitle?.title.orEmpty(),
                                imgUrl = overlayTitle?.compositeImage?.url.orEmpty(),
                                description = overlayDescription?.description.orEmpty(),
                                streamBundle = getStreamBundle(id, payload.listResponse)
                            )
                        }
                    }
                }
            }
        }

        return DevStream.EMPTY
    }

    @Throws(Exception::class)
    private fun getDetailsResponseByPackageName(packageName: String): DetailsResponse {
        val headers: Map<String, String> = getDefaultHeaders(authData)
        val params: MutableMap<String, String> = HashMap()
        params["doc"] = packageName

        val playResponse = httpClient.get(GooglePlayApi.URL_DETAILS, headers, params)
        return getResponseFromBytes(playResponse.bytesOrThrow())
    }

    @Throws(Exception::class)
    override fun getAppByPackageName(packageName: String): App {
        return try {
            val detailsResponse = getDetailsResponseByPackageName(packageName)
            AppBuilder.build(detailsResponse) ?: throw Exception("Failed to parse")
        } catch (e: Exception) {
            if (com.aurora.gplayapi.PatchedAppManager.isPatchedApp(packageName)) {
                kotlinx.coroutines.runBlocking {
                    com.aurora.gplayapi.PatchedAppManager.getAppMetadata(packageName)
                } ?: App(packageName)
            } else {
                throw e
            }
        }
    }
    @Throws(Exception::class)
    override fun getAppByPackageName(packageNames: List<String>): List<App> {
        if (packageNames.isEmpty()) {
            return emptyList()
        }

        val headers: MutableMap<String, String> = getDefaultHeaders(authData)
        val request = BulkDetailsRequest.newBuilder()
            .addAllDocId(packageNames)
            .build()
            .toByteArray()

        if (!headers.containsKey("Content-Type")) {
            headers["Content-Type"] = "application/x-protobuf"
        }

        val playResponse = httpClient.post(GooglePlayApi.URL_BULK_DETAILS, headers, request)
        val payload = getPayLoadFromBytes(playResponse.bytesOrThrow())

        if (payload.hasBulkDetailsResponse()) {
            return payload.bulkDetailsResponse.entryList.mapNotNull { AppBuilder.build(it.item) }
        }

        return emptyList()
    }

    // TODO: Move to contract
    @Throws(Exception::class)
    fun getDetailsStream(id: Int, streamUrl: String): StreamBundle {
        val headers: Map<String, String> = getDefaultHeaders(authData)
        val params: MutableMap<String, String> = HashMap()

        val playResponse = httpClient.get(
            "${GooglePlayApi.URL_FDFE}/$streamUrl",
            headers,
            params
        )

        val payload = getPayLoadFromBytes(playResponse.bytesOrThrow())
        val streamBundle = getStreamBundle(id, payload.listResponse)

        return streamBundle
    }

    fun getDeveloperStream(devId: String): DevStream {
        val headers: Map<String, String> = getDefaultHeaders(authData)
        val params: MutableMap<String, String> = HashMap()

        val playResponse = httpClient.get(
            "${GooglePlayApi.URL_FDFE}/getDeveloperPageStream?docid=developer-$devId",
            headers,
            params
        )

        val payload = getPayLoadFromBytes(playResponse.bytesOrThrow())
        val devStream = getDevStream(devId.hashCode(), payload)

        return devStream
    }

    @Throws(IOException::class)
    fun testingProgram(packageName: String?, subscribe: Boolean = true): TestingProgramStatus {
        val request = TestingProgramRequest.newBuilder()
            .setPackageName(packageName)
            .setSubscribe(subscribe)
            .build()

        val playResponse = httpClient.post(
            GooglePlayApi.URL_TESTING_PROGRAM,
            getDefaultHeaders(authData),
            request.toByteArray()
        )

        val payload = getPayLoadFromBytes(playResponse.bytesOrThrow())

        return if (payload.hasTestingProgramResponse() &&
            payload.testingProgramResponse.hasResult() &&
            payload.testingProgramResponse.result.hasDetails()
        ) {
            val details = payload.testingProgramResponse.result.details
            TestingProgramStatus(
                subscribed = details.hasSubscribed() && details.subscribed,
                unsubscribed = details.hasUnsubscribed() && details.unsubscribed
            )
        } else {
            TestingProgramStatus()
        }
    }
}
