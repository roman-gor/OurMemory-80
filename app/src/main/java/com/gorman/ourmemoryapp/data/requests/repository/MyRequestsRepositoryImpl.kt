package com.gorman.ourmemoryapp.data.requests.repository

import com.gorman.ourmemoryapp.data.requests.datasource.local.RequestsSeenLocalDataSource
import com.gorman.ourmemoryapp.data.requests.datasource.remote.MyRequestsRemoteDataSource
import com.gorman.ourmemoryapp.data.requests.mapper.toMyRequest
import com.gorman.ourmemoryapp.domain.models.MyRequest
import com.gorman.ourmemoryapp.domain.repository.MyRequestsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.time.Clock
import javax.inject.Inject

class MyRequestsRepositoryImpl @Inject constructor(
    private val remoteDataSource: MyRequestsRemoteDataSource,
    private val seenLocalDataSource: RequestsSeenLocalDataSource,
    private val clock: Clock
) : MyRequestsRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeMyRequests(): Flow<List<MyRequest>> =
        remoteDataSource.observeCurrentUid().flatMapLatest { uid ->
            if (uid == null) flowOf(emptyList()) else observeRequestsOf(uid)
        }

    private fun observeRequestsOf(uid: String) = combine(
        remoteDataSource.observeSubmissions(uid),
        remoteDataSource.observeFeedback(uid)
    ) { submissions, feedback ->
        (submissions.map { it.toMyRequest() } + feedback.map { it.toMyRequest() })
            .filter { it.id.isNotBlank() }
            .sortedByDescending { it.createdAt }
    }

    override fun observeUnseenCount() = combine(
        observeMyRequests().catch { emit(emptyList()) },
        seenLocalDataSource.observeSeenAt()
    ) { requests, seenAt -> requests.count { it.reviewedAt > seenAt } }

    override suspend fun markAllSeen() {
        seenLocalDataSource.saveSeenAt(clock.millis())
    }
}
