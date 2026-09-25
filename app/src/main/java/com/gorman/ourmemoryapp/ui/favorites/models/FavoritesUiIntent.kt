package com.gorman.ourmemoryapp.ui.favorites.models

sealed interface FavoritesUiIntent {
    data class OnRemove(val veteranId: String) : FavoritesUiIntent
    data class OnUndoRemove(val veteranId: String) : FavoritesUiIntent
}
