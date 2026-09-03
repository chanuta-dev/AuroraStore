/*
 * SPDX-FileCopyrightText: 2021-2024 Aurora OSS
 * SPDX-FileCopyrightText: 2023-2024 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.helpers

import com.aurora.gplayapi.GooglePlayApi
import com.aurora.gplayapi.ListResponse
import com.aurora.gplayapi.ModifyLibraryRequest
import com.aurora.gplayapi.data.models.App
import com.aurora.gplayapi.data.models.AuthData
import com.aurora.gplayapi.data.providers.HeaderProvider.getDefaultHeaders
import com.aurora.gplayapi.network.IHttpClient
import com.aurora.gplayapi.utils.bytesOrThrow

class LibraryHelper(authData: AuthData) : NativeHelper(authData) {

    override fun using(httpClient: IHttpClient) = apply {
        this.httpClient = httpClient
    }

    @Throws(Exception::class)
    fun getWishlistApps(): List<App> {
        val headers: Map<String, String> = getDefaultHeaders(authData)
        val params: Map<String, String> = mutableMapOf(
            "c" to "0",
            "dt" to "7",
            "libid" to "u-wl"
        )

        val playResponse = httpClient.get(GooglePlayApi.URL_LIBRARY, headers, params)

        val appList: MutableList<App> = mutableListOf()
        val listResponse: ListResponse = getResponseFromBytes(playResponse.bytesOrThrow())

        with(listResponse) {
            if (hasItem()) {
                with(item) {
                    if (subItemCount > 0) {
                        for (subItem in subItemList) {
                            appList.addAll(getAppsFromItem(subItem))
                        }
                    }
                }
            }
        }

        return appList
    }

    @Throws(Exception::class)
    fun wishlist(packageName: String, isAddRequest: Boolean = true): Boolean {
        val headers: Map<String, String> = getDefaultHeaders(authData)
        val builder = ModifyLibraryRequest.newBuilder()
            .setLibraryId("u-wl")

        if (isAddRequest) {
            builder.addAddPackageName(packageName)
        } else {
            builder.addRemovePackageName(packageName)
        }

        val playResponse = httpClient.post(
            GooglePlayApi.URL_MODIFY_LIBRARY,
            headers,
            builder.build().toByteArray()
        )

        return playResponse.isSuccessful
    }
}
