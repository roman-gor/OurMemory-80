package com.gorman.ourmemoryapp.data.submissions.repository

import com.gorman.ourmemoryapp.data.submissions.datasource.remote.SubmissionsRemoteDataSource
import com.gorman.ourmemoryapp.domain.models.SubmissionDraft
import com.gorman.ourmemoryapp.domain.repository.SubmissionsRepository
import javax.inject.Inject

class SubmissionsRepositoryImpl @Inject constructor(
    private val dataSource: SubmissionsRemoteDataSource
) : SubmissionsRepository {

    override suspend fun submit(draft: SubmissionDraft) {
        dataSource.submit(draft)
    }
}
