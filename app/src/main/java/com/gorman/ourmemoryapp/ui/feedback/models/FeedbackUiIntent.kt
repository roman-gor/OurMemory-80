package com.gorman.ourmemoryapp.ui.feedback.models

import com.gorman.ourmemoryapp.domain.models.FeedbackType

sealed interface FeedbackUiIntent {
    data class OnTypeChange(val type: FeedbackType) : FeedbackUiIntent
    data class OnTextChange(val text: String) : FeedbackUiIntent
    data class OnContactChange(val contact: String) : FeedbackUiIntent
    data object OnSendClick : FeedbackUiIntent
}
