/*
 * SPDX-FileCopyrightText: 2020-2021 Aurora OSS
 * SPDX-FileCopyrightText: 2023-2025 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.data.models

import com.aurora.gplayapi.data.providers.DeviceInfoProvider
import com.aurora.gplayapi.data.serializers.LocaleSerializer
import kotlinx.serialization.Serializable
import java.util.Locale

@Serializable
data class AuthData(
    val email: String,
    val aasToken: String = String(),
    val authToken: String = String(),
    val isAnonymous: Boolean = false,
    val gsfId: String = String(),
    val tokenDispenserUrl: String = String(),
    val ac2dmToken: String = String(),
    val androidCheckInToken: String = String(),
    val deviceCheckInConsistencyToken: String = String(),
    val deviceConfigToken: String = String(),
    val experimentsConfigToken: String = String(),
    val gcmToken: String = String(),
    val oAuthLoginToken: String = String(),
    val dfeCookie: String = String(),
    @Serializable(with = LocaleSerializer::class)
    val locale: Locale = Locale.getDefault(),
    val deviceInfoProvider: DeviceInfoProvider? = null,
    val userProfile: UserProfile? = null
) {

    /**
     * Builder class for building [AuthData] manually
     */
    data class Builder(
        var email: String,
        var aasToken: String = String(),
        var authToken: String = String(),
        var isAnonymous: Boolean = false,
        var gsfId: String = String(),
        var tokenDispenserUrl: String = String(),
        var ac2dmToken: String = String(),
        var androidCheckInToken: String = String(),
        var deviceCheckInConsistencyToken: String = String(),
        var deviceConfigToken: String = String(),
        var experimentsConfigToken: String = String(),
        var gcmToken: String = String(),
        var oAuthLoginToken: String = String(),
        var dfeCookie: String = String(),
        var locale: Locale = Locale.getDefault(),
        var deviceInfoProvider: DeviceInfoProvider? = null,
        var userProfile: UserProfile? = null
    ) {
        fun build(): AuthData {
            return AuthData(
                email = email,
                aasToken = aasToken,
                authToken = authToken,
                isAnonymous = isAnonymous,
                gsfId = gsfId,
                tokenDispenserUrl = tokenDispenserUrl,
                ac2dmToken = ac2dmToken,
                androidCheckInToken = androidCheckInToken,
                deviceCheckInConsistencyToken = deviceCheckInConsistencyToken,
                deviceConfigToken = deviceConfigToken,
                experimentsConfigToken = experimentsConfigToken,
                gcmToken = gcmToken,
                oAuthLoginToken = oAuthLoginToken,
                dfeCookie = dfeCookie,
                locale = locale,
                deviceInfoProvider = deviceInfoProvider,
                userProfile = userProfile
            )
        }
    }
}
