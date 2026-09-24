package com.gorman.ourmemoryapp.data.feedback.mapper

import com.gorman.ourmemoryapp.data.feedback.model.FeedbackDto
import com.gorman.ourmemoryapp.data.feedback.model.FeedbackStatus
import com.gorman.ourmemoryapp.domain.models.Feedback
import com.gorman.ourmemoryapp.domain.models.FeedbackType

fun FeedbackDto.toDomain() = Feedback(
    id = id,
    type = FeedbackType.entries.firstOrNull { it.name == type } ?: FeedbackType.OTHER,
    text = text,
    contact = contact,
    veteranId = veteranId,
    isReviewed = status == FeedbackStatus.DONE,
    createdAt = createdAt
)
