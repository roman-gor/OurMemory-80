package com.gorman.ourmemoryapp.data.admins.datasource.remote

import kotlinx.coroutines.flow.Flow

interface AdminsRemoteDataSource {
    fun observeAdminUids(): Flow<Set<String>>
    fun observeSuperAdminUids(): Flow<Set<String>>
    fun observeAccountEmails(): Flow<Map<String, String>>
    suspend fun findUid(email: String): String?
    suspend fun isAdmin(uid: String): Boolean
    suspend fun setAdmin(uid: String)
    suspend fun removeAdmin(uid: String)
}
