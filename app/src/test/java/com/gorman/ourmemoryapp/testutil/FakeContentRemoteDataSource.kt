package com.gorman.ourmemoryapp.testutil

import com.gorman.ourmemoryapp.data.content.datasource.remote.ContentRemoteDataSource
import com.gorman.ourmemoryapp.domain.models.Burial
import com.gorman.ourmemoryapp.domain.models.Tour
import com.gorman.ourmemoryapp.domain.models.Veteran

class FakeContentRemoteDataSource : ContentRemoteDataSource {

    val writes = mutableListOf<Any>()

    override suspend fun saveVeteran(veteran: Veteran) {
        writes += veteran
    }

    override suspend fun deleteVeteran(veteranId: String) {
        writes += veteranId
    }

    override fun newBurialId() = "-Burial"

    override suspend fun saveBurial(burial: Burial) {
        writes += burial
    }

    override fun newTourId() = "-Tour"

    override suspend fun saveTour(tour: Tour) {
        writes += tour
    }

    override suspend fun deleteTour(tourId: String) {
        writes += tourId
    }
}
