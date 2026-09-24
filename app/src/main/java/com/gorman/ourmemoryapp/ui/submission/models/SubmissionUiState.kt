package com.gorman.ourmemoryapp.ui.submission.models

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class SubmissionUiState(
    val veteranName: String = "",
    val text: String = "",
    val contact: String = "",
    val photoUris: ImmutableList<String> = persistentListOf(),
    val hasConsent: Boolean = false,
    val status: SubmissionStatus = SubmissionStatus.EDITING
) {
    val canSend = text.isNotBlank() && contact.isNotBlank() && hasConsent && status != SubmissionStatus.SENDING
    val canAddPhotos = photoUris.size < MAX_PHOTOS && status != SubmissionStatus.SENDING

    companion object {
        const val MAX_PHOTOS = 5
    }
}
