package com.gorman.ourmemoryapp.data.feedback.datasource.remote

import com.gorman.ourmemoryapp.data.feedback.model.FeedbackDto
import com.gorman.ourmemoryapp.domain.models.FeedbackDraft
import kotlinx.coroutines.flow.Flow

interface FeedbackRemoteDataSource {
    suspend fun send(draft: FeedbackDraft)
    fun observeFeedback(): Flow<List<FeedbackDto>>
    suspend fun markReviewed(feedbackId: String)
    suspend fun reply(feedbackId: String, text: String)
}
