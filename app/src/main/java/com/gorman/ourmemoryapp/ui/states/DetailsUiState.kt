package com.gorman.ourmemoryapp.ui.states

import com.gorman.ourmemoryapp.domain.models.Veteran
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

sealed interface DetailsUiState {
    data object Loading : DetailsUiState
    data class Success(
        val veteran: Veteran = Veteran(),
        val rewards: ImmutableList<Int> = persistentListOf(),
        val additionalInfo: ImmutableList<String> = persistentListOf(),
        val additionalRes: ImmutableMap<String, String> = persistentMapOf(),
        val directUrls: ImmutableMap<String, String> = persistentMapOf(),
        val additionalText: ImmutableList<String> = persistentListOf()
    ) : DetailsUiState
    data class Error(val throwable: Throwable) : DetailsUiState
}
