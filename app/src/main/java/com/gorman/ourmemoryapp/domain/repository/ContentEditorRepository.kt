package com.gorman.ourmemoryapp.domain.repository

import com.gorman.ourmemoryapp.domain.models.Burial
import com.gorman.ourmemoryapp.domain.models.Tour
import com.gorman.ourmemoryapp.domain.models.Veteran

interface ContentEditorRepository {
    suspend fun saveVeteran(veteran: Veteran)
    suspend fun deleteVeteran(veteranId: String)
    fun newBurialId(): String
    suspend fun saveBurial(burial: Burial)
    fun newTourId(): String
    suspend fun saveTour(tour: Tour)
    suspend fun deleteTour(tourId: String)
}
