package com.gorman.ourmemoryapp.testutil

import com.gorman.ourmemoryapp.domain.models.Tour
import com.gorman.ourmemoryapp.domain.repository.ToursRepository

class FakeToursRepository(
    private val tours: List<Tour> = emptyList(),
    private val error: Throwable? = null
) : ToursRepository {

    override suspend fun getAllTours(): List<Tour> {
        error?.let { throw it }
        return tours
    }
}
