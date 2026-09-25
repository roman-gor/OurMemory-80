package com.gorman.ourmemoryapp.data.accounts.datasource.remote

interface AccountIndexRemoteDataSource {
    suspend fun register(uid: String, email: String)
}
