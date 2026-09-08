package com.gorman.ourmemoryapp.data.repository

import com.gorman.ourmemoryapp.data.datasource.FirebaseDB
import com.gorman.ourmemoryapp.data.datasource.YandexApiService
import com.gorman.ourmemoryapp.data.mapper.toDomain
import com.gorman.ourmemoryapp.domain.models.Veteran
import com.gorman.ourmemoryapp.domain.models.YandexImage
import com.gorman.ourmemoryapp.domain.repository.VeteransRepository
import javax.inject.Inject

class VeteransRepositoryImpl @Inject constructor(
    private val firebaseDB: FirebaseDB,
    private val apiService: YandexApiService
) : VeteransRepository {
    override suspend fun getAllVeterans(): List<Veteran> {
        return firebaseDB.getAllVeterans()
    }

    override suspend fun getHrefFromLink(publicKey: String): YandexImage {
        return apiService.getHrefFromLink(publicKey = publicKey).toDomain()
    }
}
