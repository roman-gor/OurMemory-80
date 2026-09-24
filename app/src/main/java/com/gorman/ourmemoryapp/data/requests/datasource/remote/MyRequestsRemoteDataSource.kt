package com.gorman.ourmemoryapp.data.requests.datasource.remote

import com.gorman.ourmemoryapp.data.feedback.model.FeedbackDto
import com.gorman.ourmemoryapp.data.moderation.model.SubmissionDto
import kotlinx.coroutines.flow.Flow

interface MyRequestsRemoteDataSource {
    fun observeCurrentUid(): Flow<String?>
    fun observeSubmissions(authorUid: String): Flow<List<SubmissionDto>>
    fun observeFeedback(authorUid: String): Flow<List<FeedbackDto>>
}
