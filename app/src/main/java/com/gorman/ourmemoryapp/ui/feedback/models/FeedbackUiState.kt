package com.gorman.ourmemoryapp.ui.feedback.models

import com.gorman.ourmemoryapp.domain.models.FeedbackType

data class FeedbackUiState(
    val isAboutVeteran: Boolean = false,
    val veteranName: String = "",
    val type: FeedbackType = FeedbackType.OTHER,
    val text: String = "",
    val contact: String = "",
    val status: FeedbackFormStatus = FeedbackFormStatus.EDITING
) {
    val canSend = text.isNotBlank() && status != FeedbackFormStatus.SENDING
}
