package com.gorman.ourmemoryapp.ui.admin.veterans.models

import kotlinx.collections.immutable.ImmutableList

sealed interface AdminVeteransUiState {
    data object Loading : AdminVeteransUiState
    data object Error : AdminVeteransUiState
    data class Success(
        val items: ImmutableList<AdminVeteranItemUi>,
        val search: String
    ) : AdminVeteransUiState
}
