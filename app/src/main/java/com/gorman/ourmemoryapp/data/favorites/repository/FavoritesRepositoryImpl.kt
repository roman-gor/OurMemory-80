package com.gorman.ourmemoryapp.data.favorites.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.gorman.ourmemoryapp.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FavoritesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : FavoritesRepository {

    override fun observeFavorites() = dataStore.data.map { it[FAVORITES_KEY].orEmpty() }

    override suspend fun toggle(veteranId: String) {
        dataStore.edit { preferences ->
            val favorites = preferences[FAVORITES_KEY].orEmpty()
            preferences[FAVORITES_KEY] = if (veteranId in favorites) favorites - veteranId else favorites + veteranId
        }
    }

    companion object {
        private val FAVORITES_KEY = stringSetPreferencesKey("favorite_veterans")
    }
}
