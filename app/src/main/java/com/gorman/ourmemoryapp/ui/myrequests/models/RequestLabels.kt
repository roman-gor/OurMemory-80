package com.gorman.ourmemoryapp.ui.myrequests.models

import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.domain.models.RequestKind
import com.gorman.ourmemoryapp.domain.models.RequestStatus

val RequestStatus.labelRes
    get() = when (this) {
        RequestStatus.IN_REVIEW -> R.string.awaiting_review
        RequestStatus.APPROVED -> R.string.added_to_card
        RequestStatus.REJECTED -> R.string.rejected
        RequestStatus.REVIEWED -> R.string.reviewed
    }

val RequestKind.labelRes
    get() = when (this) {
        RequestKind.SUBMISSION -> R.string.materials_for_card
        RequestKind.FEEDBACK -> R.string.message
    }
