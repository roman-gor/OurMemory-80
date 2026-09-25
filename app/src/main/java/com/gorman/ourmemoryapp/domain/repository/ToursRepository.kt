package com.gorman.ourmemoryapp.domain.repository

import com.gorman.ourmemoryapp.domain.models.Tour

interface ToursRepository {
    suspend fun getAllTours(): List<Tour>
    suspend fun getOriginalTours(): List<Tour>
    suspend fun invalidate()
}
