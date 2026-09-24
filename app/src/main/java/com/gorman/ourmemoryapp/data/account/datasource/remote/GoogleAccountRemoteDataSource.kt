package com.gorman.ourmemoryapp.data.account.datasource.remote

import com.gorman.ourmemoryapp.domain.models.VisitorAccount
import kotlinx.coroutines.flow.Flow

interface GoogleAccountRemoteDataSource {
    fun observeAccount(): Flow<VisitorAccount?>
    suspend fun signInWithGoogle(idToken: String): VisitorAccount?
    fun signOut()
}
