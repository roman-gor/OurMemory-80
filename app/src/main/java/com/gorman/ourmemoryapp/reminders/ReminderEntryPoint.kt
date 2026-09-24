package com.gorman.ourmemoryapp.reminders

import com.gorman.ourmemoryapp.domain.repository.FavoritesRepository
import com.gorman.ourmemoryapp.domain.repository.SettingsRepository
import com.gorman.ourmemoryapp.domain.repository.VeteransRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ReminderEntryPoint {
    fun settingsRepository(): SettingsRepository
    fun favoritesRepository(): FavoritesRepository
    fun veteransRepository(): VeteransRepository
    fun clock(): Clock
}
