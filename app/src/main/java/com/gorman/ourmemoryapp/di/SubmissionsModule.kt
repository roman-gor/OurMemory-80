package com.gorman.ourmemoryapp.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.gorman.ourmemoryapp.data.submissions.datasource.remote.SubmissionsRemoteDataSourceImpl
import com.gorman.ourmemoryapp.data.submissions.repository.SubmissionsRepositoryImpl
import com.gorman.ourmemoryapp.domain.repository.SubmissionsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SubmissionsModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideSubmissionsRepository(
        @ApplicationContext context: Context,
        auth: FirebaseAuth,
        storage: FirebaseStorage,
        database: FirebaseDatabase
    ): SubmissionsRepository = SubmissionsRepositoryImpl(
        SubmissionsRemoteDataSourceImpl(context, auth, storage, database)
    )
}
