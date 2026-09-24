package com.gorman.ourmemoryapp.domain.repository

import com.gorman.ourmemoryapp.domain.models.MyRequest
import kotlinx.coroutines.flow.Flow

interface MyRequestsRepository {
    fun observeMyRequests(): Flow<List<MyRequest>>
    fun observeUnseenCount(): Flow<Int>
    suspend fun markAllSeen()
}
