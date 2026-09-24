package com.gorman.ourmemoryapp.data.favorites.repository

import com.gorman.ourmemoryapp.data.account.datasource.remote.GoogleAccountRemoteDataSource
import com.gorman.ourmemoryapp.data.favorites.datasource.local.FavoritesLocalDataSource
import com.gorman.ourmemoryapp.data.favorites.datasource.remote.FavoritesRemoteDataSource
import com.gorman.ourmemoryapp.domain.repository.FavoritesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FavoritesRepositoryImpl @Inject constructor(
    private val localDataSource: FavoritesLocalDataSource,
    private val remoteDataSource: FavoritesRemoteDataSource,
    private val accountDataSource: GoogleAccountRemoteDataSource
) : FavoritesRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeFavorites(): Flow<Set<String>> = observeAccountUid().flatMapLatest { uid ->
        if (uid == null) {
            localDataSource.observe()
        } else {
            remoteDataSource.observe(uid).catch { emitAll(localDataSource.observe()) }
        }
    }

    override suspend fun toggle(veteranId: String) {
        val isFavorite = veteranId !in observeFavorites().first()
        localDataSource.set(veteranId, isFavorite)
        observeAccountUid().first()?.let { remoteDataSource.set(it, veteranId, isFavorite) }
    }

    private fun observeAccountUid() = accountDataSource.observeAccount()
        .map { it?.uid }
        .distinctUntilChanged()
}
