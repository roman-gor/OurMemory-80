package com.gorman.ourmemoryapp.domain.repository

import com.gorman.ourmemoryapp.domain.models.Submission
import com.gorman.ourmemoryapp.domain.models.SubmissionApproval
import kotlinx.coroutines.flow.Flow

interface ModerationRepository {
    fun observeSubmissions(): Flow<List<Submission>>
    suspend fun resolvePhotoUrls(photoPaths: List<String>): List<String>
    suspend fun approve(approval: SubmissionApproval)
    suspend fun reject(submissionId: String, reply: String)
}
