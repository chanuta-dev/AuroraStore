/*
 * SPDX-FileCopyrightText: 2020 Aurora OSS
 * SPDX-FileCopyrightText: 2023-2025 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class Rating(
    val average: Float = 0f,
    val oneStar: Long = 0L,
    val twoStar: Long = 0L,
    val threeStar: Long = 0L,
    val fourStar: Long = 0L,
    val fiveStar: Long = 0L,
    val thumbsUp: Long = 0L,
    val thumbsDown: Long = 0L,
    val label: String = String(),
    val abbreviatedLabel: String = String()
) : Parcelable
