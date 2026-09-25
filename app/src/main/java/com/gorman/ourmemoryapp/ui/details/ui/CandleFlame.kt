package com.gorman.ourmemoryapp.ui.details.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.gorman.ourmemoryapp.R

@Composable
fun CandleFlame(isLit: Boolean, modifier: Modifier = Modifier) {
    val containerColor by animateColorAsState(
        targetValue = if (isLit) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primaryContainer,
        label = "flameContainer"
    )
    val iconColor by animateColorAsState(
        targetValue = if (isLit) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.primary,
        label = "flameIcon"
    )
    val breath by rememberInfiniteTransition(label = "flame").animateFloat(
        initialValue = 1f,
        targetValue = if (isLit) BREATH_SCALE else 1f,
        animationSpec = infiniteRepeatable(animation = tween(BREATH_MILLIS), repeatMode = RepeatMode.Reverse),
        label = "flameScale"
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(containerColor)
    ) {
        Icon(
            painter = painterResource(R.drawable.flame),
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.graphicsLayer {
                scaleX = breath
                scaleY = breath
            }
        )
    }
}

private const val BREATH_SCALE = 1.12f
private const val BREATH_MILLIS = 1200
