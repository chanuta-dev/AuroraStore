/*
 * SPDX-FileCopyrightText: 2024 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.helpers.contracts

import com.aurora.gplayapi.data.models.StreamCluster

interface TopChartsContract {
    fun getCluster(category: String, chart: String): StreamCluster

    fun getNextStreamCluster(id: Int, nextPageUrl: String): StreamCluster

    enum class Chart(var value: String) {
        TOP_SELLING_FREE("apps_topselling_free"),
        TOP_SELLING_PAID("apps_topselling_paid"),
        TOP_GROSSING("apps_topgrossing"),
        MOVERS_SHAKERS("apps_movers_shakers");
    }

    enum class Type(var value: String) {
        GAME("GAME"),
        APPLICATION("APPLICATION");
    }
}
