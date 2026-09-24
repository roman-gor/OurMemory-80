package com.gorman.ourmemoryapp.ui.admin.login.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.admin.login.models.AdminLoginUiIntent
import com.gorman.ourmemoryapp.ui.admin.login.models.AdminLoginUiState
import com.gorman.ourmemoryapp.ui.admin.login.viewmodels.AdminLoginViewModel
import com.gorman.ourmemoryapp.ui.common.ui.FloatingTopBar

@Composable
fun AdminLoginScreen(
    onBackClick: () -> Unit,
    onSignedIn: () -> Unit,
    adminLoginViewModel: AdminLoginViewModel = hiltViewModel()
) {
    val state by adminLoginViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSignedIn) {
        if (state.isSignedIn) onSignedIn()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AdminLoginForm(state = state, onUiIntent = adminLoginViewModel::onUiIntent)
        FloatingTopBar(
            title = stringResource(R.string.sign_in_as_admin),
            isCollapsed = true,
            onBackClick = onBackClick,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun AdminLoginForm(
    state: AdminLoginUiState,
    onUiIntent: (AdminLoginUiIntent) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.statusBarsPadding().height(TOP_BAR_HEIGHT))
        Text(
            text = stringResource(R.string.sign_in_as_admin),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        OutlinedTextField(
            value = state.email,
            onValueChange = { onUiIntent(AdminLoginUiIntent.OnEmailChange(it)) },
            label = { Text(text = stringResource(R.string.email)) },
            enabled = !state.isSigningIn,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.password,
            onValueChange = { onUiIntent(AdminLoginUiIntent.OnPasswordChange(it)) },
            label = { Text(text = stringResource(R.string.password)) },
            enabled = !state.isSigningIn,
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onUiIntent(AdminLoginUiIntent.OnSignInClick) }),
            modifier = Modifier.fillMaxWidth()
        )
        state.error?.let { error ->
            Text(
                text = stringResource(error.messageRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
        Button(
            onClick = { onUiIntent(AdminLoginUiIntent.OnSignInClick) },
            enabled = state.canSignIn,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (state.isSigningIn) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(text = stringResource(R.string.sign_in))
            }
        }
    }
}

private val TOP_BAR_HEIGHT = 56.dp
