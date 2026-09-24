package com.gorman.ourmemoryapp.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeNotificationsAsked(): Flow<Boolean>
    suspend fun markNotificationsAsked()
}
