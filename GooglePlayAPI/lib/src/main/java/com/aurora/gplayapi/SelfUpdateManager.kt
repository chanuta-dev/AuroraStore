/*
 * SPDX-FileCopyrightText: 2026 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.gplayapi

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class ReleaseInfo(
    val versionName: String,
    val downloadUrl: String,
    val releaseNotes: String,
    val fileName: String,
    val isPrerelease: Boolean = false
)

object SelfUpdateManager {
    private const val TAG = "SelfUpdateManager"
    private const val GITHUB_LATEST_RELEASE_URL = "https://api.github.com/repos/chanuta-dev/AuroraStore/releases/latest"
    private const val GITHUB_ALL_RELEASES_URL = "https://api.github.com/repos/chanuta-dev/AuroraStore/releases"

    suspend fun checkForUpdates(currentVersionName: String, includeBeta: Boolean = false): ReleaseInfo? = withContext(Dispatchers.IO) {
        try {
            if (includeBeta) {
                return@withContext checkForAllReleases(currentVersionName)
            } else {
                return@withContext checkForLatestStableRelease(currentVersionName)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for app updates", e)
        }
        return@withContext null
    }

    private fun checkForLatestStableRelease(currentVersionName: String): ReleaseInfo? {
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
            return null
        }

        val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
        val json = JSONObject(jsonString)
        return parseReleaseJson(json, currentVersionName)
    }

    private fun checkForAllReleases(currentVersionName: String): ReleaseInfo? {
        val url = URL(GITHUB_ALL_RELEASES_URL)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Accept", "application/vnd.github.v3+json")
            setRequestProperty("User-Agent", "AuroraStore-App")
            connectTimeout = 10000
            readTimeout = 10000
        }

        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            Log.w(TAG, "Failed to check all releases: HTTP ${connection.responseCode}")
            return null
        }

        val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
        val releasesArray = JSONArray(jsonString)

        var bestRelease: ReleaseInfo? = null
        var highestVersion = currentVersionName

        for (i in 0 until releasesArray.length()) {
            val releaseObj = releasesArray.getJSONObject(i)
            val releaseInfo = parseReleaseJson(releaseObj, highestVersion)
            if (releaseInfo != null) {
                bestRelease = releaseInfo
                highestVersion = releaseInfo.versionName
            }
        }

        return bestRelease
    }

    private fun parseReleaseJson(json: JSONObject, currentVersionName: String): ReleaseInfo? {
        val rawTagName = json.optString("tag_name", "")
        val latestVersionName = rawTagName.removePrefix("v").trim()
        val releaseNotes = json.optString("body", "")
        val isPrerelease = json.optBoolean("prerelease", false)

        if (latestVersionName.isEmpty() || !isNewerVersion(currentVersionName, latestVersionName)) {
            return null
        }

        val assets = json.optJSONArray("assets") ?: return null
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
            return ReleaseInfo(
                versionName = latestVersionName,
                downloadUrl = apkUrl,
                releaseNotes = releaseNotes,
                fileName = apkName ?: "AuroraStore-$latestVersionName.apk",
                isPrerelease = isPrerelease
            )
        }
        return null
    }

    private fun isNewerVersion(current: String, latest: String): Boolean {
        val currentClean = current.substringBefore("-")
        val latestClean = latest.substringBefore("-")

        val currentParts = currentClean.split(".").mapNotNull { it.toIntOrNull() }
        val latestParts = latestClean.split(".").mapNotNull { it.toIntOrNull() }

        val maxLength = maxOf(currentParts.size, latestParts.size)
        for (i in 0 until maxLength) {
            val c = currentParts.getOrElse(i) { 0 }
            val l = latestParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }

        // If base versions are equal, check build/beta numbers or consider release > beta
        if (current.contains("beta") && !latest.contains("beta")) return true
        return false
    }
}
