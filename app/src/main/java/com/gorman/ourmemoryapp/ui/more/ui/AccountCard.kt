package com.gorman.ourmemoryapp.ui.more.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.domain.models.VisitorAccount
import com.gorman.ourmemoryapp.ui.more.models.SignInStatus

@Composable
fun AccountCard(
    account: VisitorAccount?,
    signInStatus: SignInStatus,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(CARD_CORNER_RADIUS),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier.fillMaxWidth()
    ) {
        if (account == null) {
            SignedOutContent(signInStatus = signInStatus, onSignInClick = onSignInClick)
        } else {
            SignedInContent(account = account, onSignOutClick = onSignOutClick)
        }
    }
}

@Composable
private fun SignedOutContent(signInStatus: SignInStatus, onSignInClick: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(CONTENT_SPACING),
        modifier = Modifier.padding(CARD_PADDING)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CONTENT_SPACING)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(AVATAR_SIZE)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Icon(
                    painter = painterResource(R.drawable.cloud_done),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.keep_your_memory_safe),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.sign_in_to_keep_favorites_msg),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        OutlinedButton(
            onClick = onSignInClick,
            enabled = signInStatus != SignInStatus.IN_PROGRESS,
            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (signInStatus == SignInStatus.IN_PROGRESS) {
                CircularProgressIndicator(strokeWidth = PROGRESS_STROKE, modifier = Modifier.size(GOOGLE_ICON_SIZE))
            } else {
                Image(
                    painter = painterResource(R.drawable.google),
                    contentDescription = null,
                    modifier = Modifier.size(GOOGLE_ICON_SIZE)
                )
            }
            Text(
                text = stringResource(R.string.sign_in_with_google),
                modifier = Modifier.padding(start = BUTTON_ICON_SPACING)
            )
        }
        signInStatus.errorRes?.let {
            Text(
                text = stringResource(it),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun SignedInContent(account: VisitorAccount, onSignOutClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CONTENT_SPACING),
        modifier = Modifier.padding(CARD_PADDING)
    ) {
        AsyncImage(
            model = account.photoUrl.ifBlank { null },
            contentDescription = null,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.portrait_placeholder),
            error = painterResource(R.drawable.portrait_placeholder),
            fallback = painterResource(R.drawable.portrait_placeholder),
            modifier = Modifier
                .size(AVATAR_SIZE)
                .clip(CircleShape)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = account.name.ifBlank { account.email },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.favorites_and_requests_are_saved_msg),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        TextButton(onClick = onSignOutClick) {
            Text(text = stringResource(R.string.sign_out))
        }
    }
}

private val SignInStatus.errorRes
    get() = when (this) {
        SignInStatus.FAILED -> R.string.could_not_sign_in_msg
        SignInStatus.NOT_CONFIGURED -> R.string.google_sign_in_not_configured_msg
        SignInStatus.IDLE, SignInStatus.IN_PROGRESS -> null
    }

private val CARD_CORNER_RADIUS = 24.dp
private val CARD_PADDING = 16.dp
private val CONTENT_SPACING = 12.dp
private val AVATAR_SIZE = 48.dp
private val GOOGLE_ICON_SIZE = 18.dp
private val BUTTON_ICON_SPACING = 8.dp
private val PROGRESS_STROKE = 2.dp
