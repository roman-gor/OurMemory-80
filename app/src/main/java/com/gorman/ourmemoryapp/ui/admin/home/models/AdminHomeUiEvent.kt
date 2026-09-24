package com.gorman.ourmemoryapp.ui.admin.home.models

sealed interface AdminHomeUiEvent {
    data object OnSignOutClick : AdminHomeUiEvent
}
