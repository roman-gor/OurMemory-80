package com.gorman.ourmemoryapp.data.moderation.mapper

import com.gorman.ourmemoryapp.data.moderation.model.SubmissionDto
import com.gorman.ourmemoryapp.data.moderation.model.SubmissionStatusValues
import com.gorman.ourmemoryapp.domain.models.ModerationStatus
import com.gorman.ourmemoryapp.domain.models.Submission

fun SubmissionDto.toDomain() = Submission(
    id = id,
    veteranId = veteranId,
    text = text,
    contact = contact,
    photoPaths = photoPaths,
    status = when (status) {
        SubmissionStatusValues.APPROVED -> ModerationStatus.APPROVED
        SubmissionStatusValues.REJECTED -> ModerationStatus.REJECTED
        else -> ModerationStatus.PENDING
    },
    createdAt = createdAt,
    reply = reply,
    reviewedAt = reviewedAt
)
