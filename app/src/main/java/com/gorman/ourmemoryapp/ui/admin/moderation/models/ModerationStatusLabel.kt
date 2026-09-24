package com.gorman.ourmemoryapp.ui.admin.moderation.models

import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.domain.models.ModerationStatus

val ModerationStatus.labelRes
    get() = when (this) {
        ModerationStatus.PENDING -> R.string.awaiting_review
        ModerationStatus.APPROVED -> R.string.approved
        ModerationStatus.REJECTED -> R.string.rejected
    }
