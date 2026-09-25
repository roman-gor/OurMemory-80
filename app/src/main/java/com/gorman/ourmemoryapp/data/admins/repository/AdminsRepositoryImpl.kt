package com.gorman.ourmemoryapp.data.admins.repository

import com.gorman.ourmemoryapp.data.admins.datasource.remote.AdminsRemoteDataSource
import com.gorman.ourmemoryapp.domain.models.AddAdminResult
import com.gorman.ourmemoryapp.domain.models.AdminAccount
import com.gorman.ourmemoryapp.domain.repository.AdminsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class AdminsRepositoryImpl @Inject constructor(
    private val remoteDataSource: AdminsRemoteDataSource
) : AdminsRepository {

    override fun observeAdmins(): Flow<List<AdminAccount>> = combine(
        remoteDataSource.observeAdminUids(),
        remoteDataSource.observeSuperAdminUids(),
        remoteDataSource.observeAccountEmails().onStart { emit(emptyMap()) }.catch { emit(emptyMap()) }
    ) { adminUids, superAdminUids, emails ->
        (adminUids + superAdminUids)
            .map { uid -> AdminAccount(uid = uid, email = emails[uid].orEmpty(), isSuperAdmin = uid in superAdminUids) }
            .sortedWith(compareBy({ !it.isSuperAdmin }, { it.email.ifBlank { it.uid } }))
    }

    override suspend fun addAdmin(email: String): AddAdminResult {
        val uid = remoteDataSource.findUid(email) ?: return AddAdminResult.ACCOUNT_NOT_FOUND
        if (remoteDataSource.isAdmin(uid)) return AddAdminResult.ALREADY_ADMIN
        remoteDataSource.setAdmin(uid)
        return AddAdminResult.ADDED
    }

    override suspend fun removeAdmin(uid: String) = remoteDataSource.removeAdmin(uid)
}
