package com.gorman.ourmemoryapp.ui.admin.home.models

data class AdminHomeUiState(
    val email: String = "",
    val pendingSubmissionsCount: Int = 0,
    val newFeedbackCount: Int = 0,
    val contentCounts: ContentCounts = ContentCounts()
)
