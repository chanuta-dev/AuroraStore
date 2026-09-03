/*
 * SPDX-FileCopyrightText: 2024 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.helpers.contracts

import com.aurora.gplayapi.data.models.StreamBundle
import com.aurora.gplayapi.data.models.StreamCluster

interface StreamContract {
    fun fetch(type: Type, category: Category): StreamBundle

    fun nextStreamCluster(id: Int, nextPageUrl: String): StreamCluster

    fun nextStreamBundle(id: Int, category: Category, nextPageToken: String): StreamBundle

    enum class Category(var value: String) {
        APPLICATION("APPLICATION"),
        GAME("GAME"),
        NONE("NONE");
    }

    enum class Type(var value: String) {
        EARLY_ACCESS("appsEarlyAccessStream"),
        EDITOR_CHOICE("getAppsEditorsChoiceStream"),
        HOME("getHomeStream"),
        MY_APPS_LIBRARY("myAppsStream?tab=LIBRARY"),
        PREMIUM_GAMES("getAppsPremiumGameStream"),
        SUB_NAV("subnavHome"),
        TOP_CHART("topChartsStream");
    }
}
