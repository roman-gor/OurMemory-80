package com.gorman.ourmemoryapp.data.candles.datasource.remote

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CandlesRemoteDataSourceImpl @Inject constructor(
    private val database: FirebaseDatabase
) : CandlesRemoteDataSource {

    override fun observeCandles(veteranId: String): Flow<Long> = callbackFlow {
        val reference = database.getReference(CANDLES_PATH).child(veteranId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(Long::class.java) ?: 0L)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        reference.addValueEventListener(listener)
        awaitClose { reference.removeEventListener(listener) }
    }

    override suspend fun lightCandle(veteranId: String) = suspendCancellableCoroutine { continuation ->
        database.getReference(CANDLES_PATH).child(veteranId).runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                currentData.value = (currentData.getValue(Long::class.java) ?: 0L) + 1
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

    companion object {
        private const val CANDLES_PATH = "Veterans/Candles"
    }
}
