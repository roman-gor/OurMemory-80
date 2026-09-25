package com.gorman.ourmemoryapp.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.gorman.ourmemoryapp.data.account.datasource.remote.GoogleAccountRemoteDataSource
import com.gorman.ourmemoryapp.data.account.datasource.remote.GoogleAccountRemoteDataSourceImpl
import com.gorman.ourmemoryapp.data.account.repository.VisitorAccountRepositoryImpl
import com.gorman.ourmemoryapp.data.accounts.datasource.remote.AccountIndexRemoteDataSourceImpl
import com.gorman.ourmemoryapp.data.favorites.datasource.local.FavoritesLocalDataSource
import com.gorman.ourmemoryapp.data.favorites.datasource.remote.FavoritesRemoteDataSource
import com.gorman.ourmemoryapp.data.favorites.datasource.remote.FavoritesRemoteDataSourceImpl
import com.gorman.ourmemoryapp.di.annotation.MemoryRoot
import com.gorman.ourmemoryapp.domain.repository.VisitorAccountRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AccountModule {

    @Provides
    @Singleton
    fun provideGoogleAccountRemoteDataSource(auth: FirebaseAuth): GoogleAccountRemoteDataSource =
        GoogleAccountRemoteDataSourceImpl(auth)

    @Provides
    @Singleton
    fun provideFavoritesRemoteDataSource(@MemoryRoot root: DatabaseReference): FavoritesRemoteDataSource =
        FavoritesRemoteDataSourceImpl(root)

    @Provides
    @Singleton
    fun provideVisitorAccountRepository(
        accountDataSource: GoogleAccountRemoteDataSource,
        favoritesLocalDataSource: FavoritesLocalDataSource,
        favoritesRemoteDataSource: FavoritesRemoteDataSource,
        @MemoryRoot root: DatabaseReference
    ): VisitorAccountRepository = VisitorAccountRepositoryImpl(
        accountDataSource = accountDataSource,
        favoritesLocalDataSource = favoritesLocalDataSource,
        favoritesRemoteDataSource = favoritesRemoteDataSource,
        accountIndex = AccountIndexRemoteDataSourceImpl(root)
    )
}
