package com.gorman.ourmemoryapp.data.candles.datasource.remote

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.gorman.ourmemoryapp.data.firebase.DatabaseNodes
import com.gorman.ourmemoryapp.data.firebase.observeValue
import com.gorman.ourmemoryapp.di.annotation.MemoryRoot
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CandlesRemoteDataSourceImpl @Inject constructor(
    @param:MemoryRoot private val root: DatabaseReference
) : CandlesRemoteDataSource {

    override fun observeCandles(veteranId: String) = root.child(DatabaseNodes.CANDLES).child(veteranId)
        .observeValue()
        .map { it.value.toCandleCount() }

    override suspend fun lightCandle(veteranId: String) = suspendCancellableCoroutine { continuation ->
        root.child(DatabaseNodes.CANDLES).child(veteranId).runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                currentData.value = currentData.value.toCandleCount() + 1
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                if (error != null) {
                    continuation.resumeWithException(error.toException())
                } else {
                    continuation.resume(Unit)
                }
            }
        })
    }

    private fun Any?.toCandleCount(): Long = when (this) {
        is Number -> toLong()
        is String -> toLongOrNull() ?: 0L
        else -> 0L
    }
}
