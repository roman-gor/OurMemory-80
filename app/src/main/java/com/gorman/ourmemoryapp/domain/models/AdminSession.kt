package com.gorman.ourmemoryapp.domain.models

data class AdminSession(
    val uid: String = "",
    val email: String = "",
    val isAdmin: Boolean = false,
    val isSuperAdmin: Boolean = false
)
