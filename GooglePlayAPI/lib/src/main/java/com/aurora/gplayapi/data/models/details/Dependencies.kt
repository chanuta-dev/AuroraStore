/*
 * SPDX-FileCopyrightText: 2020 Aurora OSS
 * SPDX-FileCopyrightText: 2023-2025 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.data.models.details

import android.os.Parcelable
import com.aurora.gplayapi.data.models.App
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class Dependencies(
    val dependentPackages: List<String> = listOf(),
    val dependentSplits: List<String> = listOf(),
    val dependentLibraries: List<App> = listOf(),
    val targetSDK: Int = -1,
    val totalSize: Long = -1L
) : Parcelable
