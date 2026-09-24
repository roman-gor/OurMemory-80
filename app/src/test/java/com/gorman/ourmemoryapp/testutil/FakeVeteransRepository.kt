package com.gorman.ourmemoryapp.testutil

import com.gorman.ourmemoryapp.domain.models.Veteran
import com.gorman.ourmemoryapp.domain.models.YandexImage
import com.gorman.ourmemoryapp.domain.repository.VeteransRepository

class FakeVeteransRepository(
    private val veterans: List<Veteran> = emptyList(),
    private val error: Throwable? = null
) : VeteransRepository {

    var invalidations = 0

    override suspend fun getAllVeterans(): List<Veteran> {
        error?.let { throw it }
        return veterans
    }

    override suspend fun getHrefFromLink(publicKey: String) = YandexImage(href = publicKey)

    override suspend fun resolveDirectUrl(url: String) = url

    override suspend fun invalidate() {
        invalidations++
    }
}
