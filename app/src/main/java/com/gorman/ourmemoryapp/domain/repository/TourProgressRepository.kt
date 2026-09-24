package com.gorman.ourmemoryapp.domain.repository

import kotlinx.coroutines.flow.Flow

interface TourProgressRepository {
    fun observeProgress(): Flow<Map<String, Set<Int>>>
    suspend fun toggleStop(tourId: String, stopIndex: Int)
    suspend fun reset(tourId: String)
}
