package com.gorman.ourmemoryapp.domain.repository

import com.gorman.ourmemoryapp.domain.models.AdminSession
import com.gorman.ourmemoryapp.domain.models.SignInResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeSession(): Flow<AdminSession>
    suspend fun signIn(email: String, password: String): SignInResult
    fun signOut()
}
