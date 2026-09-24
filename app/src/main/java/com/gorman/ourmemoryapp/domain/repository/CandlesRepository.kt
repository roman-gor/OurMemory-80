package com.gorman.ourmemoryapp.domain.repository

import com.gorman.ourmemoryapp.domain.models.CandleState
import kotlinx.coroutines.flow.Flow

interface CandlesRepository {
    fun observeCandleState(veteranId: String): Flow<CandleState>
    suspend fun lightCandle(veteranId: String)
}
