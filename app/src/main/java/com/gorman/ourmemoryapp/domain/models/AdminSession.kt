package com.gorman.ourmemoryapp.domain.models

data class AdminSession(
    val email: String = "",
    val isAdmin: Boolean = false
)
