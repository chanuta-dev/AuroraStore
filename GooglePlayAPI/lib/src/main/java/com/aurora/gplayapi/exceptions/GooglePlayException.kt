/*
 * SPDX-FileCopyrightText: 2020-2024 Aurora OSS
 * SPDX-FileCopyrightText: 2023 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.exceptions

sealed class GooglePlayException(message: String) : Exception(message) {
    data class AuthException(val code: Int, val reason: String = "Authentication Error") :
        GooglePlayException(reason)

    data class AppNotPurchased(
        val code: Int,
        val reason: String = "App not purchased / unavailable in your country"
    ) :
        GooglePlayException(reason)

    data class NotFound(val code: Int = 0, val reason: String = "Item not found") :
        GooglePlayException(reason)

    data class AppRemoved(val code: Int, val reason: String = "App removed from Play Store") :
        GooglePlayException(reason)

    data class AppNotSupported(val code: Int, val reason: String = "App not supported") :
        GooglePlayException(reason)

    data class EmptyDownloads(val code: Int, val reason: String = "File list empty") :
        GooglePlayException(reason) // Not sure about the root cause.

    data class Unknown(val code: Int, val reason: String = "¯\\_(ツ)_/¯") :
        GooglePlayException(reason)

    data class Server(val code: Int = 500, val reason: String = "Server error") :
        GooglePlayException(reason)
}
