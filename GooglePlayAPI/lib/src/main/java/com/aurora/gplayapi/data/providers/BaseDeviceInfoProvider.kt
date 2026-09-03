/*
 * SPDX-FileCopyrightText: 2020 Aurora OSS
 * SPDX-FileCopyrightText: 2023 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.data.providers

import com.aurora.gplayapi.AndroidCheckinRequest
import com.aurora.gplayapi.DeviceConfigurationProto

abstract class BaseDeviceInfoProvider {
    abstract fun generateAndroidCheckInRequest(): AndroidCheckinRequest?
    abstract val deviceConfigurationProto: DeviceConfigurationProto?
    abstract val userAgentString: String
    abstract val authUserAgentString: String
    abstract val mccMnc: String
    abstract val sdkVersion: Int
    abstract val playServicesVersion: Int
}
