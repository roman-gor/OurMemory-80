package com.gorman.ourmemoryapp.ui.common.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.gorman.ourmemoryapp.R

@Composable
fun FloatingTopBar(
    title: String,
    isCollapsed: Boolean,
    onBackClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val containerColor by animateColorAsState(
        targetValue = if (isCollapsed) {
            MaterialTheme.colorScheme.background.copy(alpha = COLLAPSED_ALPHA)
        } else {
            Color.Transparent
        },
        label = "topBarContainer"
    )
    val titleAlpha by animateFloatAsState(
        targetValue = if (isCollapsed) 1f else 0f,
        label = "topBarTitle"
    )
    var actionsWidthPx by remember { mutableIntStateOf(0) }
    val titleInset = max(TITLE_MIN_INSET, with(LocalDensity.current) { actionsWidthPx.toDp() } + TITLE_ACTIONS_GAP)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = 12.dp)
        ) {
            onBackClick?.let { onClick ->
                CircleIconButton(
                    painter = painterResource(R.drawable.chevron_left),
                    contentDescription = stringResource(R.string.back),
                    onClick = onClick,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = titleInset)
                    .graphicsLayer { alpha = titleAlpha }
            )
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .onSizeChanged { actionsWidthPx = it.width },
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = actions
            )
        }
        AnimatedVisibility(visible = isCollapsed, enter = fadeIn(), exit = fadeOut()) {
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

private const val COLLAPSED_ALPHA = 0.97f
private val TITLE_MIN_INSET = 60.dp
private val TITLE_ACTIONS_GAP = 12.dp
