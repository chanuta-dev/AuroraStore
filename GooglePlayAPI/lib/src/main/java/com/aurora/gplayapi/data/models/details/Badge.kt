/*
 * SPDX-FileCopyrightText: 2020 Aurora OSS
 * SPDX-FileCopyrightText: 2023-2025 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.data.models.details

import android.os.Parcelable
import com.aurora.gplayapi.data.models.Artwork
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Parcelize
data class Badge(
    val id: String = UUID.randomUUID().toString(),
    val textMajor: String = String(),
    val textMinor: String = String(),
    val textMinorHtml: String? = String(),
    val textDescription: String? = String(),
    val artwork: Artwork? = null,
    val link: String = String()
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is Badge -> other.id == id
            else -> false
        }
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
