package com.gorman.ourmemoryapp.ui.common.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun rememberIsHeroScrolledAway(listState: LazyListState): State<Boolean> {
    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.getTop(density)
    val topBarHeight = with(density) { TOP_BAR_HEIGHT.roundToPx() } + statusBarHeight
    return remember(listState, topBarHeight) {
        derivedStateOf {
            val visibleItems = listState.layoutInfo.visibleItemsInfo
            val hero = visibleItems.firstOrNull { it.index == 0 }
            when {
                visibleItems.isEmpty() -> false
                hero == null -> true
                else -> hero.size + hero.offset <= topBarHeight
            }
        }
    }
}

private val TOP_BAR_HEIGHT = 56.dp
