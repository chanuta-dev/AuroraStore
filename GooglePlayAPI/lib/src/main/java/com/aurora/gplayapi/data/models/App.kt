/*
 * SPDX-FileCopyrightText: 2020-2023 Aurora OSS
 * SPDX-FileCopyrightText: 2023-2025 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.data.models

import android.os.Parcelable
import com.aurora.gplayapi.Constants.Restriction
import com.aurora.gplayapi.data.models.details.AppInfo
import com.aurora.gplayapi.data.models.details.Badge
import com.aurora.gplayapi.data.models.details.Chip
import com.aurora.gplayapi.data.models.details.Dependencies
import com.aurora.gplayapi.data.models.details.TestingProgram
import com.aurora.gplayapi.data.models.editor.EditorChoiceReason
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class App(
    val packageName: String,
    val id: Int = 0,
    val appInfo: AppInfo = AppInfo(),
    val categoryArtwork: Artwork = Artwork(),
    val categoryId: Int = 0,
    val categoryName: String = String(),
    val categoryStreamUrl: String? = String(),
    val changes: String = String(),
    val chips: List<Chip> = listOf(),
    val containsAds: Boolean = false,
    val coverArtwork: Artwork = Artwork(),
    val dependencies: Dependencies = Dependencies(),
    val description: String = String(),
    val detailsStreamUrl: String? = String(),
    val detailsPostAcquireStreamUrl: String? = String(),
    val developerAddress: String = String(),
    val developerEmail: String = String(),
    val developerName: String = String(),
    val developerWebsite: String = String(),
    val displayBadges: List<Badge> = listOf(),
    val displayName: String = String(),
    val editorReason: EditorChoiceReason? = null,
    val downloadString: String = String(),
    val earlyAccess: Boolean = false,
    val fileList: List<PlayFile> = listOf(),
    val footerHtml: String = String(),
    val iconArtwork: Artwork = Artwork(),
    val infoBadges: List<Badge> = listOf(),
    val inPlayStore: Boolean = false,
    val installs: Long = 0,
    val instantAppLink: String = String(),
    val isFree: Boolean = false,
    val isInstalled: Boolean = false,
    val isSystem: Boolean = false,
    val labeledRating: String = String(),
    val liveStreamUrl: String? = String(),
    val offerDetails: Map<String, String> = mapOf(),
    val offerType: Int = 0,
    val permissions: List<String> = listOf(),
    val price: String = String(),
    val promotionStreamUrl: String? = String(),
    val rating: Rating = Rating(),
    val relatedLinks: Map<String, String> = mapOf(),
    val restriction: Restriction = Restriction.NOT_RESTRICTED,
    val screenshots: List<Artwork> = listOf(),
    val shareUrl: String = String(),
    val shortDescription: String = String(),
    val size: Long = 0,
    val tags: List<Tag> = listOf(),
    val targetSdk: Int = 21,
    val testingProgram: TestingProgram? = null,
    val userReview: Review = Review(),
    val updatedOn: String = String(),
    val versionCode: Long = 0,
    val versionName: String = String(),
    val videoArtwork: Artwork = Artwork(),
    val certificateHashList: List<String> = listOf(),
    val certificateSetList: List<EncodedCertificateSet> = listOf(),
    val compatibility: List<ActiveDevice> = listOf(),
    val contentRating: ContentRating = ContentRating(),
    val privacyPolicyUrl: String = String(),
    val support: Support = Support()
) : Parcelable {

    override fun hashCode(): Int {
        return packageName.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is App -> packageName == other.packageName
            else -> false
        }
    }
}
