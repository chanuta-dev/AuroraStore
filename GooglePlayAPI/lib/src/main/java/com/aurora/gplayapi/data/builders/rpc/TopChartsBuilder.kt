/*
 * SPDX-FileCopyrightText: 2024 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.data.builders.rpc

internal object TopChartsBuilder {
    val TAG: String = javaClass.simpleName

    private const val TOKEN: String = TokenRepository.TOP_CHARTS_TOKEN
    fun build(category: String, chartType: String, tag: String = TAG): String {
        return """
            ["vyAe2","[$TOKEN,[2,\"$chartType\",\"$category\"]]]",null,"$tag@$category$chartType"]
        """
            .trim()
    }
}
