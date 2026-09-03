/*
 * SPDX-FileCopyrightText: 2024 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.data.builders.rpc

internal object NextBundleBuilder {
    val TAG: String = javaClass.simpleName

    private const val PAGINATED_STREAM_TOKEN: String = TokenRepository.PAGINATED_STREAM_TOKEN

    private fun getToken(nextPageToken: String): String {
        return PAGINATED_STREAM_TOKEN.format(nextPageToken)
    }

    fun build(
        category: String,
        nextPageToken: String,
        tag: String = TAG
    ): String {
        return """
            ["w3QCWb","[[null,2,\"${category}\",null,${getToken(nextPageToken)},null,2],[1,1]]",null,"$tag@$category"]
        """
            .trim()
    }
}
