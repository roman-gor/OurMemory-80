package com.gorman.ourmemoryapp.ui.tours.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.domain.models.AudioPlaybackState
import com.gorman.ourmemoryapp.ui.common.ui.CircleIconButton
import com.gorman.ourmemoryapp.ui.common.ui.ErrorContent
import com.gorman.ourmemoryapp.ui.common.ui.LoadingContent
import com.gorman.ourmemoryapp.ui.tours.models.TourUiIntent
import com.gorman.ourmemoryapp.ui.tours.models.TourUiState
import com.gorman.ourmemoryapp.ui.tours.viewmodels.TourViewModel

@Composable
fun TourScreen(
    onBackClick: () -> Unit,
    tourViewModel: TourViewModel = hiltViewModel()
) {
    val uiState by tourViewModel.uiState.collectAsStateWithLifecycle()
    val playbackState by tourViewModel.playbackState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val state = uiState) {
            TourUiState.Loading -> LoadingContent(modifier = Modifier.statusBarsPadding())
            TourUiState.Error -> ErrorContent(modifier = Modifier.statusBarsPadding())
            is TourUiState.Success -> TourContent(
                state = state,
                playbackState = playbackState,
                onUiIntent = tourViewModel::onUiIntent
            )
        }
        CircleIconButton(
            painter = painterResource(R.drawable.chevron_left),
            contentDescription = stringResource(R.string.back),
            onClick = onBackClick,
            modifier = Modifier
                .statusBarsPadding()
                .padding(12.dp)
        )
    }
}

@Composable
private fun TourContent(
    state: TourUiState.Success,
    playbackState: AudioPlaybackState,
    onUiIntent: (TourUiIntent) -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(state.selectedStopIndex) {
        state.selectedStopIndex?.let { listState.animateScrollToItem(it + HEADER_ITEMS) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TourMap(
            stops = state.stops,
            selectedStopIndex = state.selectedStopIndex,
            onStopClick = { onUiIntent(TourUiIntent.OnStopClick(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .weight(MAP_WEIGHT)
        )
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(LIST_WEIGHT)
        ) {
            item {
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    Text(
                        text = state.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (state.description.isNotBlank()) {
                        Text(
                            text = state.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    if (state.visitedStops.isNotEmpty()) {
                        TourProgressRow(
                            visitedCount = state.visitedStops.count { it in state.stops.indices },
                            totalCount = state.stops.size,
                            onResetClick = { onUiIntent(TourUiIntent.OnResetProgress) }
                        )
                    }
                }
            }
            itemsIndexed(state.stops, key = { _, stop -> stop.number }) { index, stop ->
                TourStopRow(
                    stop = stop,
                    isSelected = index == state.selectedStopIndex,
                    isPlaying = playbackState.isPlaying && playbackState.currentAudio?.id == stop.audio?.id,
                    onClick = { onUiIntent(TourUiIntent.OnStopClick(index)) },
                    isVisited = index in state.visitedStops,
                    onAudioClick = { onUiIntent(TourUiIntent.OnStopAudioClick(index)) },
                    onVisitedToggle = { onUiIntent(TourUiIntent.OnStopVisitedToggle(index)) }
                )
            }
            item { Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars)) }
        }
    }
}

@Composable
private fun TourProgressRow(visitedCount: Int, totalCount: Int, onResetClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
        Text(
            text = stringResource(R.string.visited_of_total, visitedCount, totalCount),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onResetClick) {
            Text(text = stringResource(R.string.start_over))
        }
    }
}

private const val HEADER_ITEMS = 1
private const val MAP_WEIGHT = 1f
private const val LIST_WEIGHT = 1f
