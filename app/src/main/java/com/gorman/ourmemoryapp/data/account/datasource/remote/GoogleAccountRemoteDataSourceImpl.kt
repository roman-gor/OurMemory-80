package com.gorman.ourmemoryapp.data.account.datasource.remote

import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.gorman.ourmemoryapp.data.auth.datasource.remote.observeCurrentUser
import com.gorman.ourmemoryapp.domain.models.VisitorAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class GoogleAccountRemoteDataSourceImpl @Inject constructor(
    private val auth: FirebaseAuth
) : GoogleAccountRemoteDataSource {

    private val accountVersion = MutableStateFlow(0)

    override fun observeAccount() = combine(auth.observeCurrentUser(), accountVersion) { user, _ ->
        user?.toVisitorAccount()
    }.distinctUntilChanged()

    override suspend fun signInWithGoogle(idToken: String): VisitorAccount? {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val current = auth.currentUser
        val user = try {
            if (current != null && current.isAnonymous) {
                current.linkWithCredential(credential).await().user
            } else {
                auth.signInWithCredential(credential).await().user
            }
        } catch (collision: FirebaseAuthUserCollisionException) {
            auth.signInWithCredential(collision.updatedCredential ?: credential).await().user
        } catch (_: FirebaseException) {
            null
        }
        runCatching { user?.getIdToken(true)?.await() }
        accountVersion.update { it + 1 }
        return user?.toVisitorAccount()
    }

    override fun signOut() = auth.signOut()

    private fun FirebaseUser.toVisitorAccount(): VisitorAccount? {
        val google = providerData.firstOrNull { it.providerId == GoogleAuthProvider.PROVIDER_ID } ?: return null
        return VisitorAccount(
            uid = uid,
            name = displayName.orEmpty().ifBlank { google.displayName.orEmpty() },
            email = email.orEmpty().ifBlank { google.email.orEmpty() },
            photoUrl = (photoUrl ?: google.photoUrl)?.toString().orEmpty()
        )
    }
}
