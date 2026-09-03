/*
 * SPDX-FileCopyrightText: 2020-2024 Aurora OSS
 * SPDX-FileCopyrightText: 2023-2025 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.data.builders

import com.aurora.gplayapi.AppDetails
import com.aurora.gplayapi.Constants
import com.aurora.gplayapi.Constants.IMAGE_TYPE_APP_ICON
import com.aurora.gplayapi.Constants.IMAGE_TYPE_APP_SCREENSHOT
import com.aurora.gplayapi.Constants.IMAGE_TYPE_CATEGORY_ICON
import com.aurora.gplayapi.Constants.IMAGE_TYPE_PAGE_BACKGROUND
import com.aurora.gplayapi.Constants.IMAGE_TYPE_VIDEO_THUMBNAIL
import com.aurora.gplayapi.DetailsResponse
import com.aurora.gplayapi.Item
import com.aurora.gplayapi.TagGroup
import com.aurora.gplayapi.TagType
import com.aurora.gplayapi.data.models.ActiveDevice
import com.aurora.gplayapi.data.models.App
import com.aurora.gplayapi.data.models.Artwork
import com.aurora.gplayapi.data.models.ContentRating
import com.aurora.gplayapi.data.models.EncodedCertificateSet
import com.aurora.gplayapi.data.models.PlayFile
import com.aurora.gplayapi.data.models.Support
import com.aurora.gplayapi.data.models.Tag
import com.aurora.gplayapi.data.models.details.AppInfo
import com.aurora.gplayapi.data.models.details.Badge
import com.aurora.gplayapi.data.models.details.Chip
import com.aurora.gplayapi.data.models.details.Dependencies
import com.aurora.gplayapi.data.models.editor.EditorChoiceReason
import com.aurora.gplayapi.utils.Util
import java.util.regex.Pattern

internal object AppBuilder {

    fun build(detailsResponse: DetailsResponse): App? {
        val app = build(detailsResponse.item)

        return app?.copy(
            userReview = ReviewBuilder.build(detailsResponse.userReview),
            detailsStreamUrl = detailsResponse.detailsStreamUrl,
            detailsPostAcquireStreamUrl = detailsResponse.postAcquireDetailsStreamUrl,
            footerHtml = detailsResponse.footerHtml
        )
    }

    fun build(item: Item): App? {
        // If the item doesn't have a valid id, it's a bogus package, and we should skip it.
        if (item.id.isNullOrBlank()) {
            return null
        }

        val appDetails = item.details.appDetails
        val artworks = item.imageList.map { ArtworkBuilder.build(it) }
        val activeDevices = appDetails.compatibility.activeDevicesList.map {
            ActiveDevice(name = it.name, requiredOS = it.requiredOS)
        }

        return App(
            packageName = item.id,
            id = item.id.hashCode(),
            categoryId = item.categoryId,
            displayName = item.title,
            description = item.descriptionHtml,
            shortDescription = item.promotionalDescription,
            shareUrl = item.shareUrl,
            restriction = Constants.Restriction.forInt(item.availability.restriction),

            versionName = appDetails.versionString,
            versionCode = appDetails.versionCode,
            targetSdk = appDetails.targetSdkVersion,
            updatedOn = appDetails.infoUpdatedOn,
            changes = appDetails.recentChangesHtml,

            categoryName = appDetails.categoryName,
            size = appDetails.infoDownloadSize,
            installs = getInstalls(appDetails.infoDownload),
            downloadString = appDetails.downloadLabelAbbreviated,
            permissions = appDetails.permissionList,
            containsAds = appDetails.hasInstallNotes(),
            inPlayStore = true,

            earlyAccess = appDetails.hasEarlyAccessInfo(),
            testingProgram = TestingProgramBuilder.build(appDetails),
            instantAppLink = appDetails.instantLink,

            offerType = if (item.offerCount > 0) item.getOffer(0).offerType else 0,
            isFree = if (item.offerCount > 0) item.getOffer(0).micros == 0L else false,
            price = if (item.offerCount > 0) item.getOffer(0).formattedAmount else String(),

            developerName = appDetails.developerName.ifBlank { item.creator },
            developerEmail = appDetails.developerEmail,
            developerAddress = appDetails.developerAddress,
            developerWebsite = appDetails.developerWebsite,

            labeledRating = item.aggregateRating.ratingLabel,
            rating = RatingBuilder.build(item.aggregateRating),

            categoryStreamUrl = item.annotations?.categoryStream?.link?.streamUrl,
            liveStreamUrl = item.annotations?.liveStreamUrl,
            promotionStreamUrl = item.annotations?.promotionStreamUrl,

            coverArtwork = artworks.find { it.type == IMAGE_TYPE_PAGE_BACKGROUND } ?: Artwork(),
            videoArtwork = artworks.find { it.type == IMAGE_TYPE_VIDEO_THUMBNAIL } ?: Artwork(),
            iconArtwork = artworks.find { it.type == IMAGE_TYPE_APP_ICON } ?: Artwork(),
            categoryArtwork = artworks.find { it.type == IMAGE_TYPE_CATEGORY_ICON } ?: Artwork(),
            screenshots = artworks.filter { it.type == IMAGE_TYPE_APP_SCREENSHOT }
                .ifEmpty { parseScreenshots(item) },

            appInfo = AppInfo(appInfoMap = parseAppInfo(item, activeDevices)),
            editorReason = parseEditorReasons(item),
            infoBadges = parseInfoBadges(item),
            displayBadges = parseDisplayBadges(item),
            fileList = parseFiles(item.id, appDetails.versionCode, appDetails),
            contentRating = parseContentRating(item),
            dependencies = parseDependencies(appDetails),
            chips = parseChips(item),

            certificateHashList = appDetails.certificateHashList,
            certificateSetList = appDetails.certificateSetList.map {
                EncodedCertificateSet(it.certificateHash, it.sha256)
            },

            compatibility = activeDevices,
            privacyPolicyUrl = item.annotations.privacyPolicyUrl,
            support = parseSupport(appDetails),
            tags = parseTags(appDetails.tagGroup)
        )
    }

    private fun parseEditorReasons(item: Item): EditorChoiceReason? {
        if (item.annotations?.hasReasons() == false) return null
        return EditorChoiceReason(
            bulletins = item.annotations.reasons.bulletinList,
            description = item.annotations.reasons.description
        )
    }

    private fun parseDisplayBadges(item: Item): List<Badge> {
        return item.annotations?.displayBadgeList?.map {
            Badge(
                textMajor = it.major,
                textMinor = it.minor,
                textMinorHtml = it.minorHtml,
                textDescription = it.description,
                artwork = ArtworkBuilder.build(it.image),
                link = it.link.toString()
            )
        } ?: emptyList()
    }

    private fun parseInfoBadges(item: Item): List<Badge> {
        val allBadges = mutableListOf<Badge>()
        item.annotations?.let { annotations ->
            annotations.badgeContainer.badgeList.forEach {
                allBadges.add(BadgeBuilder.build(it))
            }

            annotations.infoBadgeList?.let { badges ->
                badges.forEach { allBadges.add(BadgeBuilder.build(it)) }
            }

            annotations.badgeForLegacyRating?.let {
                allBadges.add(BadgeBuilder.build(it))
            }
        }

        return allBadges
    }

    private fun parseFiles(
        packageName: String,
        versionCode: Long,
        appDetails: AppDetails
    ): List<PlayFile> {
        return appDetails.fileList.map { file ->
            when {
                file.hasSplitId() -> PlayFile(
                    name = "${file.splitId}.$versionCode.apk",
                    type = PlayFile.Type.SPLIT,
                    size = file.size
                )

                file.fileType == 1 -> PlayFile(
                    name = "$packageName.$versionCode.obb",
                    type = PlayFile.Type.OBB,
                    size = file.size
                )

                else -> PlayFile(
                    name = "$packageName.$versionCode.apk",
                    type = PlayFile.Type.BASE,
                    size = file.size
                )
            }
        }
    }

    private fun parseContentRating(item: Item): ContentRating {
        return ContentRating(
            title = item.contentRating.title,
            description = item.contentRating.description,
            recommendation = item.contentRating.recommendation,
            artwork = Artwork(
                width = item.contentRating.contentRatingImage.dimension.width,
                height = item.contentRating.contentRatingImage.dimension.height,
                url = item.contentRating.contentRatingImage.image.url
            ),
            recommendationAndDescriptionHtml = item.contentRating.recommendationAndDescriptionHtml,
        )
    }

    private fun parseDependencies(appDetails: AppDetails): Dependencies {
        if (!appDetails.hasDependencies()) return Dependencies()
        return Dependencies(
            dependentPackages = appDetails.dependencies.dependencyList.map { it.packageName },
            dependentSplits = appDetails.dependencies.splitApksList.map { it },
            dependentLibraries = appDetails.dependencies.libraryDependencyList.map {
                App(packageName = it.packageName, versionCode = it.versionCode, offerType = 1)
            },
            totalSize = appDetails.dependencies.size,
            targetSDK = appDetails.dependencies.targetSdk
        )
    }

    private fun parseAppInfo(item: Item, activeDevices: List<ActiveDevice>): Map<String, String> {
        if (!item.hasAppInfo()) return emptyMap()

        val map = mutableMapOf<String, String>()
        val minAndroidVersion = activeDevices
            .map { it.requiredOS.split(" ")[0].toFloatOrNull() ?: 1F }
            .minOfOrNull { it } ?: 1F

        item.appInfo.sectionList.forEach {
            if (it.hasLabel() && it.hasContainer() && it.container.hasDescription()) {
                map[it.label] = it.container.description
            }
        }

        map["DOWNLOAD"] = item.details.appDetails.infoDownload
        map["UPDATED_ON"] = item.details.appDetails.infoUpdatedOn
        map["REQUIRES"] = minAndroidVersion.toString()

        return map
    }

    private fun parseScreenshots(item: Item): List<Artwork> {
        return when {
            item.hasAnnotations() && item.annotations.hasSectionImage() -> {
                item.annotations.sectionImage.imageContainerList.map { imageContainer ->
                    ArtworkBuilder.build(imageContainer.image)
                }
            }

            else -> emptyList()
        }
    }

    private fun parseChips(item: Item): List<Chip> {
        return item.annotations.chipList?.map {
            Chip(title = it.title, streamUrl = it.stream?.link?.streamUrl)
        } ?: emptyList()
    }

    private fun getInstalls(downloadInfo: String): Long {
        val matcher = Pattern.compile("[\\d]+")
            .matcher(downloadInfo.replace("[,.\\s]+".toRegex(), ""))
        return if (matcher.find()) Util.parseLong(matcher.group(0), 0) else 0
    }

    private fun parseSupport(appDetails: AppDetails): Support {
        return Support(
            developerName = appDetails.support.developerName,
            developerEmail = appDetails.support.developerEmail,
            developerAddress = appDetails.support.developerAddress,
            developerPhoneNumber = appDetails.support.developerPhoneNumber
        )
    }

    private fun parseTags(tagGroup: TagGroup): List<Tag> {
        val tags = mutableListOf<Tag>()

        fun parseTagEntries(tagType: TagType): List<Tag> {
            return tagType.entriesList.map { entry ->
                val url = when {
                    entry.hasMetadata() -> {
                        val metadata = entry.metadata
                        when {
                            metadata.hasCategory() -> metadata.category.url
                            metadata.hasSearch() -> metadata.search.url
                            else -> null
                        }
                    }

                    else -> null
                }

                Tag(entry.name, url)
            }
        }

        listOfNotNull(
            tagGroup.type1,
            tagGroup.type2,
            tagGroup.type3,
            tagGroup.type4,
            tagGroup.type5,
            tagGroup.type6
        ).forEach { tags.addAll(parseTagEntries(it)) }

        return tags
    }
}
