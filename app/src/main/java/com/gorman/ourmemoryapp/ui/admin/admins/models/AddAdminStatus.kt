package com.gorman.ourmemoryapp.ui.admin.admins.models

import androidx.annotation.StringRes
import com.gorman.ourmemoryapp.R

enum class AddAdminStatus(@param:StringRes val messageRes: Int?) {
    IDLE(null),
    ADDING(null),
    ADDED(R.string.administrator_added_msg),
    ACCOUNT_NOT_FOUND(R.string.account_not_found_msg),
    ALREADY_ADMIN(R.string.already_administrator_msg),
    FAILED(R.string.failed_to_save_msg)
}
