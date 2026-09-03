/*
 * SPDX-FileCopyrightText: 2020-2021 Aurora OSS
 * SPDX-FileCopyrightText: 2023-2025 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Parcelize
data class PlayFile(
    val id: String = UUID.randomUUID().toString(),
    val name: String = String(),
    val url: String = String(),
    val size: Long = 0L,
    val type: Type = Type.BASE,
    val sha1: String = String(),
    val sha256: String = String()
) : Parcelable {

    enum class Type {
        BASE, OBB, PATCH, SPLIT
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is PlayFile -> other.id == id
            else -> false
        }
    }
}
