package com.gorman.ourmemoryapp.di

import com.google.firebase.database.DatabaseReference
import com.gorman.ourmemoryapp.data.auth.datasource.remote.AnonymousSession
import com.gorman.ourmemoryapp.data.feedback.datasource.remote.FeedbackRemoteDataSourceImpl
import com.gorman.ourmemoryapp.data.feedback.repository.FeedbackRepositoryImpl
import com.gorman.ourmemoryapp.di.annotation.MemoryRoot
import com.gorman.ourmemoryapp.domain.repository.FeedbackRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FeedbackModule {

    @Provides
    @Singleton
    fun provideFeedbackRepository(
        anonymousSession: AnonymousSession,
        @MemoryRoot root: DatabaseReference
    ): FeedbackRepository = FeedbackRepositoryImpl(FeedbackRemoteDataSourceImpl(anonymousSession, root))
}
