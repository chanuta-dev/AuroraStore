/*
 * SPDX-FileCopyrightText: 2020-2024 Aurora OSS
 * SPDX-FileCopyrightText: 2023 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.data.builders

import com.aurora.gplayapi.AggregateRating
import com.aurora.gplayapi.data.models.Rating

internal object RatingBuilder {

    fun build(rating: AggregateRating): Rating {
        return Rating(
            rating.starRating,
            rating.oneStarRatings,
            rating.twoStarRatings,
            rating.threeStarRatings,
            rating.fourStarRatings,
            rating.fiveStarRatings,
            rating.thumbsUpCount,
            rating.thumbsDownCount,
            rating.ratingLabel,
            rating.ratingCountLabelAbbreviated
        )
    }
}
