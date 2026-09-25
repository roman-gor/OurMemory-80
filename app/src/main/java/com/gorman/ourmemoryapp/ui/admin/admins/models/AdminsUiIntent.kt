package com.gorman.ourmemoryapp.ui.admin.admins.models

sealed interface AdminsUiIntent {
    data class OnAddClick(val email: String) : AdminsUiIntent
    data class OnRemoveConfirm(val uid: String) : AdminsUiIntent
    data object OnAddStatusShown : AdminsUiIntent
}
