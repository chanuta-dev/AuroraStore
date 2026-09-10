package com.aurora.store.viewmodel.request

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurora.gplayapi.WhitelistManager
import com.aurora.gplayapi.data.models.App
import com.aurora.gplayapi.helpers.web.WebSearchHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class AppRequestItem(
    val app: App,
    val isAuthorized: Boolean,
    val isSelected: Boolean = false,
    val isSubmitted: Boolean = false
)

@HiltViewModel
class AppRequestViewModel @Inject constructor(
    private val webSearchHelper: WebSearchHelper
) : ViewModel() {

    private val VERCEL_API_URL = "https://aurora-whitelist-chi.vercel.app/api/request-app"

    private val _items = MutableStateFlow<List<AppRequestItem>>(emptyList())
    val items = _items.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting = _isSubmitting.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()

    fun search(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            _items.value = emptyList()
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                // חיפוש גולמי ללא סינון ה-Whitelist
                val cluster = webSearchHelper.search(trimmed, filterWhitelist = false)
                val mapped = cluster.clusterAppList.map { app ->
                    AppRequestItem(
                        app = app,
                        isAuthorized = WhitelistManager.isAuthorized(app.packageName),
                        isSelected = false,
                        isSubmitted = false
                    )
                }
                _items.value = mapped
            } catch (_: Exception) {
                _items.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleSelection(packageName: String) {
        _items.value = _items.value.map { item ->
            if (item.app.packageName == packageName && !item.isAuthorized && !item.isSubmitted) {
                item.copy(isSelected = !item.isSelected)
            } else {
                item
            }
        }
    }

    fun submitSelected() {
        val selectedItems = _items.value.filter { it.isSelected && !itemAlreadySubmitted(it) }
        if (selectedItems.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            _isSubmitting.value = true
            try {
                val jsonBody = JSONObject().apply {
                    val appsArray = JSONArray()
                    selectedItems.forEach { item ->
                        val appObj = JSONObject().apply {
                            put("packageName", item.app.packageName)
                            put("title", item.app.displayName)
                            put("iconUrl", item.app.iconArtwork.url)
                        }
                        appsArray.put(appObj)
                    }
                    put("apps", appsArray)
                }

                val url = URL(VERCEL_API_URL)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    setRequestProperty("Accept", "application/json")
                    doOutput = true
                    connectTimeout = 10000
                    readTimeout = 10000
                }

                conn.outputStream.use { os ->
                    os.write(jsonBody.toString().toByteArray(Charsets.UTF_8))
                }

                val responseCode = conn.responseCode
                if (responseCode in 200..299) {
                    val selectedPackages = selectedItems.map { it.app.packageName }.toSet()
                    _items.value = _items.value.map { item ->
                        if (selectedPackages.contains(item.app.packageName)) {
                            item.copy(isSelected = false, isSubmitted = true)
                        } else {
                            item
                        }
                    }
                    _toastEvent.emit("הבקשות נשלחו בהצלחה לבדיקת מנהל המערכת!")
                } else {
                    _toastEvent.emit("שגיאה בשליחת הבקשות. קוד: $responseCode")
                }
            } catch (e: Exception) {
                _toastEvent.emit("שגיאת רשת בשליחת הבקשות")
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    private fun itemAlreadySubmitted(item: AppRequestItem): Boolean =
        item.isAuthorized || item.isSubmitted
}
