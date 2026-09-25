package com.gorman.ourmemoryapp.domain.repository

import com.gorman.ourmemoryapp.domain.models.AddAdminResult
import com.gorman.ourmemoryapp.domain.models.AdminAccount
import kotlinx.coroutines.flow.Flow

interface AdminsRepository {
    fun observeAdmins(): Flow<List<AdminAccount>>
    suspend fun addAdmin(email: String): AddAdminResult
    suspend fun removeAdmin(uid: String)
}
