package com.gorman.ourmemoryapp.data.auth.datasource.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.DatabaseReference
import com.gorman.ourmemoryapp.data.auth.mapper.toAuthUser
import com.gorman.ourmemoryapp.data.auth.model.AuthUser
import com.gorman.ourmemoryapp.data.firebase.DatabaseNodes
import com.gorman.ourmemoryapp.data.firebase.observeValue
import com.gorman.ourmemoryapp.di.annotation.MemoryRoot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRemoteDataSourceImpl @Inject constructor(
    private val auth: FirebaseAuth,
    @param:MemoryRoot private val root: DatabaseReference
) : AuthRemoteDataSource {

    override fun observeUser(): Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.toAuthUser()) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override fun observeIsAdmin(uid: String) = adminReference(uid).observeValue()
        .map { it.exists() }
        .catch { emit(false) }

    override suspend fun isAdmin(uid: String) = adminReference(uid).get().await().exists()

    override suspend fun signIn(email: String, password: String): AuthUser? = try {
        auth.signInWithEmailAndPassword(email, password).await().user?.toAuthUser()
    } catch (_: FirebaseAuthInvalidCredentialsException) {
        null
    } catch (_: FirebaseAuthInvalidUserException) {
        null
    }

    override fun signOut() = auth.signOut()

    private fun adminReference(uid: String) = root.child(DatabaseNodes.ADMINS).child(uid)
}
