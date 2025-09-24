package com.gorman.ourmemoryapp.domain.repository

import com.gorman.ourmemoryapp.domain.models.Veteran
import com.gorman.ourmemoryapp.domain.models.YandexImage

interface VeteransRepository {
    suspend fun getAllVeterans(): List<Veteran>
    suspend fun getHrefFromLink(publicKey: String): YandexImage
}