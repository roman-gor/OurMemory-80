package com.gorman.ourmemoryapp.data.moderation.mapper

import com.gorman.ourmemoryapp.data.firebase.DatabaseNodes
import com.gorman.ourmemoryapp.data.firebase.VeteranKeys
import com.gorman.ourmemoryapp.data.moderation.model.SubmissionStatusValues
import com.gorman.ourmemoryapp.domain.models.SubmissionApproval
import com.gorman.ourmemoryapp.domain.models.VeteranInfoFormat.DESCRIPTION_SEPARATOR

fun SubmissionApproval.toUpdates(
    currentInfo: List<String>,
    reviewer: String,
    reviewedAt: Any
): Map<String, Any> {
    val submissionPath = "${DatabaseNodes.SUBMISSIONS}/${submission.id}"
    val newInfo = currentInfo +
        listOfNotNull(editedText.trim().takeIf { it.isNotEmpty() }) +
        approvedPhotoUrls.map { "$it$DESCRIPTION_SEPARATOR$photoCaption" }
    return mapOf(
        "${DatabaseNodes.VETERANS}/${VeteranKeys.forId(submission.veteranId)}/veteransInfo" to newInfo,
        "$submissionPath/status" to SubmissionStatusValues.APPROVED,
        "$submissionPath/reviewedBy" to reviewer,
        "$submissionPath/reviewedAt" to reviewedAt
    )
}
