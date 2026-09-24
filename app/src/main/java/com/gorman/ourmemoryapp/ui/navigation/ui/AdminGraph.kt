package com.gorman.ourmemoryapp.ui.navigation.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gorman.ourmemoryapp.domain.models.Screen
import com.gorman.ourmemoryapp.ui.admin.feedbacklist.ui.FeedbackListScreen
import com.gorman.ourmemoryapp.ui.admin.home.ui.AdminHomeScreen
import com.gorman.ourmemoryapp.ui.admin.login.ui.AdminLoginScreen
import com.gorman.ourmemoryapp.ui.admin.moderation.ui.ModerationListScreen
import com.gorman.ourmemoryapp.ui.admin.moderation.ui.SubmissionReviewScreen
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
            onModerationClick = { navController.navigate(Screen.AdminModerationScreen.route) },
            onVeteransClick = { navController.navigate(Screen.AdminVeteransScreen.route) },
            onBurialsClick = { navController.navigate(Screen.AdminBurialsScreen.route) },
            onToursClick = { navController.navigate(Screen.AdminToursScreen.route) },
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
    composable(Screen.AdminModerationScreen.route) {
        ModerationListScreen(
            onBackClick = { navController.popBackStack() },
            onSubmissionClick = { navController.navigate(Screen.AdminSubmissionScreen.withSubmission(it)) }
        )
    }
    composable(
        route = Screen.AdminSubmissionScreen.pattern,
        arguments = listOf(navArgument(Screen.AdminSubmissionScreen.SUBMISSION_ID_ARG) { type = NavType.StringType })
    ) {
        SubmissionReviewScreen(onBackClick = { navController.popBackStack() })
    }
    adminContentGraph(navController)
}
