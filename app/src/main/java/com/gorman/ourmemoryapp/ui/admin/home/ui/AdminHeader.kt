package com.gorman.ourmemoryapp.ui.admin.home.ui

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.theme.MemoryRed
import com.gorman.ourmemoryapp.ui.theme.MemoryRedBright

@Composable
fun AdminHeader(
    email: String,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(HEADER_SPACING),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(HEADER_CORNER_RADIUS))
            .background(Brush.linearGradient(listOf(MemoryRed, MemoryRedBright)))
            .padding(HEADER_PADDING)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.shield),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(TITLE_ICON_SIZE)
            )
            Text(
                text = stringResource(R.string.administration),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = TITLE_ICON_SPACING)
            )
            IconButton(onClick = onSignOutClick) {
                Icon(
                    painter = painterResource(R.drawable.logout),
                    contentDescription = stringResource(R.string.sign_out),
                    tint = Color.White
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AVATAR_SPACING)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(AVATAR_SIZE)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = AVATAR_BACKGROUND_ALPHA))
            ) {
                Text(
                    text = email.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Text(
                text = stringResource(R.string.signed_in_as, email),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = SUBTITLE_ALPHA),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private val HEADER_SPACING = 12.dp
private val HEADER_CORNER_RADIUS = 28.dp
private val HEADER_PADDING = 20.dp
private val TITLE_ICON_SIZE = 28.dp
private val TITLE_ICON_SPACING = 12.dp
private val AVATAR_SIZE = 36.dp
private val AVATAR_SPACING = 12.dp
private const val AVATAR_BACKGROUND_ALPHA = 0.2f
private const val SUBTITLE_ALPHA = 0.9f
