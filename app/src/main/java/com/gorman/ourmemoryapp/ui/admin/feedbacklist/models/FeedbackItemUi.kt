package com.gorman.ourmemoryapp.ui.admin.feedbacklist.models

import com.gorman.ourmemoryapp.domain.models.Feedback
import com.gorman.ourmemoryapp.domain.models.FeedbackType
import com.gorman.ourmemoryapp.ui.admin.common.models.toAdminDate

data class FeedbackItemUi(
    val id: String,
    val type: FeedbackType,
    val text: String,
    val contact: String,
    val veteranId: String,
    val veteranName: String,
    val isReviewed: Boolean,
    val date: String
)

fun Feedback.toUi(veteranName: String) = FeedbackItemUi(
    id = id,
    type = type,
    text = text,
    contact = contact,
    veteranId = veteranId,
    veteranName = veteranName,
    isReviewed = isReviewed,
    date = createdAt.toAdminDate()
)
