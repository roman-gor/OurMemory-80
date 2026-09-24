package com.gorman.ourmemoryapp.ui.map.viewmodels

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gorman.ourmemoryapp.di.annotation.IoDispatcher
import com.gorman.ourmemoryapp.domain.models.Burial
import com.gorman.ourmemoryapp.domain.models.Screen
import com.gorman.ourmemoryapp.domain.models.Veteran
import com.gorman.ourmemoryapp.domain.repository.BurialsRepository
import com.gorman.ourmemoryapp.domain.repository.VeteransRepository
import com.gorman.ourmemoryapp.ui.common.models.toExternalModel
import com.gorman.ourmemoryapp.ui.map.models.BurialDetailsUi
import com.gorman.ourmemoryapp.ui.map.models.BurialMarkerUi
import com.gorman.ourmemoryapp.ui.map.models.BurialType
import com.gorman.ourmemoryapp.ui.map.models.MapUiIntent
import com.gorman.ourmemoryapp.ui.map.models.MapUiState
import com.gorman.ourmemoryapp.ui.map.models.toShortUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val burialsRepository: BurialsRepository,
    private val veteransRepository: VeteransRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val focusedBurialId: String? = savedStateHandle[Screen.BurialMapScreen.BURIAL_ID_ARG]
    private val selectedBurialId = MutableStateFlow(focusedBurialId)
    private val checkedWarState = MutableStateFlow(true)
    private val checkedArtState = MutableStateFlow(true)
    private val resolvedPhotos = mutableMapOf<String, String>()

    val uiState = observeMapUiState().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = MapUiState.Loading
    )

    fun onUiIntent(intent: MapUiIntent) {
        when (intent) {
            is MapUiIntent.OnMarkerClick -> selectedBurialId.value = intent.burialId
            MapUiIntent.OnSheetDismiss -> selectedBurialId.value = null
            is MapUiIntent.OnCheckedWarChange -> checkedWarState.value = intent.value
            is MapUiIntent.OnCheckedArtChange -> checkedArtState.value = intent.value
        }
    }

    private fun observeMapUiState(): Flow<MapUiState> = combine(
        flow { emit(loadCemetery()) },
        checkedWarState,
        checkedArtState,
        selectedBurialId
    ) { cemetery, war, art, selectedId ->
        MapUiState.Success(
            markers = cemetery.visibleMarkers(war, art).toPersistentList(),
            selectedBurial = selectedId?.let { burialDetails(cemetery, it) },
            focusedBurialId = focusedBurialId,
            checkedWar = war,
            checkedArt = art
        ) as MapUiState
    }.flowOn(
        ioDispatcher
    ).catch { error ->
        Log.e(LOG_TAG, "Failed to load cemetery map", error)
        emit(MapUiState.Error)
    }

    private suspend fun loadCemetery() = coroutineScope {
        val burials = async { burialsRepository.getAllBurials() }
        val veterans = async { veteransRepository.getAllVeterans() }
        Cemetery(
            burials = burials.await().filter { it.hasCoordinates() },
            veteransByBurial = veterans.await().filter { it.burialId.isNotBlank() }.groupBy { it.burialId }
        )
    }

    private suspend fun burialDetails(cemetery: Cemetery, burialId: String): BurialDetailsUi? {
        val burial = cemetery.burials.firstOrNull { it.id == burialId } ?: return null
        return BurialDetailsUi(
            burial = burial.toExternalModel(),
            type = BurialType.fromValue(burial.type),
            photo = resolvedPhotos.getOrPut(burial.photo) { veteransRepository.resolveDirectUrl(burial.photo) },
            description = burial.description,
            veterans = cemetery.veteransByBurial[burialId].orEmpty().map { it.toShortUi() }.toPersistentList()
        )
    }

    private fun Burial.hasCoordinates() = latitude != 0.0 || longitude != 0.0

    private data class Cemetery(
        val burials: List<Burial>,
        val veteransByBurial: Map<String, List<Veteran>>
    ) {
        fun visibleMarkers(war: Boolean, art: Boolean): List<BurialMarkerUi> {
            val categories = buildSet {
                if (war) add(WAR_CATEGORY)
                if (art) add(ART_CATEGORY)
            }
            return burials
                .filter { burial ->
                    val veterans = veteransByBurial[burial.id].orEmpty()
                    veterans.isEmpty() || veterans.any { it.category in categories }
                }
                .map { BurialMarkerUi(id = it.id, latitude = it.latitude, longitude = it.longitude) }
        }
    }

    companion object {
        private const val STOP_TIMEOUT_MILLIS = 5000L
        private const val LOG_TAG = "MapViewModel"
        private const val WAR_CATEGORY = "War"
        private const val ART_CATEGORY = "Art"
    }
}
