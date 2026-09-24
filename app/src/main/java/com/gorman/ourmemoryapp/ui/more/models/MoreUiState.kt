package com.gorman.ourmemoryapp.ui.more.models

import com.gorman.ourmemoryapp.domain.models.AppSettings
import com.gorman.ourmemoryapp.domain.models.VisitorAccount

data class MoreUiState(
    val settings: AppSettings = AppSettings(),
    val unseenRequestsCount: Int = 0,
    val account: VisitorAccount? = null,
    val signInStatus: SignInStatus = SignInStatus.IDLE
)
