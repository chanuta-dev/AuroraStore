package com.aurora.gplayapi

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

object WhitelistManager {
    private const val WHITELIST_URL = "https://raw.githubusercontent.com/chanuta159-design/aurora-whitelist/refs/heads/main/categorized-whitelist.json"

    val authorizedPackages = CopyOnWriteArrayList<String>()
    val categorizedApps = ConcurrentHashMap<String, List<String>>()

    fun isAuthorized(packageName: String): Boolean = authorizedPackages.contains(packageName)

    suspend fun fetchRemoteWhitelist(): Boolean = withContext(Dispatchers.IO) {
        try {
            val jsonString = URL(WHITELIST_URL).readText()
            val jsonObject = JSONObject(jsonString)

            authorizedPackages.clear()
            categorizedApps.clear()

            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val categoryName = keys.next()
                val packagesArray = jsonObject.getJSONArray(categoryName)
                val packagesList = mutableListOf<String>()

                for (i in 0 until packagesArray.length()) {
                    val pkg = packagesArray.getString(i)
                    packagesList.add(pkg)
                    if (!authorizedPackages.contains(pkg)) {
                        authorizedPackages.add(pkg)
                    }
                }
                categorizedApps[categoryName] = packagesList
            }
            true
        } catch (e: Exception) {
            android.util.Log.e("Whitelist", "Failed to load categorized whitelist", e)
            false
        }
    }
}