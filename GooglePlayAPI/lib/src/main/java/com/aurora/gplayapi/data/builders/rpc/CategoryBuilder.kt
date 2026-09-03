/*
 * SPDX-FileCopyrightText: 2024 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.data.builders.rpc

internal object CategoryBuilder {
    val TAG: String = javaClass.simpleName

    fun build(tag: String = TAG): String {
        return """
            ["KT5WVe","[1]",null,"${tag}@${tag}"]
        """
            .trim()
    }
}
