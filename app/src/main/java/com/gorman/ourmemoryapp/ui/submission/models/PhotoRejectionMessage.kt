package com.gorman.ourmemoryapp.ui.submission.models

import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.domain.models.PhotoCheckResult

val PhotoCheckResult.messageRes
    get() = when (this) {
        PhotoCheckResult.UNREADABLE -> R.string.could_not_open_photo_msg
        PhotoCheckResult.ALLOWED, PhotoCheckResult.BLOCKED -> R.string.photo_did_not_pass_check_msg
    }
