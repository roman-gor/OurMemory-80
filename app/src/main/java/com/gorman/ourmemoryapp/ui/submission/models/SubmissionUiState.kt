package com.gorman.ourmemoryapp.ui.submission.models

import com.gorman.ourmemoryapp.domain.models.PhotoCheckResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class SubmissionUiState(
    val veteranName: String = "",
    val text: String = "",
    val contact: String = "",
    val photoUris: ImmutableList<String> = persistentListOf(),
    val checkingPhotosCount: Int = 0,
    val photoRejection: PhotoCheckResult? = null,
    val hasTextProfanity: Boolean = false,
    val hasContactProfanity: Boolean = false,
    val hasConsent: Boolean = false,
    val status: SubmissionStatus = SubmissionStatus.EDITING
) {
    val canSend = text.isNotBlank() && contact.isNotBlank() && hasConsent &&
        checkingPhotosCount == 0 && status != SubmissionStatus.SENDING
    val canAddPhotos = photoUris.size + checkingPhotosCount < MAX_PHOTOS && status != SubmissionStatus.SENDING

    companion object {
        const val MAX_PHOTOS = 5
    }
}
