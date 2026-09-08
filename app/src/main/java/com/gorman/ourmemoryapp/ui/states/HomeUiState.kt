package com.gorman.ourmemoryapp.ui.states

import com.gorman.ourmemoryapp.domain.models.Veteran
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val veterans: ImmutableList<Veteran> = persistentListOf(),
        val search: String = "",
        val checkedWar: Boolean = false,
        val checkedArt: Boolean = false
    ) : HomeUiState
    data class Error(val throwable: Throwable) : HomeUiState
}
