package com.gorman.ourmemoryapp.data.settings.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.gorman.ourmemoryapp.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    override fun observeNotificationsAsked(): Flow<Boolean> =
        dataStore.data.map { it[NOTIFICATIONS_ASKED_KEY] == true }

    override suspend fun markNotificationsAsked() {
        dataStore.edit { it[NOTIFICATIONS_ASKED_KEY] = true }
    }

    companion object {
        private val NOTIFICATIONS_ASKED_KEY = booleanPreferencesKey("notifications_asked")
    }
}
