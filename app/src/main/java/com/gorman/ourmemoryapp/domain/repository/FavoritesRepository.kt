package com.gorman.ourmemoryapp.domain.repository

import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun observeFavorites(): Flow<Set<String>>
    suspend fun toggle(veteranId: String)
}
