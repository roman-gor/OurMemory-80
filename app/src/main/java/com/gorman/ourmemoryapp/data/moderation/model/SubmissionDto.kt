package com.gorman.ourmemoryapp.data.moderation.model

data class SubmissionDto(
    val id: String = "",
    val veteranId: String = "",
    val text: String = "",
    val contact: String = "",
    val photoPaths: List<String> = emptyList(),
    val status: String = "",
    val createdAt: Long = 0L
)
