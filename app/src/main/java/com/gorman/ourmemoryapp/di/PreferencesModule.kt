package com.gorman.ourmemoryapp.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.google.firebase.database.DatabaseReference
import com.gorman.ourmemoryapp.data.candles.datasource.local.CandlesLocalDataSourceImpl
import com.gorman.ourmemoryapp.data.candles.datasource.remote.CandlesRemoteDataSourceImpl
import com.gorman.ourmemoryapp.data.candles.repository.CandlesRepositoryImpl
import com.gorman.ourmemoryapp.data.favorites.repository.FavoritesRepositoryImpl
import com.gorman.ourmemoryapp.data.settings.repository.SettingsRepositoryImpl
import com.gorman.ourmemoryapp.data.tours.repository.TourProgressRepositoryImpl
import com.gorman.ourmemoryapp.di.annotation.MemoryRoot
import com.gorman.ourmemoryapp.domain.repository.CandlesRepository
import com.gorman.ourmemoryapp.domain.repository.FavoritesRepository
import com.gorman.ourmemoryapp.domain.repository.ReminderScheduler
import com.gorman.ourmemoryapp.domain.repository.SettingsRepository
import com.gorman.ourmemoryapp.domain.repository.TourProgressRepository
import com.gorman.ourmemoryapp.reminders.ReminderSchedulerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

private const val PREFERENCES_FILE_NAME = "memory_preferences"

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemDefaultZone()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create { context.preferencesDataStoreFile(PREFERENCES_FILE_NAME) }

    @Provides
    @Singleton
    fun provideCandlesRepository(
        @MemoryRoot root: DatabaseReference,
        dataStore: DataStore<Preferences>,
        clock: Clock
    ): CandlesRepository = CandlesRepositoryImpl(
        remoteDataSource = CandlesRemoteDataSourceImpl(root),
        localDataSource = CandlesLocalDataSourceImpl(dataStore),
        clock = clock
    )

    @Provides
    @Singleton
    fun provideSettingsRepository(dataStore: DataStore<Preferences>): SettingsRepository =
        SettingsRepositoryImpl(dataStore)

    @Provides
    @Singleton
    fun provideReminderScheduler(@ApplicationContext context: Context, clock: Clock): ReminderScheduler =
        ReminderSchedulerImpl(context, clock)

    @Provides
    @Singleton
    fun provideFavoritesRepository(dataStore: DataStore<Preferences>): FavoritesRepository =
        FavoritesRepositoryImpl(dataStore)

    @Provides
    @Singleton
    fun provideTourProgressRepository(dataStore: DataStore<Preferences>): TourProgressRepository =
        TourProgressRepositoryImpl(dataStore)
}
