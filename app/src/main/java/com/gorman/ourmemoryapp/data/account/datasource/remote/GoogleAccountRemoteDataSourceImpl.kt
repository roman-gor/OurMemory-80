package com.gorman.ourmemoryapp.data.account.datasource.remote

import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.gorman.ourmemoryapp.data.auth.datasource.remote.observeCurrentUser
import com.gorman.ourmemoryapp.domain.models.VisitorAccount
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class GoogleAccountRemoteDataSourceImpl @Inject constructor(
    private val auth: FirebaseAuth
) : GoogleAccountRemoteDataSource {

    override fun observeAccount() = auth.observeCurrentUser().map { it?.toVisitorAccount() }

    override suspend fun signInWithGoogle(idToken: String): VisitorAccount? {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val current = auth.currentUser
        return try {
            val result = if (current != null && current.isAnonymous) {
                current.linkWithCredential(credential).await()
            } else {
                auth.signInWithCredential(credential).await()
            }
            result.user?.toVisitorAccount()
        } catch (collision: FirebaseAuthUserCollisionException) {
            auth.signInWithCredential(collision.updatedCredential ?: credential).await().user?.toVisitorAccount()
        } catch (_: FirebaseException) {
            null
        }
    }

    override fun signOut() = auth.signOut()

    private fun FirebaseUser.toVisitorAccount(): VisitorAccount? {
        val isGoogleAccount = !isAnonymous && providerData.any { it.providerId == GoogleAuthProvider.PROVIDER_ID }
        if (!isGoogleAccount) return null
        return VisitorAccount(
            uid = uid,
            name = displayName.orEmpty(),
            email = email.orEmpty(),
            photoUrl = photoUrl?.toString().orEmpty()
        )
    }
}
