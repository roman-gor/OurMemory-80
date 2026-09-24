package com.gorman.ourmemoryapp.ui.details.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gorman.ourmemoryapp.BuildConfig
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.data.repository.AudioRepository
import com.gorman.ourmemoryapp.di.annotation.IoDispatcher
import com.gorman.ourmemoryapp.domain.models.AudioItem
import com.gorman.ourmemoryapp.domain.models.AudioPlaybackState
import com.gorman.ourmemoryapp.domain.models.Burial
import com.gorman.ourmemoryapp.domain.models.CandleState
import com.gorman.ourmemoryapp.domain.models.Veteran
import com.gorman.ourmemoryapp.domain.repository.BurialsRepository
import com.gorman.ourmemoryapp.domain.repository.CandlesRepository
import com.gorman.ourmemoryapp.domain.repository.SettingsRepository
import com.gorman.ourmemoryapp.domain.repository.VeteransRepository
import com.gorman.ourmemoryapp.ui.common.models.AudioAction
import com.gorman.ourmemoryapp.ui.common.models.MediaUi
import com.gorman.ourmemoryapp.ui.common.models.toExternalModel
import com.gorman.ourmemoryapp.ui.details.models.DetailsUiEvent
import com.gorman.ourmemoryapp.ui.details.models.DetailsUiState
import com.gorman.ourmemoryapp.ui.details.models.parseRewards
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = DetailsViewModel.Factory::class)
class DetailsViewModel @AssistedInject constructor(
    @Assisted private val veteranId: String,
    private val veteranRepository: VeteransRepository,
    private val burialsRepository: BurialsRepository,
    private val audioRepository: AudioRepository,
    private val candlesRepository: CandlesRepository,
    private val settingsRepository: SettingsRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(veteranId: String): DetailsViewModel
    }

    val uiState = observeDetailsUiState().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = DetailsUiState.Loading
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val playbackState = audioRepository.playbackState
        .flatMapLatest { state ->
            if (state.isPlaying) {
                audioRepository.observePosition().map { state.copy(currentPosition = it) }
            } else {
                flowOf(state)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = AudioPlaybackState()
        )

    val candleState = candlesRepository.observeCandleState(veteranId)
        .catch { error ->
            Log.e(LOG_TAG, "Failed to observe candles for $veteranId", error)
            emit(CandleState())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = CandleState()
        )

    val shouldAskNotifications = settingsRepository.observeNotificationsAsked()
        .map { asked -> !asked }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = false
        )

    fun onUiEvent(event: DetailsUiEvent) {
        when (event) {
            is DetailsUiEvent.OnAudioAction -> onAudioAction(event.action)
            DetailsUiEvent.OnLightCandleClick -> lightCandle()
            DetailsUiEvent.OnNotificationsAsked -> viewModelScope.launch { settingsRepository.markNotificationsAsked() }
        }
    }

    private fun lightCandle() {
        viewModelScope.launch {
            runCatching { candlesRepository.lightCandle(veteranId) }
                .onFailure { Log.e(LOG_TAG, "Failed to light a candle for $veteranId", it) }
        }
    }

    override fun onCleared() {
        audioRepository.release()
    }

    private fun observeDetailsUiState(): Flow<DetailsUiState> = flow<DetailsUiState> {
        val (veteran, burials) = coroutineScope {
            val veterans = async { veteranRepository.getAllVeterans() }
            val burials = async { loadBurials() }
            veterans.await().first { it.id == veteranId } to burials.await()
        }
        val (links, paragraphs) = veteran.veteransInfo.partition { it.contains(LINK_MARKER) }
        emit(
            DetailsUiState.Success(
                veteran = veteran,
                rewards = parseRewards(veteran.rewards).toPersistentList(),
                paragraphs = (listOf(veteran.allInfo) + paragraphs)
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .toPersistentList(),
                media = resolveMedia(links).toPersistentList(),
                audio = audioFor(veteran),
                burial = burials.firstOrNull { it.id == veteran.burialId && veteran.burialId.isNotBlank() }
                    ?.toExternalModel()
            )
        )
    }.flowOn(
        ioDispatcher
    ).catch { error ->
        Log.e(LOG_TAG, "Failed to load veteran $veteranId", error)
        emit(DetailsUiState.Error)
    }

    private suspend fun loadBurials(): List<Burial> {
        return runCatching { burialsRepository.getAllBurials() }
            .onFailure { Log.e(LOG_TAG, "Failed to load burials", it) }
            .getOrDefault(emptyList())
    }

    private suspend fun resolveMedia(links: List<String>): List<MediaUi> = coroutineScope {
        links.map { link ->
            async {
                val url = link.substringBefore(DESCRIPTION_SEPARATOR).trim()
                val description = link.substringAfter(DESCRIPTION_SEPARATOR, missingDelimiterValue = "").trim()
                MediaUi(url = veteranRepository.resolveDirectUrl(url), description = description)
            }
        }.awaitAll()
    }

    private fun audioFor(veteran: Veteran): AudioItem? = when {
        veteran.audioUrl.isNotBlank() -> AudioItem(id = veteran.id, url = veteran.audioUrl, title = veteran.name)
        veteran.id == BUNDLED_BIOGRAPHY_VETERAN_ID -> AudioItem(
            id = veteran.id,
            url = "$ANDROID_RESOURCE_SCHEME${BuildConfig.APPLICATION_ID}/${R.raw.veteran_bio_10}",
            title = veteran.name
        )
        else -> null
    }

    private fun onAudioAction(action: AudioAction) {
        when (action) {
            AudioAction.Play -> playAudioForVeteran()
            AudioAction.Pause -> audioRepository.pauseAudio()
            AudioAction.Resume -> audioRepository.resumeAudio()
            AudioAction.Stop -> audioRepository.stopAudio()
            is AudioAction.SeekTo -> audioRepository.seekTo(action.position)
        }
    }

    private fun playAudioForVeteran() {
        (uiState.value as? DetailsUiState.Success)?.audio?.let(audioRepository::playAudio)
    }

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5000L
        private const val LOG_TAG = "DetailsViewModel"
        private const val LINK_MARKER = "http"
        private const val DESCRIPTION_SEPARATOR = "|"
        private const val BUNDLED_BIOGRAPHY_VETERAN_ID = "10"
        private const val ANDROID_RESOURCE_SCHEME = "android.resource://"
    }
}
