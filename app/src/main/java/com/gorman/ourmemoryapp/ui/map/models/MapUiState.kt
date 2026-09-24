package com.gorman.ourmemoryapp.ui.map.models

import kotlinx.collections.immutable.ImmutableList

sealed interface MapUiState {
    data object Loading : MapUiState
    data class Success(
        val markers: ImmutableList<BurialMarkerUi>,
        val selectedBurial: BurialDetailsUi?,
        val focusedBurialId: String?,
        val checkedWar: Boolean,
        val checkedArt: Boolean
    ) : MapUiState
    data object Error : MapUiState
}
