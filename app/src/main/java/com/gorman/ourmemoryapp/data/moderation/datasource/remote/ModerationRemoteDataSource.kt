package com.gorman.ourmemoryapp.data.moderation.datasource.remote

import com.gorman.ourmemoryapp.data.moderation.model.SubmissionDto
import com.gorman.ourmemoryapp.domain.models.SubmissionApproval
import kotlinx.coroutines.flow.Flow

interface ModerationRemoteDataSource {
    fun observeSubmissions(): Flow<List<SubmissionDto>>
    suspend fun resolvePhotoUrl(photoPath: String): String
    suspend fun approve(approval: SubmissionApproval)
    suspend fun reject(submissionId: String, reply: String)
}
