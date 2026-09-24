package com.gorman.ourmemoryapp.data.candles.datasource.local

import kotlinx.coroutines.flow.Flow

interface CandlesLocalDataSource {
    fun observeLastLitDate(veteranId: String): Flow<String?>
    suspend fun saveLastLitDate(veteranId: String, date: String)
}
