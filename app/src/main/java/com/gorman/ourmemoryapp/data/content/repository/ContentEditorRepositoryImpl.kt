package com.gorman.ourmemoryapp.data.content.repository

import com.gorman.ourmemoryapp.data.content.datasource.remote.ContentRemoteDataSource
import com.gorman.ourmemoryapp.domain.models.Veteran
import com.gorman.ourmemoryapp.domain.repository.ContentEditorRepository
import com.gorman.ourmemoryapp.domain.repository.VeteransRepository
import javax.inject.Inject

class ContentEditorRepositoryImpl @Inject constructor(
    private val remoteDataSource: ContentRemoteDataSource,
    private val veteransRepository: VeteransRepository
) : ContentEditorRepository {

    override suspend fun saveVeteran(veteran: Veteran) {
        remoteDataSource.saveVeteran(veteran)
        veteransRepository.invalidate()
    }

    override suspend fun deleteVeteran(veteranId: String) {
        remoteDataSource.deleteVeteran(veteranId)
        veteransRepository.invalidate()
    }
}
