package com.gorman.ourmemoryapp.testutil

import com.gorman.ourmemoryapp.domain.models.AdminSession
import com.gorman.ourmemoryapp.domain.models.SignInResult
import com.gorman.ourmemoryapp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAuthRepository(
    private val result: SignInResult = SignInResult.ADMIN,
    private val error: Throwable? = null
) : AuthRepository {

    val session = MutableStateFlow(AdminSession())
    val signInAttempts = mutableListOf<Pair<String, String>>()

    override fun observeSession() = session

    override suspend fun signIn(email: String, password: String): SignInResult {
        signInAttempts += email to password
        error?.let { throw it }
        return result
    }

    override fun signOut() {
        session.value = AdminSession()
    }
}
