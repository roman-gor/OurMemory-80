package com.gorman.ourmemoryapp.ui.common.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun rememberPlaceholderAlpha(): State<Float> = rememberInfiniteTransition(label = "placeholder").animateFloat(
    initialValue = MIN_ALPHA,
    targetValue = MAX_ALPHA,
    animationSpec = infiniteRepeatable(animation = tween(PULSE_MILLIS), repeatMode = RepeatMode.Reverse),
    label = "placeholderAlpha"
)

fun Modifier.placeholder(
    color: Color,
    alpha: () -> Float,
    shape: Shape = PlaceholderShape
) = clip(shape).drawBehind { drawRect(color = color, alpha = alpha()) }

private val PlaceholderShape = RoundedCornerShape(8.dp)
private const val MIN_ALPHA = 0.4f
private const val MAX_ALPHA = 1f
private const val PULSE_MILLIS = 800
