package com.gorman.ourmemoryapp.data.datasource

import com.google.firebase.database.DatabaseReference
import com.gorman.ourmemoryapp.domain.models.Veteran
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FirebaseDBImpl @Inject constructor(
    private val _dbRef: DatabaseReference
): FirebaseDB {
    override suspend fun getAllVeterans(): List<Veteran> = suspendCoroutine { continuation ->
        _dbRef.get()
            .addOnSuccessListener { snapshot ->
                val veteran = snapshot.children.mapNotNull { it.getValue(Veteran::class.java) }
                continuation.resume(veteran)
            }
            .addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
    }
}