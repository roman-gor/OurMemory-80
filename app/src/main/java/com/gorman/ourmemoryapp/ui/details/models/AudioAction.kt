package com.gorman.ourmemoryapp.ui.details.models

sealed interface AudioAction {
    object Play : AudioAction
    object Pause : AudioAction
    object Resume : AudioAction
    object Stop : AudioAction
    data class SeekTo(val position: Int) : AudioAction
}
