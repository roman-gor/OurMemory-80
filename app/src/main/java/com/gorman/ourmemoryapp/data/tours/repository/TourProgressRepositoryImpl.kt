package com.gorman.ourmemoryapp.data.tours.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.gorman.ourmemoryapp.domain.repository.TourProgressRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TourProgressRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : TourProgressRepository {

    override fun observeProgress() = dataStore.data.map { preferences ->
        preferences.asMap()
            .filterKeys { it.name.startsWith(KEY_PREFIX) }
            .map { (key, value) ->
                key.name.removePrefix(KEY_PREFIX) to (value as? Set<*>).orEmpty()
                    .mapNotNull { (it as? String)?.toIntOrNull() }
                    .toSet()
            }
            .toMap()
    }

    override suspend fun toggleStop(tourId: String, stopIndex: Int) {
        val key = progressKey(tourId)
        val stop = stopIndex.toString()
        dataStore.edit { preferences ->
            val visited = preferences[key].orEmpty()
            preferences[key] = if (stop in visited) visited - stop else visited + stop
        }
    }

    override suspend fun reset(tourId: String) {
        dataStore.edit { it.remove(progressKey(tourId)) }
    }

    private fun progressKey(tourId: String) = stringSetPreferencesKey("$KEY_PREFIX$tourId")

    companion object {
        private const val KEY_PREFIX = "tour_progress_"
    }
}
