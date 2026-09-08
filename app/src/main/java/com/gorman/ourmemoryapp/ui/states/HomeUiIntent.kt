package com.gorman.ourmemoryapp.ui.states

sealed interface HomeUiIntent {
    data class OnSearchChange(val text: String) : HomeUiIntent
    data class OnCheckedArtChange(val value: Boolean) : HomeUiIntent
    data class OnCheckedWarChange(val value: Boolean) : HomeUiIntent
}
