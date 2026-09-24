package com.gorman.ourmemoryapp.di

import com.google.firebase.database.DatabaseReference
import com.gorman.ourmemoryapp.data.content.datasource.remote.ContentRemoteDataSourceImpl
import com.gorman.ourmemoryapp.data.content.repository.ContentEditorRepositoryImpl
import com.gorman.ourmemoryapp.di.annotation.MemoryRoot
import com.gorman.ourmemoryapp.domain.repository.ContentEditorRepository
import com.gorman.ourmemoryapp.domain.repository.VeteransRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ContentModule {

    @Provides
    @Singleton
    fun provideContentEditorRepository(
        @MemoryRoot root: DatabaseReference,
        veteransRepository: VeteransRepository
    ): ContentEditorRepository = ContentEditorRepositoryImpl(
        remoteDataSource = ContentRemoteDataSourceImpl(root),
        veteransRepository = veteransRepository
    )
}
