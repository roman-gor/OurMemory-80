package com.gorman.ourmemoryapp.ui.navigation.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gorman.ourmemoryapp.domain.models.Screen
import com.gorman.ourmemoryapp.ui.admin.admins.ui.AdminsScreen
import com.gorman.ourmemoryapp.ui.admin.feedbacklist.ui.FeedbackListScreen
import com.gorman.ourmemoryapp.ui.admin.guide.ui.EditorGuideScreen
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
    tabComposable(Screen.AdminHomeScreen.route, hasStatusBarScrim = true) {
        AdminHomeScreen(
            onNavigate = { destination -> navController.navigate(destination.route()) },
            onSignedOut = { navController.navigateToTab(TopLevelTab.MORE) }
        )
    }
    composable(
        route = Screen.AdminGuideScreen.pattern,
        arguments = listOf(
            navArgument(Screen.AdminGuideScreen.SECTION_ARG) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) {
        EditorGuideScreen(onBackClick = { navController.popBackStack() })
    }
    composable(Screen.AdminAdminsScreen.route) {
        AdminsScreen(onBackClick = { navController.popBackStack() })
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
