package com.gorman.ourmemoryapp.testutil

import com.gorman.ourmemoryapp.domain.models.Burial
import com.gorman.ourmemoryapp.domain.repository.BurialsRepository

class FakeBurialsRepository(
    private val burials: List<Burial> = emptyList(),
    private val error: Throwable? = null
) : BurialsRepository {

    var invalidations = 0

    override suspend fun getAllBurials(): List<Burial> {
        error?.let { throw it }
        return burials
    }

    override suspend fun invalidate() {
        invalidations++
    }
}
