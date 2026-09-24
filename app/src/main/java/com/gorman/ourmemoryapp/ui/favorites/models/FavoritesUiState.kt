package com.gorman.ourmemoryapp.ui.favorites.models

import kotlinx.collections.immutable.ImmutableList

sealed interface FavoritesUiState {
    data object Loading : FavoritesUiState
    data object Error : FavoritesUiState
    data class Success(val items: ImmutableList<FavoriteVeteranUi>) : FavoritesUiState
}
