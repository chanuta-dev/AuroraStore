/*
 * SPDX-FileCopyrightText: 2020-2021 Aurora OSS
 * SPDX-FileCopyrightText: 2023-2025 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class Review(
    val title: String = String(),
    val comment: String = String(),
    val commentId: String = String(),
    val userName: String = String(),
    val userPhotoUrl: String = String(),
    val appVersion: String = String(),
    val rating: Int = 0,
    val timeStamp: Long = 0L
) : Parcelable {

    override fun hashCode(): Int {
        return commentId.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is Review -> other.commentId == commentId
            else -> false
        }
    }

    enum class Filter(val value: String) {
        ALL("ALL"),
        NEWEST("0"),
        POSITIVE("1"),
        CRITICAL("2"),
        FIVE("5"),
        FOUR("4"),
        THREE("3"),
        TWO("2"),
        ONE("1");
    }
}
