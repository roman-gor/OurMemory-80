package com.gorman.ourmemoryapp.domain.models

data class SubmissionApproval(
    val submission: Submission,
    val editedText: String,
    val approvedPhotoUrls: List<String>,
    val photoCaption: String
)
