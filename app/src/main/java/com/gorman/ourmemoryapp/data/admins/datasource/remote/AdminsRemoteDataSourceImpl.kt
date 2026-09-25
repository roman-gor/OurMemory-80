package com.gorman.ourmemoryapp.data.admins.datasource.remote

import com.google.firebase.database.DatabaseReference
import com.gorman.ourmemoryapp.data.firebase.AccountKeys
import com.gorman.ourmemoryapp.data.firebase.DatabaseNodes
import com.gorman.ourmemoryapp.data.firebase.observeValue
import com.gorman.ourmemoryapp.di.annotation.MemoryRoot
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AdminsRemoteDataSourceImpl @Inject constructor(
    @param:MemoryRoot private val root: DatabaseReference
) : AdminsRemoteDataSource {

    private val admins = root.child(DatabaseNodes.ADMINS)

    override fun observeAdminUids() = admins.observeValue().map { snapshot ->
        snapshot.children.mapNotNull { it.key }.toSet()
    }

    override fun observeSuperAdminUids() = root.child(DatabaseNodes.SUPER_ADMINS).observeValue().map { snapshot ->
        snapshot.children.mapNotNull { it.key }.toSet()
    }

    override fun observeAccountEmails() = root.child(DatabaseNodes.ACCOUNTS).observeValue().map { snapshot ->
        snapshot.children.mapNotNull { account ->
            val uid = account.child(UID).getValue(String::class.java) ?: return@mapNotNull null
            uid to account.child(EMAIL).getValue(String::class.java).orEmpty()
        }.toMap()
    }

    override suspend fun findUid(email: String): String? = root.child(DatabaseNodes.ACCOUNTS)
        .child(AccountKeys.forEmail(email))
        .child(UID)
        .get()
        .await()
        .getValue(String::class.java)

    override suspend fun isAdmin(uid: String) = admins.child(uid).get().await().exists()

    override suspend fun setAdmin(uid: String) {
        admins.child(uid).setValue(true).await()
    }

    override suspend fun removeAdmin(uid: String) {
        admins.child(uid).removeValue().await()
    }

    companion object {
        private const val UID = "uid"
        private const val EMAIL = "email"
    }
}
