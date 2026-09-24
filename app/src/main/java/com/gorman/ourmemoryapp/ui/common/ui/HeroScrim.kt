package com.gorman.ourmemoryapp.ui.common.ui

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val heroScrim = Brush.verticalGradient(
    0f to Color.Black.copy(alpha = TOP_SCRIM_ALPHA),
    TOP_SCRIM_END to Color.Transparent,
    BOTTOM_SCRIM_START to Color.Transparent,
    1f to Color.Black.copy(alpha = BOTTOM_SCRIM_ALPHA)
)

private const val TOP_SCRIM_ALPHA = 0.45f
private const val TOP_SCRIM_END = 0.25f
private const val BOTTOM_SCRIM_START = 0.5f
private const val BOTTOM_SCRIM_ALPHA = 0.8f
