package com.gorman.ourmemoryapp.domain.models

data class AdminAccount(
    val uid: String,
    val email: String,
    val isSuperAdmin: Boolean
)
