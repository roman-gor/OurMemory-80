package com.gorman.ourmemoryapp.data.requests.mapper

import com.gorman.ourmemoryapp.data.feedback.model.FeedbackDto
import com.gorman.ourmemoryapp.data.feedback.model.FeedbackStatus
import com.gorman.ourmemoryapp.data.moderation.model.SubmissionDto
import com.gorman.ourmemoryapp.data.moderation.model.SubmissionStatusValues
import com.gorman.ourmemoryapp.domain.models.MyRequest
import com.gorman.ourmemoryapp.domain.models.RequestKind
import com.gorman.ourmemoryapp.domain.models.RequestStatus

fun SubmissionDto.toMyRequest() = MyRequest(
    id = id,
    kind = RequestKind.SUBMISSION,
    veteranId = veteranId,
    text = text,
    status = when (status) {
        SubmissionStatusValues.APPROVED -> RequestStatus.APPROVED
        SubmissionStatusValues.REJECTED -> RequestStatus.REJECTED
        else -> RequestStatus.IN_REVIEW
    },
    reply = reply,
    createdAt = createdAt,
    reviewedAt = reviewedAt
)

fun FeedbackDto.toMyRequest() = MyRequest(
    id = id,
    kind = RequestKind.FEEDBACK,
    veteranId = veteranId,
    text = text,
    status = if (status == FeedbackStatus.DONE) RequestStatus.REVIEWED else RequestStatus.IN_REVIEW,
    reply = reply,
    createdAt = createdAt,
    reviewedAt = reviewedAt
)
