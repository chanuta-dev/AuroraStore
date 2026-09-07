/*
 * SPDX-FileCopyrightText: 2026 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class ReleaseInfo(
    val versionName: String,
    val downloadUrl: String,
    val releaseNotes: String,
    val fileName: String
)

object SelfUpdateManager {
    private const val TAG = "SelfUpdateManager"
    private const val GITHUB_LATEST_RELEASE_URL = "https://api.github.com/repos/chanuta-dev/AuroraStore/releases/latest"

    suspend fun checkForUpdates(currentVersionName: String): ReleaseInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL(GITHUB_LATEST_RELEASE_URL)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "AuroraStore-App")
                connectTimeout = 10000
                readTimeout = 10000
            }

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                Log.w(TAG, "Failed to check update: HTTP ${connection.responseCode}")
                return@withContext null
            }

            val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(jsonString)

            val rawTagName = json.optString("tag_name", "")
            val latestVersionName = rawTagName.removePrefix("v").trim()
            val releaseNotes = json.optString("body", "")

            if (latestVersionName.isEmpty() || !isNewerVersion(currentVersionName, latestVersionName)) {
                Log.i(TAG, "App is up to date: current=$currentVersionName, latest=$latestVersionName")
                return@withContext null
            }

            val assets = json.optJSONArray("assets") ?: return@withContext null
            var apkUrl: String? = null
            var apkName: String? = null

            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                val name = asset.optString("name", "")
                if (name.endsWith(".apk", ignoreCase = true)) {
                    apkUrl = asset.optString("browser_download_url", "")
                    apkName = name
                    break
                }
            }

            if (!apkUrl.isNullOrEmpty()) {
                return@withContext ReleaseInfo(
                    versionName = latestVersionName,
                    downloadUrl = apkUrl,
                    releaseNotes = releaseNotes,
                    fileName = apkName ?: "AuroraStore-$latestVersionName.apk"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for app updates", e)
        }
        return@withContext null
    }

    private fun isNewerVersion(current: String, latest: String): Boolean {
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }
        val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }

        val maxLength = maxOf(currentParts.size, latestParts.size)
        for (i in 0 until maxLength) {
            val c = currentParts.getOrElse(i) { 0 }
            val l = latestParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }
}
