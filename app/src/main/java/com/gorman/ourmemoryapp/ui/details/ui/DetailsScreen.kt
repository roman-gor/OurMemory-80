package com.gorman.ourmemoryapp.ui.details.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.domain.models.AudioPlaybackState
import com.gorman.ourmemoryapp.domain.models.CandleState
import com.gorman.ourmemoryapp.ui.common.models.AudioAction
import com.gorman.ourmemoryapp.ui.common.ui.ErrorContent
import com.gorman.ourmemoryapp.ui.common.ui.ExpandableTextSection
import com.gorman.ourmemoryapp.ui.common.ui.FloatingTopBar
import com.gorman.ourmemoryapp.ui.common.ui.LoadingContent
import com.gorman.ourmemoryapp.ui.common.ui.MediaGallery
import com.gorman.ourmemoryapp.ui.common.ui.NotificationPermissionRequest
import com.gorman.ourmemoryapp.ui.common.ui.SystemBarIcons
import com.gorman.ourmemoryapp.ui.common.ui.rememberIsHeroScrolledAway
import com.gorman.ourmemoryapp.ui.details.models.DetailsUiEvent
import com.gorman.ourmemoryapp.ui.details.models.DetailsUiState
import com.gorman.ourmemoryapp.ui.details.viewmodels.DetailsViewModel

@Composable
fun DetailsScreen(
    detailsViewModel: DetailsViewModel,
    onBackClick: () -> Unit,
    onShowOnMapClick: (String) -> Unit
) {
    val uiState by detailsViewModel.uiState.collectAsStateWithLifecycle()
    val playbackState by detailsViewModel.playbackState.collectAsStateWithLifecycle()
    val candleState by detailsViewModel.candleState.collectAsStateWithLifecycle()
    val shouldAskNotifications by detailsViewModel.shouldAskNotifications.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val isHeroScrolledAway by rememberIsHeroScrolledAway(listState)
    val state = uiState
    val isCollapsed = state is DetailsUiState.Success && isHeroScrolledAway

    SystemBarIcons(darkIcons = state !is DetailsUiState.Success || isHeroScrolledAway)
    NotificationPermissionRequest(
        shouldAsk = shouldAskNotifications && state is DetailsUiState.Success,
        onAsked = { detailsViewModel.onUiEvent(DetailsUiEvent.OnNotificationsAsked) }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (state) {
            DetailsUiState.Loading -> LoadingContent(modifier = Modifier.statusBarsPadding())
            DetailsUiState.Error -> ErrorContent(modifier = Modifier.statusBarsPadding())
            is DetailsUiState.Success -> DetailsContent(
                state = state,
                playbackState = playbackState,
                candleState = candleState,
                listState = listState,
                onLightCandleClick = { detailsViewModel.onUiEvent(DetailsUiEvent.OnLightCandleClick) },
                onAudioAction = { detailsViewModel.onUiEvent(DetailsUiEvent.OnAudioAction(it)) },
                onShowOnMapClick = onShowOnMapClick
            )
        }
        FloatingTopBar(
            title = (state as? DetailsUiState.Success)?.veteran?.name.orEmpty(),
            isCollapsed = isCollapsed,
            onBackClick = onBackClick,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun DetailsContent(
    state: DetailsUiState.Success,
    playbackState: AudioPlaybackState,
    candleState: CandleState,
    listState: LazyListState,
    onLightCandleClick: () -> Unit,
    onAudioAction: (AudioAction) -> Unit,
    onShowOnMapClick: (String) -> Unit
) {
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item { DetailsHeader(veteran = state.veteran) }
        item { CandleCard(candleState = candleState, onLightClick = onLightCandleClick) }
        if (state.rewards.isNotEmpty()) {
            item { RewardsRow(rewards = state.rewards) }
        }
        if (state.audio != null) {
            item { AudioPlayerCard(playbackState = playbackState, onAudioAction = onAudioAction) }
        }
        if (state.paragraphs.isNotEmpty()) {
            item {
                ExpandableTextSection(
                    title = stringResource(R.string.biography),
                    paragraphs = state.paragraphs
                )
            }
        }
        if (state.media.isNotEmpty()) {
            item { MediaGallery(title = stringResource(R.string.docs), media = state.media) }
        }
        state.burial?.let { burial ->
            item { BurialSection(burial = burial, onShowOnMapClick = onShowOnMapClick) }
        }
        item { Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars)) }
    }
}
