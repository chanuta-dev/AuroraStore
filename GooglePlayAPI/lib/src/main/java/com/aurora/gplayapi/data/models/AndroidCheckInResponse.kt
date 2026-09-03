/*
 * SPDX-FileCopyrightText: 2025 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.data.models

@ConsistentCopyVisibility
data class AndroidCheckInResponse internal constructor(
    val gsfId: String,
    val deviceCheckInConsistencyToken: String
)
