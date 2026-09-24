package com.gorman.ourmemoryapp.data.moderation.repository

import com.gorman.ourmemoryapp.data.moderation.datasource.remote.ModerationRemoteDataSource
import com.gorman.ourmemoryapp.data.moderation.mapper.toDomain
import com.gorman.ourmemoryapp.domain.models.ModerationStatus
import com.gorman.ourmemoryapp.domain.models.SubmissionApproval
import com.gorman.ourmemoryapp.domain.repository.ModerationRepository
import com.gorman.ourmemoryapp.domain.repository.VeteransRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ModerationRepositoryImpl @Inject constructor(
    private val remoteDataSource: ModerationRemoteDataSource,
    private val veteransRepository: VeteransRepository
) : ModerationRepository {

    override fun observeSubmissions() = remoteDataSource.observeSubmissions().map { items ->
        items.filter { it.id.isNotBlank() }
            .map { it.toDomain() }
            .sortedWith(compareBy({ it.status != ModerationStatus.PENDING }, { -it.createdAt }))
    }

    override suspend fun resolvePhotoUrls(photoPaths: List<String>) = coroutineScope {
        photoPaths.map { path -> async { remoteDataSource.resolvePhotoUrl(path) } }.awaitAll()
    }

    override suspend fun approve(approval: SubmissionApproval) {
        remoteDataSource.approve(approval)
        veteransRepository.invalidate()
    }

    override suspend fun reject(submissionId: String, reply: String) = remoteDataSource.reject(submissionId, reply)
}
