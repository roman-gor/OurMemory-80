package com.gorman.ourmemoryapp.ui.map.models

sealed interface MapUiIntent {
    data class OnMarkerClick(val burialId: String) : MapUiIntent
    data object OnSheetDismiss : MapUiIntent
    data class OnCheckedWarChange(val value: Boolean) : MapUiIntent
    data class OnCheckedArtChange(val value: Boolean) : MapUiIntent
}
