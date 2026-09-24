package com.gorman.ourmemoryapp.data.moderation.datasource.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ServerValue
import com.google.firebase.storage.FirebaseStorage
import com.gorman.ourmemoryapp.data.firebase.DatabaseNodes
import com.gorman.ourmemoryapp.data.firebase.VeteranKeys
import com.gorman.ourmemoryapp.data.firebase.childrenAs
import com.gorman.ourmemoryapp.data.firebase.observeValue
import com.gorman.ourmemoryapp.data.moderation.mapper.toUpdates
import com.gorman.ourmemoryapp.data.moderation.model.SubmissionDto
import com.gorman.ourmemoryapp.data.moderation.model.SubmissionStatusValues
import com.gorman.ourmemoryapp.di.annotation.MemoryRoot
import com.gorman.ourmemoryapp.domain.models.SubmissionApproval
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ModerationRemoteDataSourceImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    @param:MemoryRoot private val root: DatabaseReference
) : ModerationRemoteDataSource {

    private val submissionsReference = root.child(DatabaseNodes.SUBMISSIONS)

    override fun observeSubmissions() = submissionsReference.observeValue().map { it.childrenAs<SubmissionDto>() }

    override suspend fun resolvePhotoUrl(photoPath: String) =
        storage.getReference(photoPath).downloadUrl.await().toString()

    override suspend fun approve(approval: SubmissionApproval) {
        val veteran = root.child(DatabaseNodes.VETERANS)
            .child(VeteranKeys.forId(approval.submission.veteranId))
            .get()
            .await()
        check(veteran.exists()) { "Veteran ${approval.submission.veteranId} does not exist" }
        val currentInfo = veteran.child(VETERANS_INFO).childrenAs<String>()
        val updates = approval.toUpdates(
            currentInfo = currentInfo,
            reviewer = auth.currentUser?.email.orEmpty(),
            reviewedAt = ServerValue.TIMESTAMP
        )
        root.updateChildren(updates).await()
    }

    override suspend fun reject(submissionId: String) {
        submissionsReference.child(submissionId).updateChildren(
            mapOf(
                "status" to SubmissionStatusValues.REJECTED,
                "reviewedBy" to auth.currentUser?.email.orEmpty(),
                "reviewedAt" to ServerValue.TIMESTAMP
            )
        ).await()
    }

    companion object {
        private const val VETERANS_INFO = "veteransInfo"
    }
}
