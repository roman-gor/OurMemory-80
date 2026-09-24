package com.gorman.ourmemoryapp.domain.models

data class FeedbackDraft(
    val type: FeedbackType,
    val text: String,
    val contact: String,
    val veteranId: String
)
