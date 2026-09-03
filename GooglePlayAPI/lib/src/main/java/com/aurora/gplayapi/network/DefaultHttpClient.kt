/*
 * SPDX-FileCopyrightText: 2020-2024 Aurora OSS
 * SPDX-FileCopyrightText: 2023-2024 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.network

import android.util.Log
import com.aurora.gplayapi.data.models.PlayResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Headers.Companion.toHeaders
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

internal object DefaultHttpClient : IHttpClient {

    private const val TAG = "DefaultHttpClient"

    const val POST = "POST"
    const val GET = "GET"

    private val _responseCode = MutableStateFlow(100)
    override val responseCode: StateFlow<Int>
        get() = _responseCode.asStateFlow()

    val okHttpClient = OkHttpClient().newBuilder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    override fun get(url: String, headers: Map<String, String>): PlayResponse {
        return get(url, headers, mapOf())
    }

    override fun get(
        url: String,
        headers: Map<String, String>,
        params: Map<String, String>
    ): PlayResponse {
        val request = Request.Builder()
            .url(buildUrl(url, params))
            .headers(headers.toHeaders())
            .method(GET, null)
            .build()
        return processRequest(request)
    }

    override fun get(
        url: String,
        headers: Map<String, String>,
        paramString: String
    ): PlayResponse {
        val request = Request.Builder()
            .url(url + paramString)
            .headers(headers.toHeaders())
            .method(GET, null)
            .build()
        return processRequest(request)
    }

    override fun getAuth(url: String): PlayResponse {
        return PlayResponse(
            isSuccessful = false,
            code = 444
        )
    }

    override fun postAuth(url: String, body: ByteArray): PlayResponse {
        return PlayResponse(
            isSuccessful = false,
            code = 444
        )
    }

    override fun post(url: String, headers: Map<String, String>, body: ByteArray): PlayResponse {
        return post(url, headers, body.toRequestBody())
    }

    override fun post(
        url: String,
        headers: Map<String, String>,
        params: Map<String, String>
    ): PlayResponse {
        val request = Request.Builder()
            .url(buildUrl(url, params))
            .headers(headers.toHeaders())
            .method(POST, "".toRequestBody())
            .build()
        return processRequest(request)
    }

    private fun processRequest(request: Request): PlayResponse {
        // Reset response code as flow doesn't sends the same value twice
        _responseCode.value = 0

        val call = okHttpClient.newCall(request)
        return buildPlayResponse(call.execute())
    }

    private fun buildUrl(url: String, params: Map<String, String>): HttpUrl {
        val urlBuilder = url.toHttpUrl().newBuilder()
        params.forEach {
            urlBuilder.addQueryParameter(it.key, it.value)
        }
        return urlBuilder.build()
    }

    @Throws(IOException::class)
    fun post(url: String, headers: Map<String, String>, requestBody: RequestBody): PlayResponse {
        val request = Request.Builder()
            .url(url)
            .headers(headers.toHeaders())
            .method(POST, requestBody)
            .build()
        return processRequest(request)
    }

    @JvmStatic
    private fun buildPlayResponse(response: Response): PlayResponse {
        return PlayResponse(
            isSuccessful = response.isSuccessful,
            code = response.code,
            responseBytes = response.body.bytes(),
            errorString = if (!response.isSuccessful) response.message else String(),
            type = response.header("Content-Type", "application/octet-stream")
        ).also {
            _responseCode.value = response.code
            Log.d(TAG, "OKHTTP [${response.code}] ${response.request.url}")
        }
    }
}
