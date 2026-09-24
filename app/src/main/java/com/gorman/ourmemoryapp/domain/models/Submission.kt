package com.gorman.ourmemoryapp.domain.models

data class Submission(
    val id: String,
    val veteranId: String,
    val text: String,
    val contact: String,
    val photoPaths: List<String>,
    val status: ModerationStatus,
    val createdAt: Long
)
