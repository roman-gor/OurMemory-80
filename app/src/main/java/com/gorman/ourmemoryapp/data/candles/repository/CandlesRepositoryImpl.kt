package com.gorman.ourmemoryapp.data.candles.repository

import com.gorman.ourmemoryapp.data.candles.datasource.local.CandlesLocalDataSource
import com.gorman.ourmemoryapp.data.candles.datasource.remote.CandlesRemoteDataSource
import com.gorman.ourmemoryapp.domain.models.CandleState
import com.gorman.ourmemoryapp.domain.repository.CandlesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

class CandlesRepositoryImpl @Inject constructor(
    private val remoteDataSource: CandlesRemoteDataSource,
    private val localDataSource: CandlesLocalDataSource,
    private val clock: Clock
) : CandlesRepository {

    override fun observeCandleState(veteranId: String): Flow<CandleState> = combine(
        remoteDataSource.observeCandles(veteranId),
        localDataSource.observeLastLitDate(veteranId)
    ) { count, lastLitDate ->
        CandleState(count = count, isLitToday = lastLitDate == today())
    }

    override suspend fun lightCandle(veteranId: String) {
        if (localDataSource.observeLastLitDate(veteranId).first() == today()) return
        remoteDataSource.lightCandle(veteranId)
        localDataSource.saveLastLitDate(veteranId, today())
    }

    private fun today() = LocalDate.now(clock).toString()
}
