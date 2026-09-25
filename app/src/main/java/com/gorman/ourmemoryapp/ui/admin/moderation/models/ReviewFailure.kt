package com.gorman.ourmemoryapp.ui.admin.moderation.models

import androidx.annotation.StringRes
import com.gorman.ourmemoryapp.R

enum class ReviewFailure(@param:StringRes val messageRes: Int) {
    NETWORK(R.string.failed_to_save_msg),
    NO_PERMISSION(R.string.no_permission_to_change_request_msg)
}
