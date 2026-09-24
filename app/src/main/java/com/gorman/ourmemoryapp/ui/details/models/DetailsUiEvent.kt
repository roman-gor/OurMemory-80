package com.gorman.ourmemoryapp.ui.details.models

import com.gorman.ourmemoryapp.ui.common.models.AudioAction

sealed interface DetailsUiEvent {
    data class OnAudioAction(val action: AudioAction) : DetailsUiEvent
}
