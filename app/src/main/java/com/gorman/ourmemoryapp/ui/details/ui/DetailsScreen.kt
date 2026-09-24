package com.gorman.ourmemoryapp.ui.details.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.domain.models.AudioPlaybackState
import com.gorman.ourmemoryapp.ui.details.models.AudioAction
import com.gorman.ourmemoryapp.ui.details.models.DetailsUiEvent
import com.gorman.ourmemoryapp.ui.details.models.DetailsUiState
import com.gorman.ourmemoryapp.ui.details.viewmodels.DetailsViewModel

@Composable
fun DetailsScreen(
    detailsViewModel: DetailsViewModel,
    onBackClick: () -> Unit
) {
    val uiState by detailsViewModel.uiState.collectAsStateWithLifecycle()
    val playbackState by detailsViewModel.playbackState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val isHeaderScrolledAway by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    val title = (uiState as? DetailsUiState.Success)?.veteran?.name.orEmpty()

    Scaffold(
        topBar = {
            DetailsTopBar(
                title = if (isHeaderScrolledAway) title else "",
                onBackClick = onBackClick
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        when (val state = uiState) {
            DetailsUiState.Loading -> LoadingContent(modifier = Modifier.padding(padding))
            DetailsUiState.Error -> ErrorContent(modifier = Modifier.padding(padding))
            is DetailsUiState.Success -> DetailsContent(
                state = state,
                playbackState = playbackState,
                listState = listState,
                contentPadding = padding,
                onAudioAction = { detailsViewModel.onUiEvent(DetailsUiEvent.OnAudioAction(it)) }
            )
        }
    }
}

@Composable
private fun DetailsContent(
    state: DetailsUiState.Success,
    playbackState: AudioPlaybackState,
    listState: LazyListState,
    contentPadding: PaddingValues,
    onAudioAction: (AudioAction) -> Unit
) {
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding() + 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(28.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item { DetailsHeader(veteran = state.veteran) }
        if (state.rewards.isNotEmpty()) {
            item { RewardsRow(rewards = state.rewards) }
        }
        if (state.audio != null) {
            item { AudioPlayerCard(playbackState = playbackState, onAudioAction = onAudioAction) }
        }
        if (state.paragraphs.isNotEmpty()) {
            item { BiographySection(paragraphs = state.paragraphs) }
        }
        if (state.media.isNotEmpty()) {
            item { MediaGallery(media = state.media) }
        }
        state.burial?.let { burial ->
            item { BurialSection(burial = burial) }
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.failed_to_load_data_msg),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(32.dp)
        )
    }
}
