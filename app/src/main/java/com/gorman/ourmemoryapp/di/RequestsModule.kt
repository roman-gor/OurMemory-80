package com.gorman.ourmemoryapp.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.gorman.ourmemoryapp.data.requests.datasource.local.RequestsSeenLocalDataSource
import com.gorman.ourmemoryapp.data.requests.datasource.remote.MyRequestsRemoteDataSourceImpl
import com.gorman.ourmemoryapp.data.requests.repository.MyRequestsRepositoryImpl
import com.gorman.ourmemoryapp.di.annotation.MemoryRoot
import com.gorman.ourmemoryapp.domain.repository.MyRequestsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RequestsModule {

    @Provides
    @Singleton
    fun provideMyRequestsRepository(
        auth: FirebaseAuth,
        @MemoryRoot root: DatabaseReference,
        dataStore: DataStore<Preferences>,
        clock: Clock
    ): MyRequestsRepository = MyRequestsRepositoryImpl(
        remoteDataSource = MyRequestsRemoteDataSourceImpl(auth, root),
        seenLocalDataSource = RequestsSeenLocalDataSource(dataStore),
        clock = clock
    )
}
