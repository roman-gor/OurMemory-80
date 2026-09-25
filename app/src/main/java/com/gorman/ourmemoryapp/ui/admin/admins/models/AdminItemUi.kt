package com.gorman.ourmemoryapp.ui.admin.admins.models

data class AdminItemUi(
    val uid: String,
    val email: String,
    val isSuperAdmin: Boolean,
    val isCurrentUser: Boolean
) {
    val canRemove = !isSuperAdmin && !isCurrentUser
}
