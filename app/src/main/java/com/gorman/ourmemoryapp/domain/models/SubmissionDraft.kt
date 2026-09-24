package com.gorman.ourmemoryapp.domain.models

data class SubmissionDraft(
    val veteranId: String,
    val text: String,
    val contact: String,
    val photoUris: List<String>
)
