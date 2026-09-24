package com.gorman.ourmemoryapp.data.burials.datasource.remote

import com.gorman.ourmemoryapp.domain.models.Burial

interface BurialsRemoteDataSource {
    suspend fun getAllBurials(): List<Burial>
}
