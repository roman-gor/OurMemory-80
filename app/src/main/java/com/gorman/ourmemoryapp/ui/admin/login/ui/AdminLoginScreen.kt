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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.admin.login.models.AdminLoginUiIntent
import com.gorman.ourmemoryapp.ui.admin.login.models.AdminLoginUiState
import com.gorman.ourmemoryapp.ui.admin.login.viewmodels.AdminLoginViewModel
import com.gorman.ourmemoryapp.ui.common.ui.FloatingTopBar
import com.gorman.ourmemoryapp.ui.common.ui.bottomBarContentPadding

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
        verticalArrangement = Arrangement.spacedBy(FORM_SPACING),
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = SCREEN_PADDING)
    ) {
        Spacer(modifier = Modifier.statusBarsPadding().height(TOP_BAR_HEIGHT))
        LoginHero()
        Surface(
            shape = RoundedCornerShape(FORM_CORNER_RADIUS),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(FIELD_SPACING),
                modifier = Modifier.padding(FORM_PADDING)
            ) {
                LoginFields(state = state, onUiIntent = onUiIntent)
            }
        }
        Button(
            onClick = { onUiIntent(AdminLoginUiIntent.OnSignInClick) },
            enabled = state.canSignIn,
            modifier = Modifier
                .fillMaxWidth()
                .height(BUTTON_HEIGHT)
        ) {
            if (state.isSigningIn) {
                CircularProgressIndicator(
                    strokeWidth = PROGRESS_STROKE,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(PROGRESS_SIZE)
                )
            } else {
                Text(text = stringResource(R.string.sign_in))
            }
        }
        Spacer(modifier = Modifier.height(bottomBarContentPadding()))
    }
}

@Composable
private fun LoginHero() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(HERO_SPACING),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = HERO_VERTICAL_PADDING)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(HERO_ICON_CONTAINER_SIZE)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Icon(
                painter = painterResource(R.drawable.shield),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(HERO_ICON_SIZE)
            )
        }
        Text(
            text = stringResource(R.string.administration),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.for_cemetery_editors_msg),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LoginFields(
    state: AdminLoginUiState,
    onUiIntent: (AdminLoginUiIntent) -> Unit
) {
    OutlinedTextField(
        value = state.email,
        onValueChange = { onUiIntent(AdminLoginUiIntent.OnEmailChange(it)) },
        label = { Text(text = stringResource(R.string.email)) },
        enabled = !state.isSigningIn,
        singleLine = true,
        shape = RoundedCornerShape(FIELD_CORNER_RADIUS),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = state.password,
        onValueChange = { onUiIntent(AdminLoginUiIntent.OnPasswordChange(it)) },
        label = { Text(text = stringResource(R.string.password)) },
        enabled = !state.isSigningIn,
        singleLine = true,
        shape = RoundedCornerShape(FIELD_CORNER_RADIUS),
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
}

private val TOP_BAR_HEIGHT = 56.dp
private val SCREEN_PADDING = 16.dp
private val FORM_SPACING = 16.dp
private val FORM_CORNER_RADIUS = 24.dp
private val FORM_PADDING = 16.dp
private val FIELD_SPACING = 12.dp
private val FIELD_CORNER_RADIUS = 14.dp
private val BUTTON_HEIGHT = 52.dp
private val PROGRESS_STROKE = 2.dp
private val PROGRESS_SIZE = 20.dp
private val HERO_SPACING = 8.dp
private val HERO_VERTICAL_PADDING = 8.dp
private val HERO_ICON_CONTAINER_SIZE = 72.dp
private val HERO_ICON_SIZE = 36.dp
