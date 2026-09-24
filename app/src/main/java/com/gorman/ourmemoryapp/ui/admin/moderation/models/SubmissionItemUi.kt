package com.gorman.ourmemoryapp.ui.admin.moderation.models

import com.gorman.ourmemoryapp.domain.models.ModerationStatus
import com.gorman.ourmemoryapp.domain.models.Submission
import com.gorman.ourmemoryapp.ui.admin.common.models.toAdminDate

data class SubmissionItemUi(
    val id: String,
    val veteranName: String,
    val text: String,
    val photoCount: Int,
    val status: ModerationStatus,
    val date: String
)

fun Submission.toItemUi(veteranName: String) = SubmissionItemUi(
    id = id,
    veteranName = veteranName.ifBlank { veteranId },
    text = text,
    photoCount = photoPaths.size,
    status = status,
    date = createdAt.toAdminDate()
)
