package com.gorman.ourmemoryapp.ui.admin.burials.models

import kotlinx.collections.immutable.ImmutableList

sealed interface AdminBurialsUiState {
    data object Loading : AdminBurialsUiState
    data object Error : AdminBurialsUiState
    data class Success(val items: ImmutableList<AdminBurialItemUi>) : AdminBurialsUiState
}
