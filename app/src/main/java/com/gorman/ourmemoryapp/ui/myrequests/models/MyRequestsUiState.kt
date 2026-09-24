package com.gorman.ourmemoryapp.ui.myrequests.models

import kotlinx.collections.immutable.ImmutableList

sealed interface MyRequestsUiState {
    data object Loading : MyRequestsUiState
    data object Error : MyRequestsUiState
    data class Success(val items: ImmutableList<MyRequestItemUi>) : MyRequestsUiState
}
