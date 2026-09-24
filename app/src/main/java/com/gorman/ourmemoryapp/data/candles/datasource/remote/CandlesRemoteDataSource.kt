package com.gorman.ourmemoryapp.data.candles.datasource.remote

import kotlinx.coroutines.flow.Flow

interface CandlesRemoteDataSource {
    fun observeCandles(veteranId: String): Flow<Long>
    suspend fun lightCandle(veteranId: String)
}
