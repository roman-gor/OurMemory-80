package com.gorman.ourmemoryapp.domain.repository

import com.gorman.ourmemoryapp.domain.models.GoogleSignInResult
import com.gorman.ourmemoryapp.domain.models.VisitorAccount
import kotlinx.coroutines.flow.Flow

interface VisitorAccountRepository {
    fun observeAccount(): Flow<VisitorAccount?>
    suspend fun signInWithGoogle(idToken: String): GoogleSignInResult
    fun signOut()
}
