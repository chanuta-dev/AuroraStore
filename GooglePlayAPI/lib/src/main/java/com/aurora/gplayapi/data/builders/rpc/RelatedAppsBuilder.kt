/*
 * SPDX-FileCopyrightText: 2024 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.data.builders.rpc

internal object RelatedAppsBuilder {
    val TAG: String = javaClass.simpleName

    fun build(packageName: String, tag: String = TAG): String {
        return """
            ["ag2B9c","[[null,[\"$packageName\",7],null,[[3,[6]],null,null,[1,8]]],[1]]",null,"${tag}@${packageName}"]
        """
            .trim()
    }
}
