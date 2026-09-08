package com.gorman.ourmemoryapp.ui.states

sealed interface DetailsUiEvent {
    data class OnAudioAction(val action: AudioAction) : DetailsUiEvent
}

sealed interface AudioAction {
    object Play : AudioAction
    object Pause : AudioAction
    object Resume : AudioAction
    object Stop : AudioAction
    data class SeekTo(val position: Int) : AudioAction
}
