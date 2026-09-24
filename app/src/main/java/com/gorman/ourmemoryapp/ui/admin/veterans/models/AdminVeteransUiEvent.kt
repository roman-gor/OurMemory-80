package com.gorman.ourmemoryapp.ui.admin.veterans.models

sealed interface AdminVeteransUiEvent {
    data class OnSearchChange(val search: String) : AdminVeteransUiEvent
    data object OnScreenResumed : AdminVeteransUiEvent
}
