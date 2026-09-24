package com.gorman.ourmemoryapp.ui.admin.login.models

data class AdminLoginUiState(
    val email: String = "",
    val password: String = "",
    val isSigningIn: Boolean = false,
    val isSignedIn: Boolean = false,
    val error: AdminLoginError? = null
) {
    val canSignIn = email.isNotBlank() && password.isNotEmpty() && !isSigningIn
}
