package com.aurora.gplayapi

import com.aurora.gplayapi.data.models.App
import com.aurora.gplayapi.data.models.Artwork
import com.aurora.gplayapi.data.models.PlayFile
import org.json.JSONArray
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

data class PatchedRelease(
    val appId: String,
    val versionName: String,
    val downloadUrl: String,
    val size: Long,
    val fileName: String
)

object PatchedAppManager {
    private const val RELEASES_URL = "https://raw.githubusercontent.com/cfopuser/app-store/main/releases.json"

    val selectedPatchedApps: Map<String, String> = mapOf(
        "com.whatsapp" to "whatsapp",
        "com.spotify.music" to "spotify",
        "com.bnhp.payments.paymentsapp" to "bit",
        "com.metrolist.music" to "metrolist"
    )
    val latestReleases = ConcurrentHashMap<String, PatchedRelease>()

    fun isPatchedApp(packageName: String): Boolean = selectedPatchedApps.containsKey(packageName)

    fun getPatchedRelease(packageName: String): PatchedRelease? = latestReleases[packageName]

    fun isPatchedUpdateReady(packageName: String, installedVersionName: String): Boolean {
        val release = getPatchedRelease(packageName) ?: return false
        if (installedVersionName.isBlank()) return true
        return release.versionName.isNotBlank() && release.versionName != installedVersionName
    }

    private val appMetadataCache = java.util.concurrent.ConcurrentHashMap<String, App>()

    suspend fun getAppMetadata(packageName: String): App? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        appMetadataCache[packageName]?.let { return@withContext it }

        val appId = selectedPatchedApps[packageName] ?: return@withContext null
        val release = getPatchedRelease(packageName)

        try {
            val appJsonUrl = java.net.URL("https://raw.githubusercontent.com/cfopuser/app-store/main/apps/$appId/app.json")
            val jsonString = appJsonUrl.readText()
            val root = org.json.JSONObject(jsonString)

            val metadata = root.optJSONObject("metadata") ?: root
            val assets = root.optJSONObject("assets")
            val maintenance = root.optJSONObject("maintenance")

            // משיכה דינמית לחלוטין של כל השדות ישירות מתוך ה-JSON של האפליקציה:
            val name = metadata.optString("name_he").ifBlank { metadata.optString("name", "") }
            val desc = metadata.optString("description_he").ifBlank { metadata.optString("description", "") }
            val fullDesc = metadata.optString("full_description_he").ifBlank { metadata.optString("full_description", desc) }
            val category = metadata.optString("category_he").ifBlank { metadata.optString("category", "") }
            val developer = maintenance?.optString("maintainer", "") ?: ""

            // חילוץ האייקון בצורה דינמית
            val rawIconUrl = assets?.optString("icon_url", "") ?: ""
            val iconUrl = when {
                rawIconUrl.startsWith("http") -> rawIconUrl
                rawIconUrl.isNotBlank() -> "https://raw.githubusercontent.com/cfopuser/app-store/main/$rawIconUrl"
                else -> ""
            }

            val app = App(
                packageName = packageName,
                id = packageName.hashCode(),
                displayName = name,
                description = fullDesc,
                shortDescription = desc,
                categoryName = category,
                developerName = developer,
                versionName = release?.versionName ?: "",
                iconArtwork = Artwork(url = iconUrl),
                isFree = true,
                inPlayStore = false,
                fileList = if (release != null) {
                    listOf(
                        PlayFile(
                            name = "${packageName}.apk",
                            url = release.downloadUrl,
                            size = release.size,
                            type = PlayFile.Type.BASE
                        )
                    )
                } else emptyList()
            )

            appMetadataCache[packageName] = app
            app
        } catch (e: Exception) {
            android.util.Log.e("PatchedAppManager", "Failed to load app.json for $appId", e)
            null
        }
    }
    suspend fun fetchReleases(): Boolean = withContext(Dispatchers.IO) {
        try {
            val releasesJson = URL(RELEASES_URL).readText()
            parseReleasesJson(releasesJson)
            true
        } catch (e: Exception) {
            android.util.Log.e("PatchedAppManager", "Failed to fetch releases.json", e)
            false
        }
    }

    private fun parseReleasesJson(jsonString: String) {
        try {
            val jsonArray = JSONArray(jsonString)
            val processedApps = mutableSetOf<String>()

            for (i in 0 until jsonArray.length()) {
                val releaseObj = jsonArray.getJSONObject(i)
                val tagName = releaseObj.optString("tag_name", "")
                val assets = releaseObj.optJSONArray("assets") ?: continue

                if (assets.length() == 0) continue

                val appId = when {
                    tagName.contains("-v") -> tagName.substringBefore("-v")
                    tagName.startsWith("v") -> "bit"
                    else -> continue
                }

                if (!selectedPatchedApps.containsValue(appId) || processedApps.contains(appId)) {
                    continue
                }

                for (j in 0 until assets.length()) {
                    val asset = assets.getJSONObject(j)
                    val assetName = asset.optString("name", "")
                    val downloadUrl = asset.optString("browser_download_url", "")
                    val size = asset.optLong("size", 0L)

                    if (assetName.endsWith(".apk") && downloadUrl.isNotBlank()) {
                        val versionName = tagName.substringAfter("-v").replace("^v".toRegex(), "")

                        val packageName = selectedPatchedApps.entries.firstOrNull { it.value == appId }?.key
                        if (packageName != null) {
                            latestReleases[packageName] = PatchedRelease(
                                appId = appId,
                                versionName = versionName,
                                downloadUrl = downloadUrl,
                                size = size,
                                fileName = assetName
                            )
                            processedApps.add(appId)
                        }
                        break
                    }
                }
            }
            android.util.Log.i("PatchedAppManager", "Loaded ${latestReleases.size} selected patched apps")
        } catch (e: Exception) {
            android.util.Log.e("PatchedAppManager", "Failed to parse releases.json", e)
        }
    }
}