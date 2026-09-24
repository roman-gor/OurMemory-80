package com.gorman.ourmemoryapp.ui.admin.moderation.models

import com.gorman.ourmemoryapp.domain.models.ModerationStatus
import kotlinx.collections.immutable.ImmutableList

sealed interface SubmissionReviewUiState {
    data object Loading : SubmissionReviewUiState
    data object Error : SubmissionReviewUiState
    data class Success(
        val veteranName: String,
        val contact: String,
        val date: String,
        val text: String,
        val photos: ImmutableList<ReviewPhotoUi>,
        val status: ModerationStatus,
        val isProcessing: Boolean,
        val hasFailed: Boolean,
        val isFinished: Boolean
    ) : SubmissionReviewUiState {
        val isEditable = status == ModerationStatus.PENDING && !isProcessing
    }
}
