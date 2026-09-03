/*
 * SPDX-FileCopyrightText: 2024 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.data.builders.rpc

internal object NextClusterBuilder {
    val TAG: String = javaClass.simpleName

    private const val TOKEN: String = TokenRepository.STREAM_TOKEN

    fun build(nextPageToken: String, tag: String = TAG): String {
        return """
            ["qnKhOb","[[null,${TOKEN},null,\"${nextPageToken}\"],[1]]",null,"$tag@${nextPageToken.hashCode()}"]
        """
            .trim()
    }
}
