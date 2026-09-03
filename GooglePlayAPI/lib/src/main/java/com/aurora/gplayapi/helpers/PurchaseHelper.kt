/*
 * SPDX-FileCopyrightText: 2020-2024 Aurora OSS
 * SPDX-FileCopyrightText: 2023-2025 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.helpers

import com.aurora.gplayapi.AcquireRequest
import com.aurora.gplayapi.AcquireResponseWrapper
import com.aurora.gplayapi.Constants.PatchFormat
import com.aurora.gplayapi.DeliveryResponse
import com.aurora.gplayapi.GooglePlayApi
import com.aurora.gplayapi.ListResponse
import com.aurora.gplayapi.ResponseWrapper
import com.aurora.gplayapi.data.models.App
import com.aurora.gplayapi.data.models.AuthData
import com.aurora.gplayapi.data.models.PlayFile
import com.aurora.gplayapi.data.providers.HeaderProvider
import com.aurora.gplayapi.data.verifier.DfeResponseVerifier
import com.aurora.gplayapi.exceptions.GooglePlayException
import com.aurora.gplayapi.network.IHttpClient
import com.aurora.gplayapi.utils.CertUtil
import com.aurora.gplayapi.utils.bytesOrThrow

class PurchaseHelper(authData: AuthData) : NativeHelper(authData) {

    override fun using(httpClient: IHttpClient) = apply {
        this.httpClient = httpClient
    }

    /**
     * @param offset
     * @param getAllAppDetails
     * <code>false</code>: the returned App instances will only contain basic properties
     * <code>true</code>: all App details are retrieved using AppDetailsHelper
     * @return list of apps purchased by the selected account, starting at offset.
     * The number of entries is defined by Google (usually less than 20)
     */
    @Throws(Exception::class)
    fun getPurchaseHistory(offset: Int = 0, getAllAppDetails: Boolean = true): List<App> {
        val headers: MutableMap<String, String> = HeaderProvider.getDefaultHeaders(authData)
        val params: MutableMap<String, String> = mutableMapOf(
            "o" to "$offset"
        )
        val playResponse = httpClient.get(
            GooglePlayApi.PURCHASE_HISTORY_URL,
            headers,
            params
        )

        val purchaseAppList: MutableList<App> = mutableListOf()
        val listResponse: ListResponse = getResponseFromBytes(playResponse.bytesOrThrow())

        with(listResponse) {
            if (hasItem()) {
                for (subItem in item.subItemList) {
                    if (item.subItemCount > 0) {
                        if (item.hasAnnotations() &&
                            item.annotations.hasPurchaseHistoryDetails() &&
                            item.annotations.purchaseHistoryDetails.hasPurchaseStatus()
                        ) {
                            continue
                        }
                        purchaseAppList.addAll(getAppsFromItem(subItem))
                    }
                }
            }
        }

        if (!getAllAppDetails) {
            return purchaseAppList
        }
        return AppDetailsHelper(authData).getAppByPackageName(
            purchaseAppList.map { it.packageName }
                .distinct()
        )
    }

    @Throws(Exception::class)
    fun getDeliveryToken(
        packageName: String,
        versionCode: Long,
        offerType: Int,
        certificateHash: String? = null
    ): String {
        val params: MutableMap<String, String> = HashMap()
        params["ot"] = offerType.toString()
        params["doc"] = packageName
        params["vc"] = versionCode.toString()

        if (!certificateHash.isNullOrBlank()) {
            params["ch"] = certificateHash
        }

        val playResponse = httpClient.post(
            GooglePlayApi.PURCHASE_URL,
            HeaderProvider.getDefaultHeaders(authData),
            params
        )
        val payload = getPayLoadFromBytes(playResponse.responseBytes)

        return payload.buyResponse.encodedDeliveryToken
    }

    @Throws(Exception::class)
    fun getDeliveryResponse(
        packageName: String,
        splitModule: String? = null,
        installedVersionCode: Long? = null,
        updateVersionCode: Long,
        offerType: Int,
        patchFormat: PatchFormat = PatchFormat.GZIPPED_BSDIFF,
        deliveryToken: String,
        certificateHash: String? = null
    ): DeliveryResponse {
        val params: MutableMap<String, String> = HashMap()
        params["ot"] = offerType.toString()
        params["doc"] = packageName
        params["vc"] = updateVersionCode.toString()

        if (installedVersionCode != null && installedVersionCode > 0) {
            params["bvc"] = installedVersionCode.toString();
            params["pf"] = patchFormat.value.toString();
        }

        if (!splitModule.isNullOrBlank()) {
            params["mn"] = splitModule
        }

        if (!certificateHash.isNullOrBlank()) {
            params["ch"] = certificateHash
        }

        if (deliveryToken.isNotEmpty()) {
            params["dtok"] = deliveryToken
        }

        val playResponse = httpClient.get(
            GooglePlayApi.DELIVERY_URL,
            HeaderProvider.getDefaultHeaders(authData),
            params
        )

        val payload = ResponseWrapper.parseFrom(playResponse.bytesOrThrow()).payload

        return payload.deliveryResponse
    }

    @Throws(Exception::class)
    fun purchase(
        packageName: String,
        versionCode: Long,
        offerType: Int,
        certificateHash: String? = null,
        splitModule: String? = null,
        installedVersionCode: Long? = null,
        patchFormat: PatchFormat = PatchFormat.GZIPPED_BSDIFF
    ): List<PlayFile> {
        // --- 🚀 תחילת יירוט ההורדה עבור אפליקציות CFOPUSER ---
        if (com.aurora.gplayapi.PatchedAppManager.isPatchedApp(packageName)) {
            val release = com.aurora.gplayapi.PatchedAppManager.getPatchedRelease(packageName)
            if (release != null) {
                return listOf(
                    PlayFile(
                        name = "${packageName}.apk",
                        url = release.downloadUrl,
                        size = release.size,
                        type = PlayFile.Type.BASE,
                        sha1 = "",
                        sha256 = ""
                    )
                )
            }
        }
        // --- 🏁 סוף יירוט ההורדה ---
        // Let's try to acquire the app, we don't care if it fails.
        runCatching { acquire(packageName, versionCode, offerType) }

        val deliveryToken = getDeliveryToken(packageName, versionCode, offerType, certificateHash)
        val deliveryResponse = getDeliveryResponse(
            packageName = packageName,
            updateVersionCode = versionCode,
            offerType = offerType,
            deliveryToken = deliveryToken,
            certificateHash = certificateHash,
            splitModule = splitModule,
            installedVersionCode = installedVersionCode,
            patchFormat = patchFormat
        )

        when (deliveryResponse.status) {
            1 -> return getDownloadsFromDeliveryResponse(packageName, versionCode, deliveryResponse)
            2 -> throw GooglePlayException.AppNotSupported(2)
            3 -> throw GooglePlayException.AppNotPurchased(3)
            7 -> throw GooglePlayException.AppRemoved(7)
            9 -> throw GooglePlayException.AppNotSupported(9)
            else -> throw GooglePlayException.Unknown(deliveryResponse.status)
        }
    }

    fun acquire(packageName: String, versionCode: Long, offerType: Int): Boolean {
        val acquireRequest = AcquireRequest.newBuilder()
            .setPackage(
                AcquireRequest.Package.newBuilder()
                    .setPayload(
                        AcquireRequest.Package.Payload.newBuilder()
                            .setF2(1)
                            .setF3(3)
                            .setPackageName(packageName)
                    )
                    .setF2(1)
            )
            .setVersion(
                AcquireRequest.Version.newBuilder()
                    .setVersionCode(versionCode)
                    .setF3(0)
            )
            .setF15(0)
            .setOfferType(offerType)
            .setNonce(DfeResponseVerifier.generateNonce())
            .setF25(2)
            .setM30(
                AcquireRequest.Message30.newBuilder()
                    .setF1(2)
                    .setF2(0)
            )
            .build()

        val playResponse = httpClient.post(
            GooglePlayApi.ACQUIRE_URL,
            HeaderProvider.getDefaultHeaders(authData),
            acquireRequest.toByteArray()
        )

        val payload = AcquireResponseWrapper.parseFrom(playResponse.responseBytes)

        if (playResponse.isSuccessful) {
            with(payload.acquireResponse.acquirePayload.purchase) {
                // We don't really care about result here as we are anyway going to purchase the app later.
                return hasAppPurchase() || hasGamePurchase()
            }
        } else {
            return false
        }
    }

    private fun getDownloadsFromDeliveryResponse(
        packageName: String?,
        versionCode: Long,
        deliveryResponse: DeliveryResponse?
    ): List<PlayFile> {
        val fileList: MutableList<PlayFile> = mutableListOf()
        if (deliveryResponse != null) {
            // Add base apk
            val androidAppDeliveryData = deliveryResponse.appDeliveryData
            if (androidAppDeliveryData != null) {
                fileList.add(
                    PlayFile(
                        name = "base.apk",
                        url = androidAppDeliveryData.downloadUrl,
                        size = androidAppDeliveryData.downloadSize,
                        type = PlayFile.Type.BASE,
                        sha1 = CertUtil.decodeHash(androidAppDeliveryData.sha1),
                        sha256 = CertUtil.decodeHash(androidAppDeliveryData.sha256)
                    )
                )

                // Obb & patches (if any)
                val fileMetadataList = deliveryResponse.appDeliveryData.additionalFileList
                if (fileMetadataList != null) {
                    for (appFileMetadata in fileMetadataList) {
                        val isOBB = appFileMetadata.fileType == 0
                        val fileType = if (isOBB) "main" else "patch"
                        fileList.add(
                            PlayFile(
                                name = "$fileType.$versionCode.$packageName.obb",
                                url = appFileMetadata.downloadUrl,
                                size = appFileMetadata.size,
                                type = if (isOBB) PlayFile.Type.OBB else PlayFile.Type.PATCH,
                                sha1 = CertUtil.decodeHash(appFileMetadata.sha1)
                            )
                        )
                    }
                }

                // Add split apks (if any)
                val splitDeliveryDataList = deliveryResponse.appDeliveryData.splitDeliveryDataList
                if (fileMetadataList != null) {
                    for (splitDeliveryData in splitDeliveryDataList) {
                        fileList.add(
                            PlayFile(
                                name = "${splitDeliveryData.name}.apk",
                                url = splitDeliveryData.downloadUrl,
                                size = splitDeliveryData.downloadSize,
                                type = PlayFile.Type.SPLIT,
                                sha1 = CertUtil.decodeHash(splitDeliveryData.sha1),
                                sha256 = CertUtil.decodeHash(splitDeliveryData.sha256)
                            )
                        )
                    }
                }
            }
        }

        if (fileList.isEmpty()) {
            throw GooglePlayException.Unknown(-1)
        }

        return fileList
    }
}
