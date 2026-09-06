package com.aurora.store.data.updater

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.aurora.store.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class AppReleaseInfo(
    val versionName: String,
    val downloadUrl: String,
    val changelog: String,
    val apkName: String
)

object SelfUpdateManager {
    private const val TAG = "SelfUpdateManager"
    private const val GITHUB_REPO = "chanuta-dev/AuroraStore"
    private const val RELEASES_API = "https://api.github.com/repos/$GITHUB_REPO/releases/latest"

    private val _updateAvailable = MutableStateFlow<AppReleaseInfo?>(null)
    val updateAvailable = _updateAvailable.asStateFlow()

    suspend fun checkForUpdates() {
        withContext(Dispatchers.IO) {
            try {
                val url = URL(RELEASES_API)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("Accept", "application/vnd.github.v3+json")
                    setRequestProperty("User-Agent", "AuroraStore-SelfUpdater")
                    connectTimeout = 10000
                    readTimeout = 10000
                }

                if (connection.responseCode != 200) {
                    Log.d(TAG, "GitHub API returned response code: ${connection.responseCode}")
                    return@withContext
                }

                val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseBody)

                val tagName = json.optString("tag_name", "")
                val releaseVersion = tagName.removePrefix("v").trim()
                val currentVersion = BuildConfig.VERSION_NAME.removePrefix("v").trim()
                val changelog = json.optString("body", "")

                val assets = json.optJSONArray("assets") ?: return@withContext
                var apkDownloadUrl: String? = null
                var apkName: String? = null

                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.optString("name", "")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        apkDownloadUrl = asset.optString("browser_download_url")
                        apkName = name
                        break
                    }
                }

                if (apkDownloadUrl != null && isNewerVersion(releaseVersion, currentVersion)) {
                    _updateAvailable.value = AppReleaseInfo(
                        versionName = releaseVersion,
                        downloadUrl = apkDownloadUrl,
                        changelog = changelog,
                        apkName = apkName ?: "AuroraStore.apk"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to check for self updates", e)
            }
        }
    }

    fun dismissUpdate() {
        _updateAvailable.value = null
    }

    fun openDownload(context: Context, downloadUrl: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open download URL", e)
        }
    }

    private fun isNewerVersion(remoteVer: String, localVer: String): Boolean {
        if (remoteVer.isEmpty() || localVer.isEmpty()) return false
        if (remoteVer == localVer) return false

        val remoteParts = remoteVer.split("-")[0].split(".").mapNotNull { it.toIntOrNull() }
        val localParts = localVer.split("-")[0].split(".").mapNotNull { it.toIntOrNull() }

        val maxLength = maxOf(remoteParts.size, localParts.size)
        for (i in 0 until maxLength) {
            val remotePart = remoteParts.getOrElse(i) { 0 }
            val localPart = localParts.getOrElse(i) { 0 }
            if (remotePart > localPart) return true
            if (remotePart < localPart) return false
        } 
        return false
    }
}
