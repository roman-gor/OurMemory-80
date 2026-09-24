package com.gorman.ourmemoryapp.testutil

import com.gorman.ourmemoryapp.domain.models.Submission
import com.gorman.ourmemoryapp.domain.models.SubmissionApproval
import com.gorman.ourmemoryapp.domain.repository.ModerationRepository
import kotlinx.coroutines.flow.MutableStateFlow

class FakeModerationRepository(
    submissions: List<Submission> = emptyList(),
    private val error: Throwable? = null
) : ModerationRepository {

    val submissions = MutableStateFlow(submissions)
    val approvals = mutableListOf<SubmissionApproval>()
    val rejections = mutableListOf<String>()

    override fun observeSubmissions() = submissions

    override suspend fun resolvePhotoUrls(photoPaths: List<String>) = photoPaths.map { "https://storage/$it" }

    override suspend fun approve(approval: SubmissionApproval) {
        error?.let { throw it }
        approvals += approval
    }

    override suspend fun reject(submissionId: String) {
        error?.let { throw it }
        rejections += submissionId
    }
}
