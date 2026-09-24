package com.gorman.ourmemoryapp.testutil

import com.gorman.ourmemoryapp.domain.models.SubmissionDraft
import com.gorman.ourmemoryapp.domain.repository.SubmissionsRepository

class FakeSubmissionsRepository(
    private val error: Throwable? = null
) : SubmissionsRepository {

    val drafts = mutableListOf<SubmissionDraft>()

    override suspend fun submit(draft: SubmissionDraft) {
        error?.let { throw it }
        drafts += draft
    }
}
