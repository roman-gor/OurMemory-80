package com.gorman.ourmemoryapp.ui.tours.models

sealed interface TourUiIntent {
    data class OnStopClick(val index: Int) : TourUiIntent
    data class OnStopAudioClick(val index: Int) : TourUiIntent
}
