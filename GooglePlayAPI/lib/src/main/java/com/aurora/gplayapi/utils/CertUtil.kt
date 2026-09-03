/*
 * SPDX-FileCopyrightText: 2023 Aurora OSS
 * SPDX-FileCopyrightText: 2023 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.utils

import android.util.Base64

internal object CertUtil {

    @OptIn(ExperimentalStdlibApi::class)
    fun decodeHash(base64EncodedHash: String): String {
        return Base64.decode(base64EncodedHash, Base64.URL_SAFE).toHexString()
    }
}
