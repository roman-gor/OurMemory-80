package com.gorman.ourmemoryapp.domain.models

data class Feedback(
    val id: String,
    val type: FeedbackType,
    val text: String,
    val contact: String,
    val veteranId: String,
    val isReviewed: Boolean,
    val createdAt: Long
)
