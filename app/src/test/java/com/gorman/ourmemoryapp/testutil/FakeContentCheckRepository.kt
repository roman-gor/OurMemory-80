package com.gorman.ourmemoryapp.testutil

import com.gorman.ourmemoryapp.domain.models.PhotoCheckResult
import com.gorman.ourmemoryapp.domain.repository.ContentCheckRepository

class FakeContentCheckRepository(
    private val offensiveWords: Set<String> = emptySet(),
    private val photoResults: Map<String, PhotoCheckResult> = emptyMap()
) : ContentCheckRepository {

    override fun containsOffensiveText(text: String) = offensiveWords.any { it in text }

    override suspend fun checkPhoto(uri: String) = photoResults[uri] ?: PhotoCheckResult.ALLOWED
}
