package com.gorman.ourmemoryapp.data.repository

import com.gorman.ourmemoryapp.data.datasource.FirebaseDB
import com.gorman.ourmemoryapp.data.datasource.YandexApiService
import com.gorman.ourmemoryapp.data.mapper.toDomain
import com.gorman.ourmemoryapp.data.settings.language.ContentLanguageProvider
import com.gorman.ourmemoryapp.domain.models.Veteran
import com.gorman.ourmemoryapp.domain.models.YandexImage
import com.gorman.ourmemoryapp.domain.models.localized
import com.gorman.ourmemoryapp.domain.repository.VeteransRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class VeteransRepositoryImpl @Inject constructor(
    private val firebaseDB: FirebaseDB,
    private val apiService: YandexApiService,
    private val contentLanguage: ContentLanguageProvider
) : VeteransRepository {

    private val mutex = Mutex()
    private var cachedVeterans: List<Veteran>? = null

    override suspend fun getAllVeterans(): List<Veteran> {
        val language = contentLanguage.current()
        return getOriginalVeterans().map { it.localized(language) }
    }

    override suspend fun getOriginalVeterans(): List<Veteran> = mutex.withLock {
        cachedVeterans ?: firebaseDB.getAllVeterans().also { cachedVeterans = it }
    }

    override suspend fun invalidate() = mutex.withLock {
        cachedVeterans = null
    }

    override suspend fun getHrefFromLink(publicKey: String): YandexImage {
        return apiService.getHrefFromLink(publicKey = publicKey).toDomain()
    }

    override suspend fun resolveDirectUrl(url: String): String {
        if (!url.contains(YANDEX_MARKER)) return url
        return runCatching { getHrefFromLink(publicKey = url).href }
            .getOrNull()
            ?: url
    }

    companion object {
        private const val YANDEX_MARKER = "yandex"
    }
}
