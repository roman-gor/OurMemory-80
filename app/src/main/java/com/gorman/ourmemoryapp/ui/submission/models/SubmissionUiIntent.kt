package com.gorman.ourmemoryapp.ui.submission.models

sealed interface SubmissionUiIntent {
    data class OnTextChange(val text: String) : SubmissionUiIntent
    data class OnContactChange(val contact: String) : SubmissionUiIntent
    data class OnConsentChange(val hasConsent: Boolean) : SubmissionUiIntent
    data class OnPhotosPicked(val uris: List<String>) : SubmissionUiIntent
    data class OnRemovePhoto(val index: Int) : SubmissionUiIntent
    data object OnSendClick : SubmissionUiIntent
}
