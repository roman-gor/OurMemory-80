package com.gorman.ourmemoryapp.ui.admin.tours.models

import kotlinx.collections.immutable.ImmutableList

sealed interface AdminToursUiState {
    data object Loading : AdminToursUiState
    data object Error : AdminToursUiState
    data class Success(val items: ImmutableList<AdminTourItemUi>) : AdminToursUiState
}
