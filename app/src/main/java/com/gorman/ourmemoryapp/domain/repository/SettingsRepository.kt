package com.gorman.ourmemoryapp.domain.repository

import com.gorman.ourmemoryapp.domain.models.AppSettings
import com.gorman.ourmemoryapp.domain.models.TextScale
import com.gorman.ourmemoryapp.domain.models.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeNotificationsAsked(): Flow<Boolean>
    suspend fun markNotificationsAsked()
    fun observeSettings(): Flow<AppSettings>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setTextScale(scale: TextScale)
    suspend fun setVictoryDayReminder(isEnabled: Boolean)
    suspend fun setFavoriteReminders(isEnabled: Boolean)
}
