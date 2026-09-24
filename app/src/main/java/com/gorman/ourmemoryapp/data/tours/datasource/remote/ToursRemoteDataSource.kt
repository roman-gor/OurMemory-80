package com.gorman.ourmemoryapp.data.tours.datasource.remote

import com.gorman.ourmemoryapp.domain.models.Tour

interface ToursRemoteDataSource {
    suspend fun getAllTours(): List<Tour>
}
