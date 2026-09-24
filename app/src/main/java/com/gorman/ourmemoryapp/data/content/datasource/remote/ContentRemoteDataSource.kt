package com.gorman.ourmemoryapp.data.content.datasource.remote

import com.gorman.ourmemoryapp.domain.models.Veteran

interface ContentRemoteDataSource {
    suspend fun saveVeteran(veteran: Veteran)
    suspend fun deleteVeteran(veteranId: String)
}
