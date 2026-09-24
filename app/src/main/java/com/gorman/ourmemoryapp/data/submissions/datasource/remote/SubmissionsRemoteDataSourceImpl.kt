package com.gorman.ourmemoryapp.data.submissions.datasource.remote

import android.content.Context
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storageMetadata
import com.gorman.ourmemoryapp.domain.models.SubmissionDraft
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SubmissionsRemoteDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    private val database: FirebaseDatabase
) : SubmissionsRemoteDataSource {

    override suspend fun submit(draft: SubmissionDraft) {
        if (auth.currentUser == null) auth.signInAnonymously().await()
        val reference = database.getReference(SUBMISSIONS_PATH).push()
        val submissionId = requireNotNull(reference.key)
        val photoPaths = draft.photoUris.mapIndexed { index, uriString ->
            val uri = uriString.toUri()
            val path = "$SUBMISSIONS_PATH/$submissionId/$index"
            val metadata = storageMetadata {
                contentType = context.contentResolver.getType(uri) ?: DEFAULT_IMAGE_TYPE
            }
            storage.getReference(path).putFile(uri, metadata).await()
            path
        }
        reference.setValue(
            mapOf(
                "id" to submissionId,
                "veteranId" to draft.veteranId,
                "text" to draft.text,
                "contact" to draft.contact,
                "photoPaths" to photoPaths,
                "status" to STATUS_PENDING,
                "createdAt" to ServerValue.TIMESTAMP
            )
        ).await()
    }

    companion object {
        private const val SUBMISSIONS_PATH = "Submissions"
        private const val STATUS_PENDING = "pending"
        private const val DEFAULT_IMAGE_TYPE = "image/jpeg"
    }
}
