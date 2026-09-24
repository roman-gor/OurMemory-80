package com.gorman.ourmemoryapp.data.auth.model

data class AuthUser(
    val uid: String,
    val email: String,
    val isAnonymous: Boolean
)
