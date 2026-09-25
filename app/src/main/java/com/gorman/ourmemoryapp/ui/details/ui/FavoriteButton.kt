package com.gorman.ourmemoryapp.ui.details.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.gorman.ourmemoryapp.R
import com.gorman.ourmemoryapp.ui.common.ui.CircleIconButton
import kotlinx.coroutines.launch

@Composable
fun FavoriteButton(isFavorite: Boolean, onClick: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    CircleIconButton(
        painter = painterResource(if (isFavorite) R.drawable.favorite else R.drawable.favorite_border),
        contentDescription = stringResource(
            if (isFavorite) R.string.remove_from_favorites else R.string.add_to_favorites
        ),
        onClick = {
            haptics.performHapticFeedback(if (isFavorite) HapticFeedbackType.ToggleOff else HapticFeedbackType.ToggleOn)
            if (!isFavorite) {
                scope.launch {
                    scale.animateTo(POP_SCALE, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                    scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                }
            }
            onClick()
        },
        contentColor = MaterialTheme.colorScheme.primary,
        modifier = Modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        }
    )
}

private const val POP_SCALE = 1.25f
