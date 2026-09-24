package com.gorman.ourmemoryapp.ui.admin.login.models

import androidx.annotation.StringRes
import com.gorman.ourmemoryapp.R

enum class AdminLoginError(@param:StringRes val messageRes: Int) {
    WRONG_CREDENTIALS(R.string.wrong_email_or_password_msg),
    NO_ADMIN_RIGHTS(R.string.no_admin_rights_msg),
    CONNECTION(R.string.failed_to_sign_in_msg)
}
