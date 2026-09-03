/*
 * SPDX-FileCopyrightText: 2020 Aurora OSS
 * SPDX-FileCopyrightText: 2023-2025 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.data.models.details

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Parcelize
data class Chip(
    val id: String = UUID.randomUUID().toString(),
    val title: String = String(),
    val streamUrl: String? = String()
) : Parcelable {

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is Chip -> other.id == id
            else -> false
        }
    }
}
