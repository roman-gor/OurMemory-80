package com.gorman.ourmemoryapp.ui.navigation.ui

import androidx.navigation.NavHostController
import com.gorman.ourmemoryapp.domain.models.Screen
import com.gorman.ourmemoryapp.ui.navigation.models.TopLevelTab

fun NavHostController.navigateToTab(tab: TopLevelTab) {
    navigate(tab.screen.route) {
        popUpTo(Screen.HomeScreen.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
