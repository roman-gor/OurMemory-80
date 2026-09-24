package com.gorman.ourmemoryapp.domain.models

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val textScale: TextScale = TextScale.NORMAL,
    val victoryDayReminder: Boolean = true,
    val favoriteReminders: Boolean = true
)
