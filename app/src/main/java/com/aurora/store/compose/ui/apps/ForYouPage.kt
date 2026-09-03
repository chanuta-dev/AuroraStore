package com.aurora.store.compose.ui.apps

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.aurora.gplayapi.data.models.App
import com.aurora.gplayapi.data.models.StreamCluster
import com.aurora.gplayapi.helpers.contracts.StreamContract
import com.aurora.store.HomeStash
import com.aurora.store.R
import com.aurora.store.compose.composable.Placeholder
import com.aurora.store.compose.composable.StreamCarousel
import com.aurora.store.compose.composition.LocalNetworkStatus
import com.aurora.store.data.model.NetworkStatus
import com.aurora.store.data.model.ViewState
import com.aurora.store.viewmodel.homestream.StreamViewModel

@Composable
internal fun ForYouContent(
    pageType: Int,
    viewModel: StreamViewModel,
    onAppClick: (App) -> Unit,
    onHeaderClick: (StreamCluster) -> Unit,
    onClusterScrolled: (StreamCluster) -> Unit,
    onScrolledToEnd: () -> Unit
) {
    val category = category(pageType)
    val state by viewModel.liveData.observeAsState()
    val networkStatus = LocalNetworkStatus.current

    LaunchedEffect(category) {
        viewModel.getStreamBundle(category, StreamContract.Type.HOME)
    }

    // רענון אוטומטי ברגע שהאינטרנט חוזר להיות זמין
    LaunchedEffect(networkStatus) {
        if (networkStatus == NetworkStatus.AVAILABLE && (state is ViewState.Error || com.aurora.gplayapi.WhitelistManager.categorizedApps.isEmpty())) {
            viewModel.getStreamBundle(category, StreamContract.Type.HOME)
        }
    }

    if (state is ViewState.Error) {
        Placeholder(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(R.drawable.ic_refresh),
            message = stringResource(R.string.error),
            actionLabel = stringResource(R.string.action_retry),
            onAction = { viewModel.getStreamBundle(category, StreamContract.Type.HOME) }
        )
        return
    }

    @Suppress("UNCHECKED_CAST")
    val streamBundle = (state as? ViewState.Success<*>)?.data as? HomeStash
    StreamCarousel(
        modifier = Modifier.fillMaxSize(),
        streamBundle = streamBundle?.get(category),
        onHeaderClick = onHeaderClick,
        onAppClick = onAppClick,
        onClusterScrolled = onClusterScrolled,
        onScrolledToEnd = onScrolledToEnd
    )
}