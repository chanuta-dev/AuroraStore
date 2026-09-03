/*
 * SPDX-FileCopyrightText: 2020-2024 Aurora OSS
 * SPDX-FileCopyrightText: 2023 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi

import com.aurora.gplayapi.data.models.AndroidCheckInResponse
import com.aurora.gplayapi.data.models.AuthData
import com.aurora.gplayapi.data.providers.HeaderProvider.getAuthHeaders
import com.aurora.gplayapi.data.providers.HeaderProvider.getDefaultHeaders
import com.aurora.gplayapi.data.providers.ParamProvider.getAASTokenParams
import com.aurora.gplayapi.data.providers.ParamProvider.getAuthParams
import com.aurora.gplayapi.data.providers.ParamProvider.getDefaultAuthParams
import com.aurora.gplayapi.exceptions.GooglePlayException.AuthException
import com.aurora.gplayapi.network.DefaultHttpClient
import com.aurora.gplayapi.network.IHttpClient
import com.aurora.gplayapi.utils.Util
import com.aurora.gplayapi.utils.bytesOrThrow
import java.io.IOException
import java.math.BigInteger

class GooglePlayApi {

    private var httpClient: IHttpClient = DefaultHttpClient

    fun using(httpClient: IHttpClient) = apply {
        this.httpClient = httpClient
    }

    @Throws(Exception::class)
    fun toc(authData: AuthData): String {
        val playResponse = httpClient.get(URL_TOC, getDefaultHeaders(authData))
        val tocResponse =
            ResponseWrapper.parseFrom(playResponse.bytesOrThrow(true)).payload.tocResponse
        if (tocResponse.tosContent.isNotBlank() && tocResponse.tosToken.isNotBlank()) {
            acceptTos(tocResponse.tosToken, authData)
        }
        return tocResponse.cookie
    }

    @Throws(Exception::class)
    private fun acceptTos(tosToken: String, authData: AuthData): AcceptTosResponse {
        val headers: MutableMap<String, String> = getDefaultHeaders(authData)
        val params: MutableMap<String, String> = HashMap()
        params["tost"] = tosToken
        params["toscme"] = "false"

        val playResponse = httpClient.post(URL_TOS_ACCEPT, headers, params)
        return ResponseWrapper.parseFrom(playResponse.bytesOrThrow(true))
            .payload
            .acceptTosResponse
    }

    @Throws(IOException::class)
    fun uploadDeviceConfig(authData: AuthData): String {
        val request = UploadDeviceConfigRequest.newBuilder()
            .setDeviceConfiguration(authData.deviceInfoProvider!!.deviceConfigurationProto)
            .build()

        val headers: MutableMap<String, String> = getDefaultHeaders(authData)
        val playResponse = httpClient.post(URL_UPLOAD_DEVICE_CONFIG, headers, request.toByteArray())

        return ResponseWrapper.parseFrom(playResponse.bytesOrThrow(true))
            .payload
            .uploadDeviceConfigResponse
            .uploadDeviceConfigToken
    }

    @Throws(Exception::class)
    fun generateGsfId(authData: AuthData): AndroidCheckInResponse {
        val request = authData.deviceInfoProvider!!.generateAndroidCheckInRequest()
        val checkInResponse = checkIn(authData, request!!.toByteArray())
        val gsfId = BigInteger.valueOf(checkInResponse.androidId).toString(16)
        return AndroidCheckInResponse(
            gsfId = gsfId,
            deviceCheckInConsistencyToken = checkInResponse.deviceCheckinConsistencyToken
        )
    }

    @Throws(Exception::class)
    private fun checkIn(authData: AuthData, request: ByteArray): AndroidCheckinResponse {
        val headers: MutableMap<String, String> = getAuthHeaders(authData)
        headers["Content-Type"] = "application/x-protobuffer"
        headers["Host"] = "android.clients.google.com"

        val responseBody = httpClient.post(URL_CHECK_IN, headers, request)
        return AndroidCheckinResponse.parseFrom(responseBody.bytesOrThrow(true))
    }

    @Throws(Exception::class)
    fun generateAASToken(authData: AuthData): String? {
        val params: MutableMap<String, String> = HashMap()
        params.putAll(getDefaultAuthParams(authData))
        params.putAll(getAASTokenParams(authData.oAuthLoginToken))

        val headers: MutableMap<String, String> = getAuthHeaders(authData)
        headers["app"] = "com.android.vending"

        val playResponse = httpClient.post(URL_AUTH, headers, params)
        val hashMap = Util.parseResponse(playResponse.bytesOrThrow(true))

        return hashMap["Auth"] ?: throw AuthException(401, "Could not generate AAS Token")
    }

    @Throws(Exception::class)
    fun generateToken(authData: AuthData, service: Service): String {
        val headers: MutableMap<String, String> = getAuthHeaders(authData)
        val params: MutableMap<String, String> = HashMap()
        params.putAll(getDefaultAuthParams(authData))
        params.putAll(getAuthParams(authData.aasToken))

        when (service) {
            Service.AC2DM -> {
                params["service"] = "ac2dm"
                params.remove("app")
            }

            Service.ANDROID_CHECK_IN_SERVER -> {
                params["oauth2_foreground"] = "0"
                params["app"] = "com.google.android.gms"
                params["service"] = "AndroidCheckInServer"
            }

            Service.EXPERIMENTAL_CONFIG -> {
                params["service"] = "oauth2:https://www.googleapis.com/auth/experimentsandconfigs"
            }

            Service.NUMBERER -> {
                params["app"] = "com.google.android.gms"
                params["service"] = "oauth2:https://www.googleapis.com/auth/numberer"
            }

            Service.GCM -> {
                params["app"] = "com.google.android.gms"
                params["service"] = "oauth2:https://www.googleapis.com/auth/gcm"
            }

            Service.GOOGLE_PLAY -> {
                headers["app"] = "com.google.android.gms"
                params["service"] = "oauth2:https://www.googleapis.com/auth/googleplay"
            }

            Service.OAUTHLOGIN -> {
                params["oauth2_foreground"] = "0"
                params["app"] = "com.google.android.googlequicksearchbox"
                params["service"] = "oauth2:https://www.google.com/accounts/OAuthLogin"
                params["callerPkg"] = "com.google.android.googlequicksearchbox"
            }

            Service.ANDROID -> {
                params["service"] = "android"
            }
        }

        val playResponse = httpClient.post(URL_AUTH, headers, params)
        val hashMap = Util.parseResponse(playResponse.bytesOrThrow(true))

        return hashMap["Auth"] ?: throw AuthException(401, "Could not generate OAuth Token")
    }

    enum class Service {
        AC2DM,
        ANDROID,
        ANDROID_CHECK_IN_SERVER,
        EXPERIMENTAL_CONFIG,
        GCM,
        GOOGLE_PLAY,
        NUMBERER,
        OAUTHLOGIN
    }

    internal companion object {
        const val URL_BASE = "https://android.clients.google.com"
        const val URL_FDFE = "$URL_BASE/fdfe"
        const val ACQUIRE_URL = "$URL_FDFE/acquire"
        const val CATEGORIES_URL = "$URL_FDFE/categoriesList"
        const val CATEGORIES_URL_2 = "$URL_FDFE/allCategoriesList"
        const val DELIVERY_URL = "$URL_FDFE/delivery"
        const val PURCHASE_URL = "$URL_FDFE/purchase"
        const val PURCHASE_HISTORY_URL = "$URL_FDFE/purchaseHistory"
        const val TOP_CHART_URL = "$URL_FDFE/listTopChartItems"
        const val URL_AUTH = "$URL_BASE/auth"
        const val URL_BULK_DETAILS = "$URL_FDFE/bulkDetails"
        const val URL_BULK_PREFETCH = "$URL_FDFE/bulkPrefetch"
        const val URL_CHECK_IN = "$URL_BASE/checkin"
        const val URL_DETAILS = "$URL_FDFE/details"
        const val URL_DETAILS_DEVELOPER = "$URL_FDFE/browseDeveloperPage"
        const val URL_MY_APPS = "$URL_FDFE/myApps"
        const val URL_REVIEW_ADD_EDIT = "$URL_FDFE/addReview"
        const val URL_REVIEW_DELETE = "$URL_FDFE/deleteReview"
        const val URL_REVIEW_USER = "$URL_FDFE/userReview"
        const val URL_REVIEWS = "$URL_FDFE/rev"
        const val URL_SEARCH = "$URL_FDFE/search"
        const val URL_SEARCH_SUGGEST = "$URL_FDFE/searchSuggest"
        const val URL_TESTING_PROGRAM = "$URL_FDFE/apps/testingProgram"
        const val URL_TOC = "$URL_FDFE/toc"
        const val URL_TOS_ACCEPT = "$URL_FDFE/acceptTos"
        const val URL_UPLOAD_DEVICE_CONFIG = "$URL_FDFE/uploadDeviceConfig"
        const val URL_SYNC = "$URL_FDFE/apps/contentSync"
        const val URL_SELF_UPDATE = "$URL_FDFE/selfUpdate"
        const val URL_USER_PROFILE = "$URL_FDFE/api/userProfile"
        const val URL_LIBRARY = "$URL_FDFE/library"
        const val URL_MODIFY_LIBRARY = "$URL_FDFE/modifyLibrary"

        const val LEGACY_USER_AGENT =
            "Android-Finsky/29.2.15-21 [0] [PR] 426536134 (api=3,versionCode=82921510,sdk=25)"
    }
}
