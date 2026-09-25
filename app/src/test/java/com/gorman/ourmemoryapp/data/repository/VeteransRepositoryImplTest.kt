package com.gorman.ourmemoryapp.data.repository

import com.gorman.ourmemoryapp.data.datasource.FirebaseDB
import com.gorman.ourmemoryapp.data.datasource.YandexApiService
import com.gorman.ourmemoryapp.data.models.YandexImageResponse
import com.gorman.ourmemoryapp.domain.models.Veteran
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class VeteransRepositoryImplTest {

    private class CountingFirebaseDB : FirebaseDB {
        var calls = 0

        override suspend fun getAllVeterans(): List<Veteran> {
            calls++
            return listOf(Veteran(id = "1"))
        }
    }

    private class FailingYandexApiService : YandexApiService {
        override suspend fun getHrefFromLink(publicKey: String): YandexImageResponse = error("offline")
    }

    @Test
    fun veteransAreLoadedOnceAndServedFromCache() = runTest {
        val firebaseDB = CountingFirebaseDB()
        val repository = VeteransRepositoryImpl(firebaseDB, FailingYandexApiService()) { null }

        repository.getAllVeterans()
        repository.getAllVeterans()

        assertEquals(1, firebaseDB.calls)
    }

    @Test
    fun yandexLinkFallsBackToOriginalUrlWhenResolvingFails() = runTest {
        val repository = VeteransRepositoryImpl(CountingFirebaseDB(), FailingYandexApiService()) { null }
        val link = "https://disk.yandex.ru/i/photo"

        assertEquals(link, repository.resolveDirectUrl(link))
    }
}
