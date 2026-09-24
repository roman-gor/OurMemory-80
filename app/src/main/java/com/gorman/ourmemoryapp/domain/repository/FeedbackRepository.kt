package com.gorman.ourmemoryapp.domain.repository

import com.gorman.ourmemoryapp.domain.models.Feedback
import com.gorman.ourmemoryapp.domain.models.FeedbackDraft
import kotlinx.coroutines.flow.Flow

interface FeedbackRepository {
    suspend fun send(draft: FeedbackDraft)
    fun observeFeedback(): Flow<List<Feedback>>
    suspend fun markReviewed(feedbackId: String)
}
