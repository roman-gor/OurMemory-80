package com.gorman.ourmemoryapp.ui.admin.login.models

sealed interface AdminLoginUiIntent {
    data class OnEmailChange(val email: String) : AdminLoginUiIntent
    data class OnPasswordChange(val password: String) : AdminLoginUiIntent
    data object OnSignInClick : AdminLoginUiIntent
}
