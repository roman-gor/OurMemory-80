package com.gorman.ourmemoryapp.data.feedback.model

data class FeedbackDto(
    val id: String = "",
    val type: String = "",
    val text: String = "",
    val contact: String = "",
    val veteranId: String = "",
    val status: String = "",
    val createdAt: Long = 0L
)
