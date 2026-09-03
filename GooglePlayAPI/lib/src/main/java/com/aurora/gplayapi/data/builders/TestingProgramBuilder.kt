/*
 * SPDX-FileCopyrightText: 2020-2024 Aurora OSS
 * SPDX-FileCopyrightText: 2023-2025 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.data.builders

import com.aurora.gplayapi.AppDetails
import com.aurora.gplayapi.data.models.details.TestingProgram

internal object TestingProgramBuilder {

    fun build(appDetails: AppDetails): TestingProgram? {
        return if (appDetails.hasTestingProgramInfo()) {
            TestingProgram(
                artwork = ArtworkBuilder.build(appDetails.testingProgramInfo.image),
                displayName = appDetails.testingProgramInfo.displayName,
                email = appDetails.testingProgramInfo.email,
                isAvailable = true,
                isSubscribed = appDetails.testingProgramInfo.subscribed,
                isSubscribedAndInstalled = appDetails.testingProgramInfo.subscribedAndInstalled
            )
        } else {
            null
        }
    }
}
