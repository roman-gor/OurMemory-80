package com.gorman.ourmemoryapp.data.feedback.repository

import com.gorman.ourmemoryapp.data.feedback.datasource.remote.FeedbackRemoteDataSource
import com.gorman.ourmemoryapp.data.feedback.mapper.toDomain
import com.gorman.ourmemoryapp.domain.models.FeedbackDraft
import com.gorman.ourmemoryapp.domain.repository.FeedbackRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FeedbackRepositoryImpl @Inject constructor(
    private val remoteDataSource: FeedbackRemoteDataSource
) : FeedbackRepository {

    override suspend fun send(draft: FeedbackDraft) = remoteDataSource.send(draft)

    override fun observeFeedback() = remoteDataSource.observeFeedback().map { items ->
        items.map { it.toDomain() }.sortedWith(compareBy({ it.isReviewed }, { -it.createdAt }))
    }

    override suspend fun markReviewed(feedbackId: String) = remoteDataSource.markReviewed(feedbackId)
}
