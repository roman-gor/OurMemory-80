package com.gorman.ourmemoryapp.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.FirebaseStorage
import com.gorman.ourmemoryapp.data.submissions.datasource.local.PhotoCompressor
import com.gorman.ourmemoryapp.data.submissions.datasource.remote.SubmissionsRemoteDataSourceImpl
import com.gorman.ourmemoryapp.data.submissions.repository.SubmissionsRepositoryImpl
import com.gorman.ourmemoryapp.di.annotation.IoDispatcher
import com.gorman.ourmemoryapp.di.annotation.MemoryRoot
import com.gorman.ourmemoryapp.domain.repository.SubmissionsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
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
        @MemoryRoot root: DatabaseReference,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): SubmissionsRepository = SubmissionsRepositoryImpl(
        SubmissionsRemoteDataSourceImpl(PhotoCompressor(context), auth, storage, root, ioDispatcher)
    )
}
