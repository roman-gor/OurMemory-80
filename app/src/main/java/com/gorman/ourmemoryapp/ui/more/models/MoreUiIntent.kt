package com.gorman.ourmemoryapp.ui.more.models

import com.gorman.ourmemoryapp.domain.models.TextScale
import com.gorman.ourmemoryapp.domain.models.ThemeMode

sealed interface MoreUiIntent {
    data class OnThemeModeChange(val mode: ThemeMode) : MoreUiIntent
    data class OnTextScaleChange(val scale: TextScale) : MoreUiIntent
    data class OnVictoryDayReminderChange(val isEnabled: Boolean) : MoreUiIntent
    data class OnFavoriteRemindersChange(val isEnabled: Boolean) : MoreUiIntent
}
