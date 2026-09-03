/*
 * SPDX-FileCopyrightText: 2024 Aurora OSS
 * SPDX-FileCopyrightText: 2025 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class DeveloperInfo(
    val devId: String,
    val name: String = String(),
    val email: String = String(),
    val website: String = String(),
    val address: String = String()
) : Parcelable {
    override fun hashCode(): Int {
        return devId.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is DeveloperInfo -> {
                devId == other.devId
            }

            else -> false
        }
    }
}

