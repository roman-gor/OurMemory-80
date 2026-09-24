package com.gorman.ourmemoryapp.ui.navigation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.gorman.ourmemoryapp.ui.common.ui.LocalBottomBarInset

fun NavGraphBuilder.tabComposable(route: String, content: @Composable (NavBackStackEntry) -> Unit) {
    composable(route) { entry ->
        CompositionLocalProvider(LocalBottomBarInset provides BAR_HEIGHT + BAR_VERTICAL_PADDING * 2) {
            content(entry)
        }
    }
}
