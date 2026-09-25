package com.gorman.ourmemoryapp.data.auth.datasource.remote

import com.gorman.ourmemoryapp.data.auth.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRemoteDataSource {
    fun observeUser(): Flow<AuthUser?>
    fun observeIsAdmin(uid: String): Flow<Boolean>
    fun observeIsSuperAdmin(uid: String): Flow<Boolean>
    suspend fun isAdmin(uid: String): Boolean
    suspend fun signIn(email: String, password: String): AuthUser?
    fun signOut()
}
