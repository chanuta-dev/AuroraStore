/*
 * SPDX-FileCopyrightText: 2020-2024 Aurora OSS
 * SPDX-FileCopyrightText: 2023-2025 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.data.builders

import com.aurora.gplayapi.Image
import com.aurora.gplayapi.data.models.Artwork

internal object ArtworkBuilder {
    fun build(image: Image): Artwork {
        return Artwork(
            type = image.imageType,
            url = image.imageUrl,
            urlAlt = image.imageUrlAlt,
            aspectRatio = image.dimension.aspectRatio,
            width = image.dimension.width,
            height = image.dimension.height
        )
    }
}
