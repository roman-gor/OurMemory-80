package com.gorman.ourmemoryapp.data.content.datasource.remote

import com.gorman.ourmemoryapp.domain.models.Burial
import com.gorman.ourmemoryapp.domain.models.Tour
import com.gorman.ourmemoryapp.domain.models.Veteran

interface ContentRemoteDataSource {
    suspend fun saveVeteran(veteran: Veteran)
    suspend fun deleteVeteran(veteranId: String)
    fun newBurialId(): String
    suspend fun saveBurial(burial: Burial)
    fun newTourId(): String
    suspend fun saveTour(tour: Tour)
    suspend fun deleteTour(tourId: String)
}
