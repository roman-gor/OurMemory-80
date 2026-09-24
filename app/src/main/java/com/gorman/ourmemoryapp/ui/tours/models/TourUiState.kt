package com.gorman.ourmemoryapp.ui.tours.models

import kotlinx.collections.immutable.ImmutableList

sealed interface TourUiState {
    data object Loading : TourUiState
    data class Success(
        val title: String,
        val description: String,
        val stops: ImmutableList<TourStopUi>,
        val selectedStopIndex: Int?
    ) : TourUiState
    data object Error : TourUiState
}
