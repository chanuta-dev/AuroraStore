/*
 * SPDX-FileCopyrightText: 2020-2021 Aurora OSS
 * SPDX-FileCopyrightText: 2023 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.network

import com.aurora.gplayapi.data.models.PlayResponse
import kotlinx.coroutines.flow.StateFlow
import java.io.IOException

interface IHttpClient {
    val responseCode: StateFlow<Int>

    @Throws(IOException::class)
    fun post(url: String, headers: Map<String, String>, body: ByteArray): PlayResponse

    @Throws(IOException::class)
    fun post(url: String, headers: Map<String, String>, params: Map<String, String>): PlayResponse

    @Throws(IOException::class)
    fun get(url: String, headers: Map<String, String>): PlayResponse

    @Throws(IOException::class)
    fun get(url: String, headers: Map<String, String>, params: Map<String, String>): PlayResponse

    @Throws(IOException::class)
    fun get(url: String, headers: Map<String, String>, paramString: String): PlayResponse

    @Throws(IOException::class)
    fun getAuth(url: String): PlayResponse

    @Throws(IOException::class)
    fun postAuth(url: String, body: ByteArray): PlayResponse
}
