/*
 * SPDX-FileCopyrightText: 2020-2024 Aurora OSS
 * SPDX-FileCopyrightText: 2023-2024 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class Category(
    val title: String = String(),
    val imageUrl: String = String(),
    val browseUrl: String = String(),
    val color: String = String(),
    val type: Type = Type.APPLICATION
) : Parcelable {

    enum class Type(val value: String) {
        APPLICATION("APPLICATION"),
        GAME("GAME"),
        FAMILY("FAMILY"),
    }

    enum class WebType(val value: Int) {
        APPLICATION(0),
        GAME(1),
        FAMILY(2),
    }

    override fun hashCode(): Int {
        return title.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is Category -> title == other.title
            else -> false
        }
    }
}
