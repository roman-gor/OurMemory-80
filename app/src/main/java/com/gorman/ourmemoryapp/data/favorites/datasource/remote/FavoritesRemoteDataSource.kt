package com.gorman.ourmemoryapp.data.favorites.datasource.remote

import kotlinx.coroutines.flow.Flow

interface FavoritesRemoteDataSource {
    fun observe(uid: String): Flow<Set<String>>
    suspend fun set(uid: String, veteranId: String, isFavorite: Boolean)
    suspend fun addAll(uid: String, veteranIds: Set<String>)
}
