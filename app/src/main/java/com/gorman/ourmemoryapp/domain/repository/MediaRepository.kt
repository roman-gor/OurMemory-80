package com.gorman.ourmemoryapp.domain.repository

interface MediaRepository {
    suspend fun uploadPhoto(uri: String, folder: String): String
    suspend fun uploadAudio(uri: String, folder: String): String
}
