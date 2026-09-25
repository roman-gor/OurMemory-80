package com.gorman.ourmemoryapp.di

import com.google.firebase.database.DatabaseReference
import com.gorman.ourmemoryapp.data.admins.datasource.remote.AdminsRemoteDataSourceImpl
import com.gorman.ourmemoryapp.data.admins.repository.AdminsRepositoryImpl
import com.gorman.ourmemoryapp.di.annotation.MemoryRoot
import com.gorman.ourmemoryapp.domain.repository.AdminsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AdminsModule {
    @Provides
    @Singleton
    fun provideAdminsRepository(@MemoryRoot root: DatabaseReference): AdminsRepository =
        AdminsRepositoryImpl(AdminsRemoteDataSourceImpl(root))
}
