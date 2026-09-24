package com.gorman.ourmemoryapp.ui.admin.feedbacklist.models

sealed interface FeedbackListUiEvent {
    data class OnMarkReviewedClick(val feedbackId: String) : FeedbackListUiEvent
}
