package com.gorman.ourmemoryapp.data.candles.datasource.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CandlesLocalDataSourceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : CandlesLocalDataSource {

    override fun observeLastLitDate(veteranId: String): Flow<String?> =
        dataStore.data.map { it[lastLitKey(veteranId)] }

    override suspend fun saveLastLitDate(veteranId: String, date: String) {
        dataStore.edit { it[lastLitKey(veteranId)] = date }
    }

    private fun lastLitKey(veteranId: String) = stringPreferencesKey("$LAST_LIT_KEY_PREFIX$veteranId")

    companion object {
        private const val LAST_LIT_KEY_PREFIX = "candle_lit_"
    }
}
