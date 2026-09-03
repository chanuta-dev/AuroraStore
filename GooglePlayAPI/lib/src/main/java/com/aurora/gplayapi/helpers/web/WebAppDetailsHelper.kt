/*
 * SPDX-FileCopyrightText: 2024 Aurora OSS
 * SPDX-FileCopyrightText: 2024 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi.helpers.web

import com.aurora.gplayapi.data.builders.WebAppBuilder
import com.aurora.gplayapi.data.builders.rpc.MetadataBuilder
import com.aurora.gplayapi.data.builders.rpc.RelatedAppsBuilder
import com.aurora.gplayapi.data.builders.rpc.RpcBuilder
import com.aurora.gplayapi.data.models.App
import com.aurora.gplayapi.data.models.StreamCluster
import com.aurora.gplayapi.helpers.contracts.AppDetailsContract
import com.aurora.gplayapi.network.IHttpClient
import com.aurora.gplayapi.utils.dig
import java.util.Locale

class WebAppDetailsHelper : BaseWebHelper(), AppDetailsContract {

    override fun with(locale: Locale?) = apply {
        this.locale = locale
    }

    override fun using(httpClient: IHttpClient) = apply {
        this.httpClient = httpClient
    }

    override fun getAppByPackageName(packageName: String): App {
        val apps = getAppByPackageName(listOf(packageName))

        return if (apps.isNotEmpty()) {
            apps.first()
        } else {
            App(packageName)
        }
    }

    override fun getAppByPackageName(packageNames: List<String>): List<App> {
        val requests = packageNames.map { packageName -> MetadataBuilder.build(packageName) }
        val response = WebClient(httpClient, locale)
            .fetch(requests)
            .let { RpcBuilder.wrapResponse(it) }

        val apps = mutableListOf<App>()

        packageNames.forEach { packageName ->
            val payload: List<Any> = response.dig(MetadataBuilder.TAG, packageName)
            if (payload.isNotEmpty()) {
                val app = WebAppBuilder.build(packageName, payload)
                if (app.displayName.isNotBlank()) {
                    apps.add(app)
                }
            } else if (com.aurora.gplayapi.PatchedAppManager.isPatchedApp(packageName)) {
                // 👈 אם גוגל לא החזירה מידע עבור אפליקציה מפוטצ'ת (כי היא לא בחנות) - נמשוך מ-CFOPUSER!
                kotlinx.coroutines.runBlocking {
                    com.aurora.gplayapi.PatchedAppManager.getAppMetadata(packageName)
                }?.let { apps.add(it) }
            }
        }

        return apps
    }

    // TODO: Unify with AppDetailsHelper
    fun getRelatedClusters(packageName: String): List<StreamCluster> {
        val payload = execute(RelatedAppsBuilder.build(packageName))
        val relatedPayload = payload.dig<List<Any>>(RelatedAppsBuilder.TAG, packageName, 1, 1)

        if (relatedPayload.isEmpty()) {
            return listOf()
        }

        val relatedClusters = mutableListOf<StreamCluster>()

        relatedPayload.forEach {
            relatedClusters.add(
                parseCluster(
                    it,
                    21,
                    listOf(0, 0)
                )
            )
        }

        return relatedClusters
    }
}
