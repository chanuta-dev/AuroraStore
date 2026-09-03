/*
 * SPDX-FileCopyrightText: 2024 Aurora OSS
 * SPDX-FileCopyrightText: 2024 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.helpers.web

import com.aurora.gplayapi.data.builders.rpc.CategoryBuilder
import com.aurora.gplayapi.data.models.Category
import com.aurora.gplayapi.helpers.contracts.CategoryContract
import com.aurora.gplayapi.network.IHttpClient
import com.aurora.gplayapi.utils.dig
import java.util.Locale

class WebCategoryHelper : BaseWebHelper(), CategoryContract {

    override fun with(locale: Locale?) = apply {
        this.locale = locale
    }

    override fun using(httpClient: IHttpClient) = apply {
        this.httpClient = httpClient
    }

    override fun getAllCategories(type: Category.Type): List<Category> {
        val webType: Category.WebType = when (type) {
            Category.Type.GAME -> Category.WebType.GAME
            Category.Type.APPLICATION -> Category.WebType.APPLICATION
            Category.Type.FAMILY -> Category.WebType.FAMILY
        }

        return getAllCategoriesList(webType)
    }

    private fun getAllCategoriesList(type: Category.WebType): List<Category> {
        val response = execute(CategoryBuilder.build())
        val payload = response.dig<List<Any>>(
            CategoryBuilder.TAG,
            CategoryBuilder.TAG,
        )

        if (payload.isEmpty()) {
            return emptyList()
        }

        val categoryPayload = payload.dig<List<Any>>(0, 1, 0, 3)

        return parseCategory(type, categoryPayload)
    }

    private fun parseCategory(type: Category.WebType, payload: List<Any>): List<Category> {
        return payload.dig<List<Any>>(type.value, 3).map {
            val browseUrl = it.dig<String>(1, 0)
            Category(
                title = it.dig<String>(1, 1),
                browseUrl = browseUrl,
                imageUrl = getImageUrl(type, browseUrl)
            )
        }
    }

    private fun getImageUrl(type: Category.WebType, browseUrl: String): String {
        // Sample: https://play-apps-features.googleusercontent.com/png/gm3_categories_icons_apps/auto_and_vehicles.png
        val category = when (type) {
            Category.WebType.APPLICATION -> "apps"
            Category.WebType.GAME -> "games"
            Category.WebType.FAMILY -> "family"
        }

        var subCategory = browseUrl
            .split("/")
            .last()
            .lowercase()
            .replace("game_", "")
            .replace("app_", "")


        subCategory = when (subCategory) {
            "watch_face" -> return "watch_faces"
            "watch_app" -> return "watch_apps"
            else -> subCategory
        }

        return "https://play-apps-features.googleusercontent.com/png/gm3_categories_icons_${category}/${subCategory}.png"
    }
}
