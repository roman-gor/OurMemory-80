package com.gorman.ourmemoryapp

import com.google.firebase.Firebase
import com.google.firebase.database.database
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FirebaseDB{

    private val _dbRef = Firebase.database.getReference("Veterans")

    suspend fun getAllVeterans(): List<Veteran> = suspendCoroutine { continuation ->
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