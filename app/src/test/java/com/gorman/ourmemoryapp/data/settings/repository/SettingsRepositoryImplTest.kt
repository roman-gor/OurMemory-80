package com.gorman.ourmemoryapp.data.settings.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.gorman.ourmemoryapp.domain.models.AppSettings
import com.gorman.ourmemoryapp.domain.models.TextScale
import com.gorman.ourmemoryapp.domain.models.ThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SettingsRepositoryImplTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val scope = CoroutineScope(SupervisorJob())

    private fun repository() = SettingsRepositoryImpl(
        PreferenceDataStoreFactory.create(scope = scope) {
            folder.newFile("settings.preferences_pb").also { it.delete() }
        }
    )

    @After
    fun tearDown() {
        scope.cancel()
    }

    @Test
    fun defaultsFollowSystemWithRemindersOn() = runTest {
        assertEquals(AppSettings(), repository().observeSettings().first())
    }

    @Test
    fun changesArePersisted() = runTest {
        val repository = repository()

        repository.setThemeMode(ThemeMode.DARK)
        repository.setTextScale(TextScale.EXTRA_LARGE)
        repository.setVictoryDayReminder(false)

        val settings = repository.observeSettings().first()
        assertEquals(ThemeMode.DARK, settings.themeMode)
        assertEquals(TextScale.EXTRA_LARGE, settings.textScale)
        assertEquals(false, settings.victoryDayReminder)
        assertEquals(true, settings.favoriteReminders)
    }

    @Test
    fun themeModeResolvesAgainstSystem() {
        assertEquals(true, ThemeMode.SYSTEM.isDark(isSystemDark = true))
        assertEquals(false, ThemeMode.LIGHT.isDark(isSystemDark = true))
        assertEquals(true, ThemeMode.DARK.isDark(isSystemDark = false))
    }
}
