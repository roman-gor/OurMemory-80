package com.gorman.ourmemoryapp.ui.tours.viewmodels

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gorman.ourmemoryapp.data.repository.AudioRepository
import com.gorman.ourmemoryapp.di.annotation.IoDispatcher
import com.gorman.ourmemoryapp.domain.models.AudioItem
import com.gorman.ourmemoryapp.domain.models.AudioPlaybackState
import com.gorman.ourmemoryapp.domain.models.Screen
import com.gorman.ourmemoryapp.domain.repository.BurialsRepository
import com.gorman.ourmemoryapp.domain.repository.TourProgressRepository
import com.gorman.ourmemoryapp.domain.repository.ToursRepository
import com.gorman.ourmemoryapp.domain.repository.VeteransRepository
import com.gorman.ourmemoryapp.ui.common.models.BurialType
import com.gorman.ourmemoryapp.ui.common.models.toExternalModel
import com.gorman.ourmemoryapp.ui.tours.models.TourStopUi
import com.gorman.ourmemoryapp.ui.tours.models.TourUiIntent
import com.gorman.ourmemoryapp.ui.tours.models.TourUiState
import com.gorman.ourmemoryapp.ui.tours.models.resumeStopIndex
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TourViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val toursRepository: ToursRepository,
    private val burialsRepository: BurialsRepository,
    private val veteransRepository: VeteransRepository,
    private val audioRepository: AudioRepository,
    private val tourProgressRepository: TourProgressRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val tourId: String = savedStateHandle[Screen.TourScreen.TOUR_ID_ARG] ?: ""
    private val selectedStopIndex = MutableStateFlow<Int?>(null)

    val uiState = observeTourUiState().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = TourUiState.Loading
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

    fun onUiIntent(intent: TourUiIntent) {
        when (intent) {
            is TourUiIntent.OnStopClick -> selectedStopIndex.value = intent.index
            is TourUiIntent.OnStopAudioClick -> onStopAudioClick(intent.index)
            is TourUiIntent.OnStopVisitedToggle -> viewModelScope.launch {
                tourProgressRepository.toggleStop(tourId, intent.index)
            }
            TourUiIntent.OnResetProgress -> viewModelScope.launch { tourProgressRepository.reset(tourId) }
        }
    }

    override fun onCleared() {
        audioRepository.release()
    }

    private fun observeTourUiState(): Flow<TourUiState> = combine(
        flow { emit(loadTour()) }.flowOn(ioDispatcher),
        selectedStopIndex,
        tourProgressRepository.observeProgress().map { it[tourId].orEmpty() }
    ) { tour, selectedIndex, visited ->
        TourUiState.Success(
            title = tour.title,
            description = tour.description,
            stops = tour.stops,
            selectedStopIndex = selectedIndex ?: resumeStopIndex(tour.stops.size, visited),
            visitedStops = visited.toPersistentSet()
        ) as TourUiState
    }.catch { error ->
        Log.e(LOG_TAG, "Failed to load tour $tourId", error)
        emit(TourUiState.Error)
    }

    private suspend fun loadTour(): LoadedTour = coroutineScope {
        val tours = async { toursRepository.getAllTours() }
        val burials = async { burialsRepository.getAllBurials() }
        val veterans = async { veteransRepository.getAllVeterans() }
        val tour = tours.await().first { it.id == tourId }
        val burialsById = burials.await().associateBy { it.id }
        val veteransByBurial = veterans.await().filter { it.burialId.isNotBlank() }.groupBy { it.burialId }
        val stops = tour.stops
            .mapNotNull { stop -> burialsById[stop.burialId]?.let { burial -> stop to burial } }
            .filter { (_, burial) -> burial.latitude != 0.0 || burial.longitude != 0.0 }
            .mapIndexed { index, (stop, burial) ->
                val title = veteransByBurial[burial.id].orEmpty().joinToString(NAMES_SEPARATOR) { it.name }
                TourStopUi(
                    number = index + 1,
                    title = title,
                    type = BurialType.fromValue(burial.type),
                    burial = burial.toExternalModel(),
                    text = stop.text,
                    audio = stop.audioUrl
                        .takeIf { it.isNotBlank() }
                        ?.let { url ->
                            AudioItem(
                                id = "${tourId}_$index",
                                url = url,
                                title = title.ifBlank { tour.title },
                                subtitle = tour.title
                            )
                        }
                )
            }
        LoadedTour(title = tour.title, description = tour.description, stops = stops.toPersistentList())
    }

    private fun onStopAudioClick(index: Int) {
        val stop = (uiState.value as? TourUiState.Success)?.stops?.getOrNull(index) ?: return
        val audio = stop.audio ?: return
        val playback = audioRepository.playbackState.value
        when {
            playback.currentAudio?.id != audio.id -> audioRepository.playAudio(audio)
            playback.isPlaying -> audioRepository.pauseAudio()
            else -> audioRepository.resumeAudio()
        }
    }

    private data class LoadedTour(
        val title: String,
        val description: String,
        val stops: ImmutableList<TourStopUi>
    )

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5000L
        private const val LOG_TAG = "TourViewModel"
        private const val NAMES_SEPARATOR = ", "
    }
}
