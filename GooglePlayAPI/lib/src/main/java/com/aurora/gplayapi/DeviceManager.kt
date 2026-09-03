/*
 * SPDX-FileCopyrightText: 2020-2024 Aurora OSS
 * SPDX-FileCopyrightText: 2023 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi

import java.io.FileNotFoundException
import java.util.Properties

internal object DeviceManager {
    fun loadProperties(deviceName: String?): Properties? {
        return try {
            val properties = Properties()
            val inputStream = javaClass.classLoader?.getResourceAsStream("gplayapi_$deviceName")
            if (inputStream != null) {
                properties.load(inputStream)
            } else {
                throw FileNotFoundException("Device config file not found")
            }
            properties
        } catch (e: Exception) {
            null
        }
    }
}
