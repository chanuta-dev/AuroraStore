/*
 * SPDX-FileCopyrightText: 2020-2024 Aurora OSS
 * SPDX-FileCopyrightText: 2023 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.helpers

import com.aurora.gplayapi.GooglePlayApi
import com.aurora.gplayapi.UserProfileResponse
import com.aurora.gplayapi.data.builders.UserProfileBuilder
import com.aurora.gplayapi.data.models.AuthData
import com.aurora.gplayapi.data.models.UserProfile
import com.aurora.gplayapi.data.providers.HeaderProvider.getDefaultHeaders
import com.aurora.gplayapi.network.IHttpClient

class UserProfileHelper(authData: AuthData) : NativeHelper(authData) {

    override fun using(httpClient: IHttpClient) = apply {
        this.httpClient = httpClient
    }

    @Throws(Exception::class)
    fun getUserProfileResponse(): UserProfileResponse {
        val headers: MutableMap<String, String> = getDefaultHeaders(authData)
        val playResponse = httpClient.get(GooglePlayApi.URL_USER_PROFILE, headers, mapOf())

        val payload = getUserProfileResponse(playResponse.responseBytes)
        return payload.userProfileResponse
    }

    @Throws(Exception::class)
    fun getUserProfile(): UserProfile? {
        return try {
            UserProfileBuilder.build(getUserProfileResponse().userProfile)
        } catch (_: Exception) {
            null
        }
    }
}
