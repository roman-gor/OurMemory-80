package com.gorman.ourmemoryapp.data.auth.repository

import com.gorman.ourmemoryapp.data.accounts.datasource.remote.AccountIndexRemoteDataSource
import com.gorman.ourmemoryapp.data.auth.datasource.remote.AuthRemoteDataSource
import com.gorman.ourmemoryapp.data.auth.model.AuthUser
import com.gorman.ourmemoryapp.domain.models.AdminSession
import com.gorman.ourmemoryapp.domain.models.SignInResult
import com.gorman.ourmemoryapp.domain.repository.AuthRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val remoteDataSource: AuthRemoteDataSource,
    private val accountIndex: AccountIndexRemoteDataSource
) : AuthRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeSession(): Flow<AdminSession> = remoteDataSource.observeUser().flatMapLatest { user ->
        if (user == null || user.isAnonymous) {
            flowOf(AdminSession())
        } else {
            combine(
                remoteDataSource.observeIsAdmin(user.uid),
                remoteDataSource.observeIsSuperAdmin(user.uid)
            ) { isAdmin, isSuperAdmin ->
                AdminSession(uid = user.uid, email = user.email, isAdmin = isAdmin, isSuperAdmin = isSuperAdmin)
            }.onStart { registerAccount(user) }
        }
    }

    override suspend fun signIn(email: String, password: String): SignInResult {
        val user = remoteDataSource.signIn(email, password) ?: return SignInResult.WRONG_CREDENTIALS
        registerAccount(user)
        return if (remoteDataSource.isAdmin(user.uid)) {
            SignInResult.ADMIN
        } else {
            remoteDataSource.signOut()
            SignInResult.NOT_ADMIN
        }
    }

    override fun signOut() = remoteDataSource.signOut()

    private suspend fun registerAccount(user: AuthUser) {
        if (user.isAnonymous || user.email.isBlank()) return
        runCatching { accountIndex.register(user.uid, user.email) }
    }
}
