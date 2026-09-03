/*
 * SPDX-FileCopyrightText: 2020-2024 Aurora OSS
 * SPDX-FileCopyrightText: 2023-2025 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class StreamBundle(
    val id: Int,
    val streamTitle: String = "",
    val streamNextPageUrl: String = "",
    val streamClusters: Map<Int, StreamCluster> = emptyMap()
) : Parcelable {
    companion object {
        val EMPTY = StreamBundle(-1)
    }

    fun hasNext(): Boolean {
        return streamNextPageUrl.isNotBlank()
    }

    fun hasCluster(): Boolean {
        return streamClusters.isNotEmpty()
    }
}
