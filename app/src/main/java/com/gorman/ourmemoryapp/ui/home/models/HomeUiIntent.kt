package com.gorman.ourmemoryapp.ui.home.models

sealed interface HomeUiIntent {
    data class OnSearchChange(val text: String) : HomeUiIntent
    data class OnCheckedArtChange(val value: Boolean) : HomeUiIntent
    data class OnCheckedWarChange(val value: Boolean) : HomeUiIntent
}
