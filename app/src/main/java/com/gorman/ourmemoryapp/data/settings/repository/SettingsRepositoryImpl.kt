package com.gorman.ourmemoryapp.data.settings.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.gorman.ourmemoryapp.domain.models.AppSettings
import com.gorman.ourmemoryapp.domain.models.TextScale
import com.gorman.ourmemoryapp.domain.models.ThemeMode
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

    override fun observeSettings() = dataStore.data.map { preferences ->
        AppSettings(
            themeMode = ThemeMode.entries.firstOrNull { it.name == preferences[THEME_MODE_KEY] } ?: ThemeMode.SYSTEM,
            textScale = TextScale.entries.firstOrNull { it.name == preferences[TEXT_SCALE_KEY] } ?: TextScale.NORMAL,
            victoryDayReminder = preferences[VICTORY_DAY_REMINDER_KEY] ?: true,
            favoriteReminders = preferences[FAVORITE_REMINDERS_KEY] ?: true
        )
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[THEME_MODE_KEY] = mode.name }
    }

    override suspend fun setTextScale(scale: TextScale) {
        dataStore.edit { it[TEXT_SCALE_KEY] = scale.name }
    }

    override suspend fun setVictoryDayReminder(isEnabled: Boolean) {
        dataStore.edit { it[VICTORY_DAY_REMINDER_KEY] = isEnabled }
    }

    override suspend fun setFavoriteReminders(isEnabled: Boolean) {
        dataStore.edit { it[FAVORITE_REMINDERS_KEY] = isEnabled }
    }

    companion object {
        private val NOTIFICATIONS_ASKED_KEY = booleanPreferencesKey("notifications_asked")
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val TEXT_SCALE_KEY = stringPreferencesKey("text_scale")
        private val VICTORY_DAY_REMINDER_KEY = booleanPreferencesKey("victory_day_reminder")
        private val FAVORITE_REMINDERS_KEY = booleanPreferencesKey("favorite_reminders")
    }
}
