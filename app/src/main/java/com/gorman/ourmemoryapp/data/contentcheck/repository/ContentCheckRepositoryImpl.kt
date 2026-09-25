package com.gorman.ourmemoryapp.data.contentcheck.repository

import androidx.core.net.toUri
import com.gorman.ourmemoryapp.data.contentcheck.datasource.local.NsfwImageClassifier
import com.gorman.ourmemoryapp.data.contentcheck.datasource.local.ProfanityDetector
import com.gorman.ourmemoryapp.data.contentcheck.model.NsfwScores
import com.gorman.ourmemoryapp.domain.models.PhotoCheckResult
import com.gorman.ourmemoryapp.domain.repository.ContentCheckRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class ContentCheckRepositoryImpl(
    private val profanityDetector: ProfanityDetector,
    private val imageClassifier: NsfwImageClassifier,
    private val ioDispatcher: CoroutineDispatcher
) : ContentCheckRepository {

    override fun containsProfanity(text: String) = profanityDetector.containsProfanity(text)

    override suspend fun checkPhoto(uri: String) = withContext(ioDispatcher) {
        runCatching { imageClassifier.classify(uri.toUri()) }
            .map { scores -> if (scores.isExplicit()) PhotoCheckResult.BLOCKED else PhotoCheckResult.ALLOWED }
            .getOrDefault(PhotoCheckResult.UNREADABLE)
    }

    private fun NsfwScores.isExplicit() =
        porn >= EXPLICIT_THRESHOLD || hentai >= EXPLICIT_THRESHOLD || porn + hentai + sexy >= COMBINED_THRESHOLD

    companion object {
        private const val EXPLICIT_THRESHOLD = 0.6f
        private const val COMBINED_THRESHOLD = 0.8f
    }
}
