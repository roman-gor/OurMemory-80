package com.gorman.ourmemoryapp.di

import android.content.Context
import com.google.firebase.storage.FirebaseStorage
import com.gorman.ourmemoryapp.data.media.datasource.remote.MediaUploader
import com.gorman.ourmemoryapp.data.submissions.datasource.local.PhotoCompressor
import com.gorman.ourmemoryapp.di.annotation.IoDispatcher
import com.gorman.ourmemoryapp.domain.repository.MediaRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaModule {

    @Provides
    @Singleton
    fun provideMediaRepository(
        storage: FirebaseStorage,
        @ApplicationContext context: Context,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): MediaRepository = MediaUploader(storage, PhotoCompressor(context), context, ioDispatcher)
}
