package com.gorman.ourmemoryapp.testutil

import com.gorman.ourmemoryapp.data.feedback.datasource.remote.FeedbackRemoteDataSource
import com.gorman.ourmemoryapp.data.feedback.model.FeedbackDto
import com.gorman.ourmemoryapp.domain.models.FeedbackDraft
import kotlinx.coroutines.flow.flowOf

class FakeFeedbackRemoteDataSource(
    private val items: List<FeedbackDto>
) : FeedbackRemoteDataSource {

    override suspend fun send(draft: FeedbackDraft) = Unit

    override fun observeFeedback() = flowOf(items)

    override suspend fun markReviewed(feedbackId: String) = Unit
}
