package com.gorman.ourmemoryapp.data.auth.datasource.remote

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AnonymousSession @Inject constructor(
    private val auth: FirebaseAuth
) {
    suspend fun ensureSignedIn(): String {
        val user = auth.currentUser ?: requireNotNull(auth.signInAnonymously().await().user)
        return user.uid
    }
}
