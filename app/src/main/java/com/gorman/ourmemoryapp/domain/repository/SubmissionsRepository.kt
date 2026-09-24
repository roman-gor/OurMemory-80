package com.gorman.ourmemoryapp.domain.repository

import com.gorman.ourmemoryapp.domain.models.SubmissionDraft

interface SubmissionsRepository {
    suspend fun submit(draft: SubmissionDraft)
}
