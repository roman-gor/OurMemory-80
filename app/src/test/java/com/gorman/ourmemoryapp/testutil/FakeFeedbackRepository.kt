package com.gorman.ourmemoryapp.testutil

import com.gorman.ourmemoryapp.domain.models.Feedback
import com.gorman.ourmemoryapp.domain.models.FeedbackDraft
import com.gorman.ourmemoryapp.domain.repository.FeedbackRepository
import kotlinx.coroutines.flow.MutableStateFlow

class FakeFeedbackRepository(
    private val error: Throwable? = null
) : FeedbackRepository {

    val drafts = mutableListOf<FeedbackDraft>()
    val feedback = MutableStateFlow(emptyList<Feedback>())

    override suspend fun send(draft: FeedbackDraft) {
        error?.let { throw it }
        drafts += draft
    }

    override fun observeFeedback() = feedback

    override suspend fun markReviewed(feedbackId: String) {
        feedback.value = feedback.value.map { if (it.id == feedbackId) it.copy(isReviewed = true) else it }
    }

    override suspend fun reply(feedbackId: String, text: String) {
        feedback.value = feedback.value.map {
            if (it.id == feedbackId) it.copy(isReviewed = true, reply = text) else it
        }
    }
}
