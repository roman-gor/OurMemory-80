package com.gorman.ourmemoryapp.data.accounts.datasource.remote

import com.google.firebase.database.DatabaseReference
import com.gorman.ourmemoryapp.data.firebase.AccountKeys
import com.gorman.ourmemoryapp.data.firebase.DatabaseNodes
import com.gorman.ourmemoryapp.di.annotation.MemoryRoot
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AccountIndexRemoteDataSourceImpl @Inject constructor(
    @param:MemoryRoot private val root: DatabaseReference
) : AccountIndexRemoteDataSource {

    override suspend fun register(uid: String, email: String) {
        root.child(DatabaseNodes.ACCOUNTS)
            .child(AccountKeys.forEmail(email))
            .setValue(mapOf(UID to uid, EMAIL to email))
            .await()
    }

    companion object {
        private const val UID = "uid"
        private const val EMAIL = "email"
    }
}
