package com.gorman.ourmemoryapp.data.favorites.datasource.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FavoritesLocalDataSourceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : FavoritesLocalDataSource {

    override fun observe() = dataStore.data.map { it[FAVORITES_KEY].orEmpty() }

    override suspend fun set(veteranId: String, isFavorite: Boolean) {
        dataStore.edit { preferences ->
            val favorites = preferences[FAVORITES_KEY].orEmpty()
            preferences[FAVORITES_KEY] = if (isFavorite) favorites + veteranId else favorites - veteranId
        }
    }

    companion object {
        private val FAVORITES_KEY = stringSetPreferencesKey("favorite_veterans")
    }
}
