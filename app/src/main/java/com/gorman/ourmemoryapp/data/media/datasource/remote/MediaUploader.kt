package com.gorman.ourmemoryapp.data.media.datasource.remote

import android.content.Context
import androidx.core.net.toUri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storageMetadata
import com.gorman.ourmemoryapp.data.firebase.DatabaseNodes
import com.gorman.ourmemoryapp.data.submissions.datasource.local.PhotoCompressor
import com.gorman.ourmemoryapp.di.annotation.IoDispatcher
import com.gorman.ourmemoryapp.domain.repository.MediaRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class MediaUploader @Inject constructor(
    private val storage: FirebaseStorage,
    private val photoCompressor: PhotoCompressor,
    @param:ApplicationContext private val context: Context,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : MediaRepository {

    override suspend fun uploadPhoto(uri: String, folder: String): String {
        val bytes = withContext(ioDispatcher) { photoCompressor.compress(uri.toUri()) }
        val reference = mediaReference(folder, "${UUID.randomUUID()}$JPEG_EXTENSION")
        reference.putBytes(bytes, storageMetadata { contentType = JPEG_TYPE }).await()
        return reference.downloadUrl.await().toString()
    }

    override suspend fun uploadAudio(uri: String, folder: String): String {
        val fileUri = uri.toUri()
        val type = context.contentResolver.getType(fileUri) ?: DEFAULT_AUDIO_TYPE
        val reference = mediaReference(folder, UUID.randomUUID().toString())
        reference.putFile(fileUri, storageMetadata { contentType = type }).await()
        return reference.downloadUrl.await().toString()
    }

    private fun mediaReference(folder: String, fileName: String): StorageReference =
        storage.getReference("${DatabaseNodes.ROOT}/$MEDIA_FOLDER/$folder/$fileName")

    companion object {
        private const val MEDIA_FOLDER = "Media"
        private const val JPEG_TYPE = "image/jpeg"
        private const val JPEG_EXTENSION = ".jpg"
        private const val DEFAULT_AUDIO_TYPE = "audio/mpeg"
    }
}
