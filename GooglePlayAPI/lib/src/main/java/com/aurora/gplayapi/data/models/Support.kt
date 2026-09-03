/*
 * SPDX-FileCopyrightText: 2025 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class Support(
    val developerName: String = String(),
    val developerEmail: String = String(),
    val developerAddress: String = String(),
    val developerPhoneNumber: String = String()
) : Parcelable
