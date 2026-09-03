/*
 * SPDX-FileCopyrightText: 2021-2024 Aurora OSS
 * SPDX-FileCopyrightText: 2023-2024 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.data.builders

import com.aurora.gplayapi.data.models.UserProfile

internal object UserProfileBuilder {

    fun build(userProfileProto: com.aurora.gplayapi.UserProfile): UserProfile {
        return UserProfile(
            name = userProfileProto.name,
            email = userProfileProto.profileDescription,
            artwork = ArtworkBuilder.build(userProfileProto.getImage(0))
        )
    }
}
