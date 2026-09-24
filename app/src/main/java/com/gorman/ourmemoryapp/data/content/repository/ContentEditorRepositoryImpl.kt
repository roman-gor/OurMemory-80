package com.gorman.ourmemoryapp.data.content.repository

import com.gorman.ourmemoryapp.data.content.datasource.remote.ContentRemoteDataSource
import com.gorman.ourmemoryapp.domain.models.Burial
import com.gorman.ourmemoryapp.domain.models.Tour
import com.gorman.ourmemoryapp.domain.models.Veteran
import com.gorman.ourmemoryapp.domain.repository.BurialsRepository
import com.gorman.ourmemoryapp.domain.repository.ContentEditorRepository
import com.gorman.ourmemoryapp.domain.repository.ToursRepository
import com.gorman.ourmemoryapp.domain.repository.VeteransRepository
import javax.inject.Inject

class ContentEditorRepositoryImpl @Inject constructor(
    private val remoteDataSource: ContentRemoteDataSource,
    private val veteransRepository: VeteransRepository,
    private val burialsRepository: BurialsRepository,
    private val toursRepository: ToursRepository
) : ContentEditorRepository {

    override suspend fun saveVeteran(veteran: Veteran) {
        remoteDataSource.saveVeteran(veteran)
        veteransRepository.invalidate()
    }

    override suspend fun deleteVeteran(veteranId: String) {
        remoteDataSource.deleteVeteran(veteranId)
        veteransRepository.invalidate()
    }

    override fun newBurialId() = remoteDataSource.newBurialId()

    override suspend fun saveBurial(burial: Burial) {
        remoteDataSource.saveBurial(burial)
        burialsRepository.invalidate()
    }

    override fun newTourId() = remoteDataSource.newTourId()

    override suspend fun saveTour(tour: Tour) {
        remoteDataSource.saveTour(tour)
        toursRepository.invalidate()
    }

    override suspend fun deleteTour(tourId: String) {
        remoteDataSource.deleteTour(tourId)
        toursRepository.invalidate()
    }
}
