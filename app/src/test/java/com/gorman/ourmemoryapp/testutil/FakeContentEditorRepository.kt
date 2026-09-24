package com.gorman.ourmemoryapp.testutil

import com.gorman.ourmemoryapp.domain.models.Burial
import com.gorman.ourmemoryapp.domain.models.Tour
import com.gorman.ourmemoryapp.domain.models.Veteran
import com.gorman.ourmemoryapp.domain.repository.ContentEditorRepository

class FakeContentEditorRepository(
    private val error: Throwable? = null
) : ContentEditorRepository {

    val savedVeterans = mutableListOf<Veteran>()
    val deletedVeteranIds = mutableListOf<String>()
    val savedBurials = mutableListOf<Burial>()
    val savedTours = mutableListOf<Tour>()
    val deletedTourIds = mutableListOf<String>()

    override suspend fun saveVeteran(veteran: Veteran) {
        error?.let { throw it }
        savedVeterans += veteran
    }

    override suspend fun deleteVeteran(veteranId: String) {
        error?.let { throw it }
        deletedVeteranIds += veteranId
    }

    override fun newBurialId() = NEW_BURIAL_ID

    override suspend fun saveBurial(burial: Burial) {
        error?.let { throw it }
        savedBurials += burial
    }

    override fun newTourId() = NEW_TOUR_ID

    override suspend fun saveTour(tour: Tour) {
        error?.let { throw it }
        savedTours += tour
    }

    override suspend fun deleteTour(tourId: String) {
        error?.let { throw it }
        deletedTourIds += tourId
    }

    companion object {
        const val NEW_BURIAL_ID = "-NewBurial"
        const val NEW_TOUR_ID = "-NewTour"
    }
}
