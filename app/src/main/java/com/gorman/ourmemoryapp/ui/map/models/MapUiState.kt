package com.gorman.ourmemoryapp.ui.map.models

import com.gorman.ourmemoryapp.ui.tours.models.TourSummaryUi
import kotlinx.collections.immutable.ImmutableList

sealed interface MapUiState {
    data object Loading : MapUiState
    data class Success(
        val markers: ImmutableList<BurialMarkerUi>,
        val selectedBurial: BurialDetailsUi?,
        val focusedBurialId: String?,
        val checkedWar: Boolean,
        val checkedArt: Boolean,
        val tours: ImmutableList<TourSummaryUi>
    ) : MapUiState
    data object Error : MapUiState
}
