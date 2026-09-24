package com.gorman.ourmemoryapp.testutil

import com.gorman.ourmemoryapp.domain.repository.TourProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeTourProgressRepository(
    progress: Map<String, Set<Int>> = emptyMap()
) : TourProgressRepository {

    val progress = MutableStateFlow(progress)

    override fun observeProgress() = progress

    override suspend fun toggleStop(tourId: String, stopIndex: Int) {
        progress.update { current ->
            val visited = current[tourId].orEmpty()
            current + (tourId to if (stopIndex in visited) visited - stopIndex else visited + stopIndex)
        }
    }

    override suspend fun reset(tourId: String) {
        progress.update { it - tourId }
    }
}
