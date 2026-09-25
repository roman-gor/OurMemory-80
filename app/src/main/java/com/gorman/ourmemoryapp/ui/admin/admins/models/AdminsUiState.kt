package com.gorman.ourmemoryapp.ui.admin.admins.models

import kotlinx.collections.immutable.ImmutableList

sealed interface AdminsUiState {
    data object Loading : AdminsUiState
    data object Error : AdminsUiState
    data class Success(
        val items: ImmutableList<AdminItemUi>,
        val addStatus: AddAdminStatus
    ) : AdminsUiState
}
