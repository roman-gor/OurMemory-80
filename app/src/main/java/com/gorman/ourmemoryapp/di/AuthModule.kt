package com.gorman.ourmemoryapp.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.gorman.ourmemoryapp.data.accounts.datasource.remote.AccountIndexRemoteDataSourceImpl
import com.gorman.ourmemoryapp.data.auth.datasource.remote.AuthRemoteDataSourceImpl
import com.gorman.ourmemoryapp.data.auth.repository.AuthRepositoryImpl
import com.gorman.ourmemoryapp.di.annotation.MemoryRoot
import com.gorman.ourmemoryapp.domain.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideAuthRepository(auth: FirebaseAuth, @MemoryRoot root: DatabaseReference): AuthRepository =
        AuthRepositoryImpl(AuthRemoteDataSourceImpl(auth, root), AccountIndexRemoteDataSourceImpl(root))
}
