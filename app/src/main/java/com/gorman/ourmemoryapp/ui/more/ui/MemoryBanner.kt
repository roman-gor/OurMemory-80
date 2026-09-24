package com.gorman.ourmemoryapp.ui.more.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.theme.MemoryRed
import com.gorman.ourmemoryapp.ui.theme.MemoryRedBright

@Composable
fun MemoryBanner(modifier: Modifier = Modifier) {
    val flameAlpha by rememberInfiniteTransition(label = "flame").animateFloat(
        initialValue = FLAME_MIN_ALPHA,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(FLAME_PULSE_MILLIS), repeatMode = RepeatMode.Reverse),
        label = "flameAlpha"
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BANNER_SPACING),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(BANNER_CORNER_RADIUS))
            .background(Brush.linearGradient(listOf(MemoryRed, MemoryRedBright)))
            .padding(BANNER_PADDING)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(FLAME_CONTAINER_SIZE)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = FLAME_BACKGROUND_ALPHA))
        ) {
            Icon(
                painter = painterResource(R.drawable.flame),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(FLAME_SIZE)
                    .alpha(flameAlpha)
            )
        }
        Column {
            Text(
                text = stringResource(R.string.never_forgotten_msg),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = stringResource(R.string.thank_you_for_keeping_memory_msg),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = SUBTITLE_ALPHA)
            )
        }
    }
}

private val BANNER_SPACING = 16.dp
private val BANNER_CORNER_RADIUS = 24.dp
private val BANNER_PADDING = 20.dp
private val FLAME_CONTAINER_SIZE = 52.dp
private val FLAME_SIZE = 28.dp
private const val FLAME_MIN_ALPHA = 0.6f
private const val FLAME_PULSE_MILLIS = 1400
private const val FLAME_BACKGROUND_ALPHA = 0.16f
private const val SUBTITLE_ALPHA = 0.85f
