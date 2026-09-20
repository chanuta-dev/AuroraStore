package com.aurora.gplayapi

import com.aurora.gplayapi.data.models.App
import com.aurora.gplayapi.data.models.Artwork
import com.aurora.gplayapi.data.models.PlayFile
import org.json.JSONArray
import org.json.JSONObject
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
    private const val CFOPUSER_RELEASES_URL = "https://raw.githubusercontent.com/cfopuser/app-store/main/releases.json"
    private const val APP_SOURCES_URL = "https://raw.githubusercontent.com/chanuta159-design/aurora-whitelist/refs/heads/main/app-sources.json"

    // מפה דינמית: נטענת מרחוק מ-app-sources.json (אין יותר קידוד קשיח!)
    val dynamicCfopApps = ConcurrentHashMap<String, String>()
    val customApps = ConcurrentHashMap<String, JSONObject>()

    val latestReleases = ConcurrentHashMap<String, PatchedRelease>()
    private val appMetadataCache = ConcurrentHashMap<String, App>()

    fun isPatchedApp(packageName: String): Boolean =
        dynamicCfopApps.containsKey(packageName) || customApps.containsKey(packageName)

    fun getPatchedRelease(packageName: String): PatchedRelease? = latestReleases[packageName]

    fun isPatchedUpdateReady(packageName: String, installedVersionName: String): Boolean {
        val release = getPatchedRelease(packageName) ?: return false
        if (installedVersionName.isBlank()) return true
        return release.versionName.isNotBlank() && release.versionName != installedVersionName
    }

    suspend fun getAppMetadata(packageName: String): App? = withContext(Dispatchers.IO) {
        appMetadataCache[packageName]?.let { return@withContext it }

        // 1. אם זו אפליקציה עצמאית (CUSTOM) - הפרטים נמשכים ישירות מהמקור המאוחד
        customApps[packageName]?.let { obj ->
            val name = obj.optString("name").ifBlank { obj.optString("name_en", packageName) }
            val desc = obj.optString("description", "")
            val category = obj.optString("category", "כלים")
            val iconUrl = obj.optString("iconUrl", "")
            val downloadUrl = obj.optString("downloadUrl", "")
            val size = obj.optLong("size", 0L)
            val versionName = obj.optString("versionName", "1.0")
            val repo = obj.optString("repo", "")
            val developer = if (repo.contains("/")) repo.substringBefore("/") else "עצמאי"

            val app = App(
                packageName = packageName,
                id = packageName.hashCode(),
                displayName = name,
                description = desc,
                shortDescription = desc,
                categoryName = category,
                developerName = developer,
                versionName = versionName,
                iconArtwork = Artwork(url = iconUrl),
                isFree = true,
                inPlayStore = false,
                fileList = if (downloadUrl.isNotBlank()) {
                    listOf(
                        PlayFile(
                            name = "${packageName}.apk",
                            url = downloadUrl,
                            size = size,
                            type = PlayFile.Type.BASE
                        )
                    )
                } else emptyList()
            )
            appMetadataCache[packageName] = app
            return@withContext app
        }

        // 2. אם זו אפליקציה מ-CFOPUSER - נמשוך מ-app.json של CFOPUSER
        val appId = dynamicCfopApps[packageName] ?: return@withContext null
        val release = getPatchedRelease(packageName)

        try {
            val appJsonUrl = URL("https://raw.githubusercontent.com/cfopuser/app-store/main/apps/$appId/app.json")
            val jsonString = appJsonUrl.readText()
            val root = JSONObject(jsonString)

            val metadata = root.optJSONObject("metadata") ?: root
            val assets = root.optJSONObject("assets")
            val maintenance = root.optJSONObject("maintenance")

            val name = metadata.optString("name_he").ifBlank { metadata.optString("name", "") }
            val desc = metadata.optString("description_he").ifBlank { metadata.optString("description", "") }
            val fullDesc = metadata.optString("full_description_he").ifBlank { metadata.optString("full_description", desc) }
            val category = metadata.optString("category_he").ifBlank { metadata.optString("category", "") }
            val developer = maintenance?.optString("maintainer", "") ?: ""

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
        // שלב א': טעינת רשימת המקורות המאוחדת מ-GitHub
        try {
            val sourcesJson = URL(APP_SOURCES_URL).readText()
            parseAppSourcesJson(sourcesJson)
        } catch (e: Exception) {
            android.util.Log.e("PatchedAppManager", "Failed to fetch app-sources.json", e)
        }

        // שלב ב': טעינת שחרורי CFOPUSER עבור האפליקציות שסומנו כ-CFOPUSER
        try {
            val releasesJson = URL(CFOPUSER_RELEASES_URL).readText()
            parseCfopReleasesJson(releasesJson)
            true
        } catch (e: Exception) {
            android.util.Log.e("PatchedAppManager", "Failed to fetch cfopuser releases.json", e)
            false
        }
    }

    private fun parseAppSourcesJson(jsonString: String) {
        try {
            val jsonArray = JSONArray(jsonString)
            dynamicCfopApps.clear()
            customApps.clear()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val pkg = obj.optString("packageName", "").trim()
                if (pkg.isBlank()) continue

                val source = obj.optString("source", "CFOPUSER").uppercase()

                when (source) {
                    "CFOPUSER" -> {
                        val appId = obj.optString("appId", "")
                        if (appId.isNotBlank()) {
                            dynamicCfopApps[pkg] = appId
                        }
                    }
                    "CUSTOM" -> {
                        customApps[pkg] = obj
                        val versionName = obj.optString("versionName", "1.0")
                        val downloadUrl = obj.optString("downloadUrl", "")
                        val size = obj.optLong("size", 0L)

                        latestReleases[pkg] = PatchedRelease(
                            appId = pkg,
                            versionName = versionName,
                            downloadUrl = downloadUrl,
                            size = size,
                            fileName = "${pkg}.apk"
                        )
                    }
                }
            }
            android.util.Log.i("PatchedAppManager", "Loaded app sources: ${dynamicCfopApps.size} CFOPUSER, ${customApps.size} CUSTOM")
        } catch (e: Exception) {
            android.util.Log.e("PatchedAppManager", "Failed to parse app-sources.json", e)
        }
    }

    private fun parseCfopReleasesJson(jsonString: String) {
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

                if (!dynamicCfopApps.containsValue(appId) || processedApps.contains(appId)) {
                    continue
                }

                for (j in 0 until assets.length()) {
                    val asset = assets.getJSONObject(j)
                    val assetName = asset.optString("name", "")
                    val downloadUrl = asset.optString("browser_download_url", "")
                    val size = asset.optLong("size", 0L)

                    if (assetName.endsWith(".apk") && downloadUrl.isNotBlank()) {
                        val versionName = tagName.substringAfter("-v").replace("^v".toRegex(), "")
                        val packageName = dynamicCfopApps.entries.firstOrNull { it.value == appId }?.key
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
            android.util.Log.i("PatchedAppManager", "Loaded ${latestReleases.size} active releases")
        } catch (e: Exception) {
            android.util.Log.e("PatchedAppManager", "Failed to parse cfopuser releases.json", e)
        }
    }
}
