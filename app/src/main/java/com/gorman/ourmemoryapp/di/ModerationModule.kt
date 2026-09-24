package com.gorman.ourmemoryapp.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.FirebaseStorage
import com.gorman.ourmemoryapp.data.moderation.datasource.remote.ModerationRemoteDataSourceImpl
import com.gorman.ourmemoryapp.data.moderation.repository.ModerationRepositoryImpl
import com.gorman.ourmemoryapp.di.annotation.MemoryRoot
import com.gorman.ourmemoryapp.domain.repository.ModerationRepository
import com.gorman.ourmemoryapp.domain.repository.VeteransRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ModerationModule {

    @Provides
    @Singleton
    fun provideModerationRepository(
        auth: FirebaseAuth,
        storage: FirebaseStorage,
        @MemoryRoot root: DatabaseReference,
        veteransRepository: VeteransRepository
    ): ModerationRepository = ModerationRepositoryImpl(
        remoteDataSource = ModerationRemoteDataSourceImpl(auth, storage, root),
        veteransRepository = veteransRepository
    )
}
