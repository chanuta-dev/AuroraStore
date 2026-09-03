package com.aurora.store.viewmodel.homestream

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurora.extensions.TAG
import com.aurora.gplayapi.data.models.StreamBundle
import com.aurora.gplayapi.data.models.StreamCluster
import com.aurora.gplayapi.helpers.contracts.StreamContract
import com.aurora.gplayapi.helpers.web.WebAppDetailsHelper
import com.aurora.gplayapi.helpers.web.WebStreamHelper
import com.aurora.store.HomeStash
import com.aurora.store.data.model.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@HiltViewModel
class StreamViewModel @Inject constructor(
    private val webStreamHelper: WebStreamHelper,
    private val webAppDetailsHelper: WebAppDetailsHelper
) : ViewModel() {

    val liveData: MutableLiveData<ViewState> = MutableLiveData()
    private val stash: HomeStash = mutableMapOf()
    private val stashMutex = Mutex()

    fun getStreamBundle(category: StreamContract.Category, type: StreamContract.Type, forceRefresh: Boolean = false) {
        // 1. אם המידע כבר שמור בזיכרון ולא ביקשנו רענון בכוח - נציג אותו מיד ללא טעינה!
        if (!forceRefresh && stash[category]?.hasCluster() == true) {
            liveData.postValue(ViewState.Success(stash.toMap()))
            return
        }

        // 2. רק אם המידע באמת לא קיים עדיין, נציג שלדי טעינה
        liveData.postValue(ViewState.Loading)
        observe(category, type)
    }

    fun observe(category: StreamContract.Category, type: StreamContract.Type) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // בדיקה אם הרשימה הלבנה ריקה (למקרה שהאפליקציה נפתחה ללא אינטרנט)
                if (com.aurora.gplayapi.WhitelistManager.categorizedApps.isEmpty()) {
                    com.aurora.gplayapi.WhitelistManager.fetchRemoteWhitelist()
                    com.aurora.gplayapi.PatchedAppManager.fetchReleases()
                }

                if (com.aurora.gplayapi.WhitelistManager.categorizedApps.isEmpty()) {
                    liveData.postValue(ViewState.Error("אין חיבור לאינטרנט"))
                    return@launch
                }

                stashMutex.withLock {
                    val customClusters = com.aurora.gplayapi.WhitelistManager.categorizedApps.map { (categoryTitle, packageList) ->

                        // שליפה מרוכזת ומהירה של כל האפליקציות בקטגוריה
                        val richApps = try {
                            webAppDetailsHelper.getAppByPackageName(packageList)
                        } catch (e: Exception) {
                            packageList.map { pkg ->
                                com.aurora.gplayapi.data.models.App(
                                    id = pkg.hashCode(),
                                    packageName = pkg,
                                    displayName = pkg
                                )
                            }
                        }

                        StreamCluster(
                            id = categoryTitle.hashCode(),
                            clusterTitle = categoryTitle,
                            clusterSubtitle = "${richApps.size} יישומים",
                            clusterAppList = richApps
                        )
                    }.associateBy { it.id }

                    val customBundle = StreamBundle(
                        id = category.value.hashCode(),
                        streamClusters = customClusters
                    )

                    // שמירה ב-Cache
                    stash[category] = customBundle
                    liveData.postValue(ViewState.Success(stash.toMap()))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error building custom stream bundle", e)
                liveData.postValue(ViewState.Error(e.message))
            }
        }
    }

    fun observeCluster(category: StreamContract.Category, streamCluster: StreamCluster) {
        // אין צורך בפעולה הזו מכיוון שאין דפים נוספים לטעון
    }
}