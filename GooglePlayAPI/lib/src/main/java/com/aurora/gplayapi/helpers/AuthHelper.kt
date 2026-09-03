/*
 * SPDX-FileCopyrightText: 2020-2024 Aurora OSS
 * SPDX-FileCopyrightText: 2023-2024 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.helpers

import com.aurora.gplayapi.DeviceManager
import com.aurora.gplayapi.GooglePlayApi
import com.aurora.gplayapi.data.models.AuthData
import com.aurora.gplayapi.data.providers.DeviceInfoProvider
import com.aurora.gplayapi.network.DefaultHttpClient
import com.aurora.gplayapi.network.IHttpClient
import java.util.Locale
import java.util.Properties

object AuthHelper {

    enum class Token {
        AAS, AUTH
    }

    private var httpClient: IHttpClient = DefaultHttpClient

    fun using(httpClient: IHttpClient) = apply {
        this.httpClient = httpClient
    }

    /**
     * Builds and returns authentication data to work with the helper methods
     * @param email E-mail address for the account
     * @param token Token for account authentication
     * @param tokenType Type of the token, can be either [Token.AAS] or [Token.AUTH]
     * @param isAnonymous If the account credentials are anonymous (e.g. provided by Aurora Dispenser), defaults to false
     * @param properties Properties of the device, defaults to Pixel 7a properties
     * @param locale Locale for the account, default otherwise
     * @return An instance of [AuthData]
     */
    fun build(
        email: String,
        token: String,
        tokenType: Token,
        isAnonymous: Boolean = false,
        properties: Properties = DeviceManager.loadProperties("px_9a.properties")!!,
        locale: Locale = Locale.getDefault()
    ): AuthData {
        val authBuilder = AuthData.Builder(
            email = email,
            aasToken = if (tokenType == Token.AAS) token else String(),
            authToken = if (tokenType == Token.AUTH) token else String(),
            deviceInfoProvider = DeviceInfoProvider(properties, locale.toString()),
            locale = locale,
            isAnonymous = isAnonymous
        )

        val api = GooglePlayApi()
            .using(httpClient)

        // Android GSF ID
        val checkInResponse = api.generateGsfId(authBuilder.build())
        authBuilder.gsfId = checkInResponse.gsfId
        authBuilder.deviceCheckInConsistencyToken = checkInResponse.deviceCheckInConsistencyToken

        // Upload Device Config
        authBuilder.deviceConfigToken = api.uploadDeviceConfig(authBuilder.build())

        // Generate AuthToken if we are working with AAS
        if (tokenType == Token.AAS) {
            authBuilder.authToken = api.generateToken(
                authBuilder.build(), GooglePlayApi.Service.GOOGLE_PLAY
            )
        }

        // Do not accept GooglePlay TOS, this adds device to google account
        // authBuilder.dfeCookie = api.toc(authBuilder.build())

        // Fetch UserProfile
        authBuilder.userProfile = UserProfileHelper(authBuilder.build())
            .using(httpClient)
            .getUserProfile()

        return authBuilder.build()
    }

    /**
     * Validates given AuthData by fetching a test application
     * @param authData [AuthData] to validate
     */
    fun isValid(authData: AuthData, packageName: String = "com.google.android.apps.maps"): Boolean {
        return try {
            val app = AppDetailsHelper(authData)
                .using(httpClient)
                .getAppByPackageName(packageName)

            app.packageName == packageName && app.displayName.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
}
