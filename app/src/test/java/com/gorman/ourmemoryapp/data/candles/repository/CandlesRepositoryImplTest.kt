package com.gorman.ourmemoryapp.data.candles.repository

import com.gorman.ourmemoryapp.data.candles.datasource.local.CandlesLocalDataSource
import com.gorman.ourmemoryapp.data.candles.datasource.remote.CandlesRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

class CandlesRepositoryImplTest {

    private class FakeRemote : CandlesRemoteDataSource {
        val counts = MutableStateFlow(mapOf<String, Long>())

        override fun observeCandles(veteranId: String): Flow<Long> = counts.map { it[veteranId] ?: 0L }

        override suspend fun lightCandle(veteranId: String) {
            counts.value = counts.value + (veteranId to (counts.value[veteranId] ?: 0L) + 1)
        }
    }

    private class FakeLocal : CandlesLocalDataSource {
        val dates = MutableStateFlow(mapOf<String, String>())

        override fun observeLastLitDate(veteranId: String): Flow<String?> = dates.map { it[veteranId] }

        override suspend fun saveLastLitDate(veteranId: String, date: String) {
            dates.value = dates.value + (veteranId to date)
        }
    }

    private class MutableClock(var instant: Instant) : Clock() {
        override fun getZone(): ZoneId = ZoneOffset.UTC
        override fun withZone(zone: ZoneId?): Clock = this
        override fun instant(): Instant = instant
    }

    private val remote = FakeRemote()
    private val local = FakeLocal()
    private val clock = MutableClock(Instant.parse("2026-05-09T08:00:00Z"))
    private val repository = CandlesRepositoryImpl(remote, local, clock)

    @Test
    fun candleCanBeLitOnlyOncePerDay() = runTest {
        repository.lightCandle(VETERAN_ID)
        repository.lightCandle(VETERAN_ID)

        val state = repository.observeCandleState(VETERAN_ID).first()

        assertEquals(1L, state.count)
        assertTrue(state.isLitToday)
    }

    @Test
    fun candleCanBeLitAgainNextDay() = runTest {
        repository.lightCandle(VETERAN_ID)
        clock.instant = Instant.parse("2026-05-10T08:00:00Z")

        assertFalse(repository.observeCandleState(VETERAN_ID).first().isLitToday)

        repository.lightCandle(VETERAN_ID)

        assertEquals(2L, repository.observeCandleState(VETERAN_ID).first().count)
    }

    private companion object {
        const val VETERAN_ID = "10"
    }
}
