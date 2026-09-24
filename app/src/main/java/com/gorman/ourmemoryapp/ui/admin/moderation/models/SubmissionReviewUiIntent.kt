package com.gorman.ourmemoryapp.ui.admin.moderation.models

sealed interface SubmissionReviewUiIntent {
    data class OnTextChange(val text: String) : SubmissionReviewUiIntent
    data class OnPhotoToggle(val url: String) : SubmissionReviewUiIntent
    data class OnApproveClick(val photoCaption: String) : SubmissionReviewUiIntent
    data object OnRejectClick : SubmissionReviewUiIntent
}
