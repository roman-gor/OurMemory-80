package com.gorman.ourmemoryapp.ui.admin.feedbacklist.models

sealed interface FeedbackListUiEvent {
    data class OnMarkReviewedClick(val feedbackId: String) : FeedbackListUiEvent
    data class OnReplyClick(val feedbackId: String, val reply: String) : FeedbackListUiEvent
}
