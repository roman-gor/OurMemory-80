package com.gorman.ourmemoryapp.ui.more.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.more.models.MoreUiIntent
import com.gorman.ourmemoryapp.ui.more.models.SignInStatus
import kotlinx.coroutines.launch

@Composable
fun rememberGoogleSignInAction(onUiIntent: (MoreUiIntent) -> Unit): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val clientId = stringResource(R.string.default_web_client_id)
    return remember(context, clientId, onUiIntent) {
        {
            onUiIntent(MoreUiIntent.OnGoogleSignInStarted)
            scope.launch {
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(GetSignInWithGoogleOption.Builder(clientId).build())
                    .build()
                try {
                    val credential = credentialManager.getCredential(context, request).credential
                    val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    onUiIntent(MoreUiIntent.OnGoogleIdToken(idToken))
                } catch (_: GetCredentialCancellationException) {
                    onUiIntent(MoreUiIntent.OnGoogleSignInFailed(SignInStatus.IDLE))
                } catch (_: GetCredentialException) {
                    onUiIntent(MoreUiIntent.OnGoogleSignInFailed(SignInStatus.FAILED))
                } catch (_: GoogleIdTokenParsingException) {
                    onUiIntent(MoreUiIntent.OnGoogleSignInFailed(SignInStatus.FAILED))
                }
            }
        }
    }
}
