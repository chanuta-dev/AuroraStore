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

@Serializable
@Parcelize
data class TestingProgram(
    val artwork: Artwork = Artwork(),
    val displayName: String = String(),
    val email: String = String(),
    val isAvailable: Boolean = false,
    val isSubscribed: Boolean = false,
    val isSubscribedAndInstalled: Boolean = false
) : Parcelable

@Parcelize
data class TestingProgramStatus(
    val subscribed: Boolean = false,
    val unsubscribed: Boolean = false
) : Parcelable
