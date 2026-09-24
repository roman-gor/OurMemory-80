package com.gorman.ourmemoryapp.ui.more.models

import com.gorman.ourmemoryapp.domain.models.AppSettings

data class MoreUiState(
    val settings: AppSettings = AppSettings(),
    val unseenRequestsCount: Int = 0
)
