package com.gorman.ourmemoryapp.domain.repository

import com.gorman.ourmemoryapp.domain.models.PhotoCheckResult

interface ContentCheckRepository {
    fun containsOffensiveText(text: String): Boolean
    suspend fun checkPhoto(uri: String): PhotoCheckResult
}
