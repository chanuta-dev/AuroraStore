/*
 * SPDX-FileCopyrightText: 2024 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.helpers.contracts

import com.aurora.gplayapi.data.models.StreamBundle
import com.aurora.gplayapi.data.models.StreamCluster
import com.aurora.gplayapi.helpers.contracts.StreamContract.Category

interface CategoryStreamContract {
    fun fetch(url: String): StreamBundle
    fun nextStreamCluster(id: Int, nextPageUrl: String): StreamCluster
    fun nextStreamBundle(id: Int, category: Category, nextPageToken: String): StreamBundle
}
