package com.gorman.ourmemoryapp.ui.details.models

sealed interface DetailsUiEvent {
    data class OnAudioAction(val action: AudioAction) : DetailsUiEvent
}
