package com.gorman.ourmemoryapp.data.submissions.datasource.remote

import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storageMetadata
import com.gorman.ourmemoryapp.data.submissions.datasource.local.PhotoCompressor
import com.gorman.ourmemoryapp.di.annotation.IoDispatcher
import com.gorman.ourmemoryapp.domain.models.SubmissionDraft
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SubmissionsRemoteDataSourceImpl @Inject constructor(
    private val photoCompressor: PhotoCompressor,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    private val database: FirebaseDatabase,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : SubmissionsRemoteDataSource {

    override suspend fun submit(draft: SubmissionDraft) {
        if (auth.currentUser == null) auth.signInAnonymously().await()
        val reference = database.getReference(SUBMISSIONS_PATH).push()
        val submissionId = requireNotNull(reference.key)
        val metadata = storageMetadata { contentType = JPEG_TYPE }
        val photoPaths = draft.photoUris.mapIndexed { index, uriString ->
            val bytes = withContext(ioDispatcher) { photoCompressor.compress(uriString.toUri()) }
            val path = "$SUBMISSIONS_PATH/$submissionId/$index$JPEG_EXTENSION"
            storage.getReference(path).putBytes(bytes, metadata).await()
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
        private const val JPEG_TYPE = "image/jpeg"
        private const val JPEG_EXTENSION = ".jpg"
    }
}
