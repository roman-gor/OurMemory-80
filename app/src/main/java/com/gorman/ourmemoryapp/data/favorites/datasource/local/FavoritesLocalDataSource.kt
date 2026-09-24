package com.gorman.ourmemoryapp.data.favorites.datasource.local

import kotlinx.coroutines.flow.Flow

interface FavoritesLocalDataSource {
    fun observe(): Flow<Set<String>>
    suspend fun set(veteranId: String, isFavorite: Boolean)
}
