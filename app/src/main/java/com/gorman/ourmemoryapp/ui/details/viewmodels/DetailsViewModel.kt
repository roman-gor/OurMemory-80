package com.gorman.ourmemoryapp.ui.details.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.data.repository.AudioRepository
import com.gorman.ourmemoryapp.di.annotation.IoDispatcher
import com.gorman.ourmemoryapp.domain.models.AudioItem
import com.gorman.ourmemoryapp.domain.models.AudioPlaybackState
import com.gorman.ourmemoryapp.domain.models.Burial
import com.gorman.ourmemoryapp.domain.repository.BurialsRepository
import com.gorman.ourmemoryapp.domain.repository.VeteransRepository
import com.gorman.ourmemoryapp.ui.common.models.MediaUi
import com.gorman.ourmemoryapp.ui.common.models.toExternalModel
import com.gorman.ourmemoryapp.ui.details.models.AudioAction
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

@HiltViewModel(assistedFactory = DetailsViewModel.Factory::class)
class DetailsViewModel @AssistedInject constructor(
    @Assisted private val veteranId: String,
    private val veteranRepository: VeteransRepository,
    private val burialsRepository: BurialsRepository,
    private val audioRepository: AudioRepository,
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

    fun onUiEvent(event: DetailsUiEvent) {
        when (event) {
            is DetailsUiEvent.OnAudioAction -> onAudioAction(event.action)
        }
    }

    override fun onCleared() {
        audioRepository.stopAudio()
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
                audio = loadAudioForVeteran(),
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

    private fun loadAudioForVeteran() = AudioItem(
        id = BIOGRAPHY_AUDIO_ID,
        fileName = BIOGRAPHY_AUDIO_FILE_NAME,
        rawResourceId = R.raw.veteran_bio_10,
        itemId = BIOGRAPHY_AUDIO_ID
    )

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
        private const val BIOGRAPHY_AUDIO_ID = 10
        private const val BIOGRAPHY_AUDIO_FILE_NAME = "veteran_bio_10.mp3"
    }
}
