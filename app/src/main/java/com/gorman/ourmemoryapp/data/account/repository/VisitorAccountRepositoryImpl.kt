package com.gorman.ourmemoryapp.data.account.repository

import com.gorman.ourmemoryapp.data.account.datasource.remote.GoogleAccountRemoteDataSource
import com.gorman.ourmemoryapp.data.accounts.datasource.remote.AccountIndexRemoteDataSource
import com.gorman.ourmemoryapp.data.favorites.datasource.local.FavoritesLocalDataSource
import com.gorman.ourmemoryapp.data.favorites.datasource.remote.FavoritesRemoteDataSource
import com.gorman.ourmemoryapp.domain.models.GoogleSignInResult
import com.gorman.ourmemoryapp.domain.repository.VisitorAccountRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class VisitorAccountRepositoryImpl @Inject constructor(
    private val accountDataSource: GoogleAccountRemoteDataSource,
    private val favoritesLocalDataSource: FavoritesLocalDataSource,
    private val favoritesRemoteDataSource: FavoritesRemoteDataSource,
    private val accountIndex: AccountIndexRemoteDataSource
) : VisitorAccountRepository {

    override fun observeAccount() = accountDataSource.observeAccount()

    override suspend fun signInWithGoogle(idToken: String): GoogleSignInResult {
        val account = accountDataSource.signInWithGoogle(idToken) ?: return GoogleSignInResult.FAILED
        if (account.email.isNotBlank()) runCatching { accountIndex.register(account.uid, account.email) }
        favoritesRemoteDataSource.addAll(account.uid, favoritesLocalDataSource.observe().first())
        return GoogleSignInResult.SUCCESS
    }

    override fun signOut() = accountDataSource.signOut()
}
