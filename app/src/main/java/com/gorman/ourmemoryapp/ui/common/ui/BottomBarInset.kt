package com.gorman.ourmemoryapp.ui.common.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val LocalBottomBarInset = staticCompositionLocalOf { 0.dp }

@Composable
fun bottomBarContentPadding(): Dp =
    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + LocalBottomBarInset.current
