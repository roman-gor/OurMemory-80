package com.gorman.ourmemoryapp.di

import com.gorman.ourmemoryapp.data.contentcheck.datasource.local.ExtremismDetector
import com.gorman.ourmemoryapp.data.contentcheck.datasource.local.LatinProfanityDetector
import com.gorman.ourmemoryapp.data.contentcheck.datasource.local.NsfwImageClassifier
import com.gorman.ourmemoryapp.data.contentcheck.datasource.local.ProfanityDetector
import com.gorman.ourmemoryapp.data.contentcheck.repository.ContentCheckRepositoryImpl
import com.gorman.ourmemoryapp.di.annotation.IoDispatcher
import com.gorman.ourmemoryapp.domain.repository.ContentCheckRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ContentCheckModule {

    @Provides
    @Singleton
    fun provideContentCheckRepository(
        profanityDetector: ProfanityDetector,
        latinProfanityDetector: LatinProfanityDetector,
        extremismDetector: ExtremismDetector,
        imageClassifier: NsfwImageClassifier,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): ContentCheckRepository = ContentCheckRepositoryImpl(
        profanityDetector = profanityDetector,
        latinProfanityDetector = latinProfanityDetector,
        extremismDetector = extremismDetector,
        imageClassifier = imageClassifier,
        ioDispatcher = ioDispatcher
    )
}
