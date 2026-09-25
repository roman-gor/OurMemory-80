package com.gorman.ourmemoryapp.ui.navigation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.gorman.ourmemoryapp.ui.common.ui.LocalBottomBarInset
import com.gorman.ourmemoryapp.ui.common.ui.StatusBarScrim

fun NavGraphBuilder.tabComposable(
    route: String,
    hasStatusBarScrim: Boolean = false,
    content: @Composable (NavBackStackEntry) -> Unit
) {
    composable(route) { entry ->
        CompositionLocalProvider(LocalBottomBarInset provides BAR_HEIGHT + BAR_VERTICAL_PADDING * 2) {
            Box {
                content(entry)
                if (hasStatusBarScrim) {
                    StatusBarScrim(modifier = Modifier.align(Alignment.TopCenter))
                }
            }
        }
    }
}
