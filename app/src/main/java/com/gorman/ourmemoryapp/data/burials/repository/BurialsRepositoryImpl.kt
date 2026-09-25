package com.gorman.ourmemoryapp.data.burials.repository

import com.gorman.ourmemoryapp.data.burials.datasource.remote.BurialsRemoteDataSource
import com.gorman.ourmemoryapp.data.settings.language.ContentLanguageProvider
import com.gorman.ourmemoryapp.domain.models.Burial
import com.gorman.ourmemoryapp.domain.models.localized
import com.gorman.ourmemoryapp.domain.repository.BurialsRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class BurialsRepositoryImpl @Inject constructor(
    private val dataSource: BurialsRemoteDataSource,
    private val contentLanguage: ContentLanguageProvider
) : BurialsRepository {

    private val mutex = Mutex()
    private var cachedBurials: List<Burial>? = null

    override suspend fun getAllBurials(): List<Burial> {
        val language = contentLanguage.current()
        return getOriginalBurials().map { it.localized(language) }
    }

    override suspend fun getOriginalBurials(): List<Burial> = mutex.withLock {
        cachedBurials ?: dataSource.getAllBurials().also { cachedBurials = it }
    }

    override suspend fun invalidate() = mutex.withLock {
        cachedBurials = null
    }
}
