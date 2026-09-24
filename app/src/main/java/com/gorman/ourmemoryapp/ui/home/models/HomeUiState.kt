package com.gorman.ourmemoryapp.ui.home.models

import com.gorman.ourmemoryapp.domain.models.Veteran
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val veterans: ImmutableList<Veteran> = persistentListOf(),
        val search: String = "",
        val checkedWar: Boolean = false,
        val checkedArt: Boolean = false,
        val anniversaries: ImmutableList<AnniversaryUi> = persistentListOf()
    ) : HomeUiState
    data class Error(val throwable: Throwable) : HomeUiState
}
