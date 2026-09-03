/*
 * SPDX-FileCopyrightText: 2023-2024 Aurora OSS
 * SPDX-FileCopyrightText: 2023-2025 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.data.builders

import com.aurora.gplayapi.data.models.App
import com.aurora.gplayapi.data.models.Artwork
import com.aurora.gplayapi.data.models.ContentRating
import com.aurora.gplayapi.data.models.Rating
import com.aurora.gplayapi.data.models.Tag
import com.aurora.gplayapi.utils.dig

internal object WebAppBuilder {

    fun build(packageName: String, payload: List<Any>): App {
        val appInfoPayload = payload.dig<List<Any>>(1, 2)
        val downloadsPayload = appInfoPayload.dig<List<Any>>(13)
        val offersPayload = appInfoPayload.dig<List<Any>>(57, 0, 0, 0, 0)
        val ratingPayload = appInfoPayload.dig<List<Any>>(51)
        val contentRatingPayload = appInfoPayload.dig<List<Any>>(9)

        return App(
            packageName = packageName,
            id = packageName.hashCode(),
            categoryId = 3,
            displayName = appInfoPayload.dig<String>(0, 0),
            description = appInfoPayload.dig<String>(72, 0, 1),
            shortDescription = appInfoPayload.dig<String>(73, 0, 1),
            changes = appInfoPayload.dig<String>(144, 1, 1),
            versionName = appInfoPayload.dig<String>(140, 0, 0, 0),

            iconArtwork = parseArtwork(appInfoPayload.dig(95, 0), 4),
            coverArtwork = parseArtwork(appInfoPayload.dig(96, 0), 2),
            videoArtwork = parseArtwork(appInfoPayload.dig(100, 1, 0), 13),
            screenshots = appInfoPayload.dig<List<List<Any>>>(78, 0)
                .map { parseArtwork(it, 1) }.toMutableList(),

            categoryName = appInfoPayload.dig<String>(79, 0, 0, 2),
            targetSdk = appInfoPayload.dig<Double>(140, 1, 0, 0, 0).toInt(),
            updatedOn = appInfoPayload.dig<String>(145, 0, 0),

            containsAds = (appInfoPayload.dig<String>(48, 0)).isNotBlank(),
            earlyAccess = (appInfoPayload.dig<String>(18, 2)).isNotBlank(),
            inPlayStore = true,
            developerName = appInfoPayload.dig<String>(68, 0),
            developerEmail = appInfoPayload.dig<String>(69, 1, 0),
            developerWebsite = appInfoPayload.dig<String>(69, 0, 5, 2),
            developerAddress = appInfoPayload.dig<String>(69, 2, 0),

            downloadString = appInfoPayload.dig<String>(13, 3),
            installs = downloadsPayload.dig<Double>(2).toLong(),
            isFree = offersPayload.dig<Double>(1, 0, 0) == 0.0,
            price = offersPayload.dig<String>(1, 0, 2),
            rating = parseRating(ratingPayload) ?: Rating(),
            labeledRating = ratingPayload.dig<String>(0, 0),
            contentRating = ContentRating(
                title = contentRatingPayload.dig<String>(0),
                description = contentRatingPayload.dig<String>(2, 1),
                recommendation = contentRatingPayload.dig<String>(3, 1),
                artwork = parseArtwork(contentRatingPayload.dig(1), 3),
                recommendationAndDescriptionHtml = contentRatingPayload.dig<String>(4, 1)
            ),
            tags = parseTags(appInfoPayload.dig(118)),
        )
    }

    private fun parseRating(payload: List<Any>): Rating? {
        if (payload.isEmpty()) return null

        return Rating(
            average = payload.dig<Double>(0, 1).toFloat(),
            oneStar = payload.dig<Long>(1, 1, 1, 1),
            twoStar = payload.dig<Long>(1, 2, 1, 1),
            threeStar = payload.dig<Long>(1, 3, 1, 1),
            fourStar = payload.dig<Long>(1, 4, 1, 1),
            fiveStar = payload.dig<Long>(1, 5, 1, 1),
            label = payload.dig<String>(2, 0),
            abbreviatedLabel = payload.dig<String>(0, 0)
        )
    }

    fun parseArtwork(payload: List<Any>, artworkType: Int = 0): Artwork {
        return Artwork(
            type = artworkType,
            url = payload.dig<String>(3, 2),
            height = payload.dig<Double>(2, 0).toInt(),
            width = payload.dig<Double>(2, 1).toInt()
        )
    }

    fun parseTags(payload: List<Any>): List<Tag> {
        val tags = mutableListOf<Tag>()

        fun dig(node: Any?) {
            if (node !is List<*>) return

            for (item in node) {
                if (item is List<*>) {
                    val name = item.getOrNull(0).toString()
                    val url = item.dig<String>(1, 4, 2)

                    if (url.isNotBlank()) {
                        tags.add(Tag(name, url))
                    } else {
                        dig(item)
                    }
                }
            }
        }

        dig(payload)

        return tags
    }
}
