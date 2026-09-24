package com.gorman.ourmemoryapp.data.submissions.datasource.remote

import com.gorman.ourmemoryapp.domain.models.SubmissionDraft

interface SubmissionsRemoteDataSource {
    suspend fun submit(draft: SubmissionDraft)
}
