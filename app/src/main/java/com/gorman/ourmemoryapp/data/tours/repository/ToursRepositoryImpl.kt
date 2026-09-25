package com.gorman.ourmemoryapp.data.tours.repository

import com.gorman.ourmemoryapp.data.settings.language.ContentLanguageProvider
import com.gorman.ourmemoryapp.data.tours.datasource.remote.ToursRemoteDataSource
import com.gorman.ourmemoryapp.domain.models.Tour
import com.gorman.ourmemoryapp.domain.models.localized
import com.gorman.ourmemoryapp.domain.repository.ToursRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class ToursRepositoryImpl @Inject constructor(
    private val dataSource: ToursRemoteDataSource,
    private val contentLanguage: ContentLanguageProvider
) : ToursRepository {

    private val mutex = Mutex()
    private var cachedTours: List<Tour>? = null

    override suspend fun getAllTours(): List<Tour> {
        val language = contentLanguage.current()
        return getOriginalTours().map { it.localized(language) }
    }

    override suspend fun getOriginalTours(): List<Tour> = mutex.withLock {
        cachedTours ?: dataSource.getAllTours().also { cachedTours = it }
    }

    override suspend fun invalidate() = mutex.withLock {
        cachedTours = null
    }
}
