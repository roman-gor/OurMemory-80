package com.gorman.ourmemoryapp.data.requests.datasource.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RequestsSeenLocalDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    fun observeSeenAt() = dataStore.data.map { it[SEEN_AT_KEY] ?: 0L }

    suspend fun saveSeenAt(timestamp: Long) {
        dataStore.edit { it[SEEN_AT_KEY] = timestamp }
    }

    companion object {
        private val SEEN_AT_KEY = longPreferencesKey("requests_seen_at")
    }
}
