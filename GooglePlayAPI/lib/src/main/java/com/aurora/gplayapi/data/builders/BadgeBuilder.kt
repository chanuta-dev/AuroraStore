/*
 * SPDX-FileCopyrightText: 2020-2024 Aurora OSS
 * SPDX-FileCopyrightText: 2023-2025 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.data.builders

import com.aurora.gplayapi.data.models.details.Badge

internal object BadgeBuilder {

    fun build(badge: com.aurora.gplayapi.Badge): Badge {
        return Badge(
            textMajor = badge.major,
            textMinor = badge.minor,
            textMinorHtml = badge.minorHtml,
            textDescription = badge.description,
            artwork = ArtworkBuilder.build(badge.image),
            link = badge.link.toString()
        )
    }
}
