package com.gorman.ourmemoryapp.data.burials.repository

import com.gorman.ourmemoryapp.data.burials.datasource.remote.BurialsRemoteDataSource
import com.gorman.ourmemoryapp.domain.models.Burial
import com.gorman.ourmemoryapp.domain.repository.BurialsRepository
import javax.inject.Inject

class BurialsRepositoryImpl @Inject constructor(
    private val dataSource: BurialsRemoteDataSource
) : BurialsRepository {
    override suspend fun getAllBurials(): List<Burial> {
        return dataSource.getAllBurials()
    }
}
