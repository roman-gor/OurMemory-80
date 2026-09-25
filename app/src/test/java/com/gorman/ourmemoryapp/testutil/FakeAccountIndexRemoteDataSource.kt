package com.gorman.ourmemoryapp.testutil

import com.gorman.ourmemoryapp.data.accounts.datasource.remote.AccountIndexRemoteDataSource

class FakeAccountIndexRemoteDataSource : AccountIndexRemoteDataSource {
    val accounts = mutableMapOf<String, String>()

    override suspend fun register(uid: String, email: String) {
        accounts[email] = uid
    }
}
