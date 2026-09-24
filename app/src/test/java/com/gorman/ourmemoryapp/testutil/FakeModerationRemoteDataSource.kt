package com.gorman.ourmemoryapp.testutil

import com.gorman.ourmemoryapp.data.moderation.datasource.remote.ModerationRemoteDataSource
import com.gorman.ourmemoryapp.data.moderation.model.SubmissionDto
import com.gorman.ourmemoryapp.domain.models.SubmissionApproval
import kotlinx.coroutines.flow.flowOf

class FakeModerationRemoteDataSource(
    private val items: List<SubmissionDto> = emptyList()
) : ModerationRemoteDataSource {

    val approvals = mutableListOf<SubmissionApproval>()

    override fun observeSubmissions() = flowOf(items)

    override suspend fun resolvePhotoUrl(photoPath: String) = "https://storage/$photoPath"

    override suspend fun approve(approval: SubmissionApproval) {
        approvals += approval
    }

    override suspend fun reject(submissionId: String) = Unit
}
