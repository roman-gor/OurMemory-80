package com.gorman.ourmemoryapp.ui.navigation.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.gorman.ourmemoryapp.domain.models.Screen
import com.gorman.ourmemoryapp.ui.admin.feedbacklist.ui.FeedbackListScreen
import com.gorman.ourmemoryapp.ui.admin.home.ui.AdminHomeScreen
import com.gorman.ourmemoryapp.ui.admin.login.ui.AdminLoginScreen
import com.gorman.ourmemoryapp.ui.navigation.models.TopLevelTab

fun NavGraphBuilder.adminGraph(navController: NavHostController) {
    composable(Screen.AdminLoginScreen.route) {
        AdminLoginScreen(
            onBackClick = { navController.popBackStack() },
            onSignedIn = {
                navController.popBackStack()
                navController.navigateToTab(TopLevelTab.ADMIN)
            }
        )
    }
    composable(Screen.AdminHomeScreen.route) {
        AdminHomeScreen(
            onFeedbackClick = { navController.navigate(Screen.AdminFeedbackScreen.route) },
            onSignedOut = { navController.navigateToTab(TopLevelTab.ABOUT) }
        )
    }
    composable(Screen.AdminFeedbackScreen.route) {
        FeedbackListScreen(
            onBackClick = { navController.popBackStack() },
            onVeteranClick = { navController.navigate("${Screen.DetailScreen.route}/$it") }
        )
    }
}
