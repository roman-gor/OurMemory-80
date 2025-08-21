package com.gorman.ourmemoryapp.data

import javax.inject.Inject

class VeteransRepository @Inject constructor(
    private val firebaseDB: FirebaseDB,
    private val apiService: YandexApiService
) {
    suspend fun getAllVeterans(): List<Veteran> {
        return firebaseDB.getAllVeterans()
    }

    suspend fun getHrefFromLink(publicKey: String): YandexImageResponse{
        return apiService.getHrefFromLink(publicKey = publicKey)
    }
}