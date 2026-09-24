package com.gorman.ourmemoryapp.testutil

import com.gorman.ourmemoryapp.data.auth.datasource.remote.AuthRemoteDataSource
import com.gorman.ourmemoryapp.data.auth.model.AuthUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeAuthRemoteDataSource(
    private val accounts: Map<String, AuthUser> = emptyMap(),
    adminUids: Set<String> = emptySet()
) : AuthRemoteDataSource {

    val user = MutableStateFlow<AuthUser?>(null)
    val admins = MutableStateFlow(adminUids)

    override fun observeUser() = user

    override fun observeIsAdmin(uid: String) = admins.map { uid in it }

    override suspend fun isAdmin(uid: String) = uid in admins.value

    override suspend fun signIn(email: String, password: String): AuthUser? =
        accounts[email]?.takeIf { password == PASSWORD }?.also { user.value = it }

    override fun signOut() {
        user.value = null
    }

    companion object {
        const val PASSWORD = "secret"
    }
}
