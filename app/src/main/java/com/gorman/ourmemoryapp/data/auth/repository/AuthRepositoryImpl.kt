package com.gorman.ourmemoryapp.data.auth.repository

import com.gorman.ourmemoryapp.data.auth.datasource.remote.AuthRemoteDataSource
import com.gorman.ourmemoryapp.domain.models.AdminSession
import com.gorman.ourmemoryapp.domain.models.SignInResult
import com.gorman.ourmemoryapp.domain.repository.AuthRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val remoteDataSource: AuthRemoteDataSource
) : AuthRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeSession(): Flow<AdminSession> = remoteDataSource.observeUser().flatMapLatest { user ->
        if (user == null || user.isAnonymous) {
            flowOf(AdminSession())
        } else {
            remoteDataSource.observeIsAdmin(user.uid).map { isAdmin ->
                AdminSession(email = user.email, isAdmin = isAdmin)
            }
        }
    }

    override suspend fun signIn(email: String, password: String): SignInResult {
        val user = remoteDataSource.signIn(email, password)
        return when {
            user == null -> SignInResult.WRONG_CREDENTIALS
            remoteDataSource.isAdmin(user.uid) -> SignInResult.ADMIN
            else -> {
                remoteDataSource.signOut()
                SignInResult.NOT_ADMIN
            }
        }
    }

    override fun signOut() = remoteDataSource.signOut()
}
