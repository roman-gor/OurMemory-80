package com.gorman.ourmemoryapp.ui.navigation.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gorman.ourmemoryapp.domain.models.Screen
import com.gorman.ourmemoryapp.ui.admin.burials.ui.AdminBurialsScreen
import com.gorman.ourmemoryapp.ui.admin.burials.ui.BurialEditorScreen
import com.gorman.ourmemoryapp.ui.admin.guide.models.GuideSection
import com.gorman.ourmemoryapp.ui.admin.tours.ui.AdminToursScreen
import com.gorman.ourmemoryapp.ui.admin.tours.ui.TourEditorScreen
import com.gorman.ourmemoryapp.ui.admin.veterans.ui.AdminVeteransScreen
import com.gorman.ourmemoryapp.ui.admin.veterans.ui.VeteranEditorScreen

fun NavGraphBuilder.adminContentGraph(navController: NavHostController) {
    composable(Screen.AdminVeteransScreen.route) {
        AdminVeteransScreen(
            onBackClick = { navController.popBackStack() },
            onVeteranClick = { navController.navigate(Screen.AdminVeteranEditorScreen.forVeteran(it)) },
            onNewVeteranClick = { navController.navigate(Screen.AdminVeteranEditorScreen.forVeteran("")) },
            onGuideClick = { navController.navigate(Screen.AdminGuideScreen.forSection(GuideSection.VETERAN.name)) }
        )
    }
    composable(
        route = Screen.AdminVeteranEditorScreen.pattern,
        arguments = listOf(
            navArgument(Screen.AdminVeteranEditorScreen.VETERAN_ID_ARG) {
                type = NavType.StringType
                defaultValue = ""
            }
        )
    ) {
        VeteranEditorScreen(onBackClick = { navController.popBackStack() })
    }
    composable(Screen.AdminBurialsScreen.route) {
        AdminBurialsScreen(
            onBackClick = { navController.popBackStack() },
            onBurialClick = { navController.navigate(Screen.AdminBurialEditorScreen.forBurial(it)) },
            onNewBurialClick = { navController.navigate(Screen.AdminBurialEditorScreen.forBurial("")) },
            onGuideClick = { navController.navigate(Screen.AdminGuideScreen.forSection(GuideSection.BURIAL.name)) }
        )
    }
    composable(
        route = Screen.AdminBurialEditorScreen.pattern,
        arguments = listOf(
            navArgument(Screen.AdminBurialEditorScreen.BURIAL_ID_ARG) {
                type = NavType.StringType
                defaultValue = ""
            }
        )
    ) {
        BurialEditorScreen(onBackClick = { navController.popBackStack() })
    }
    composable(Screen.AdminToursScreen.route) {
        AdminToursScreen(
            onBackClick = { navController.popBackStack() },
            onTourClick = { navController.navigate(Screen.AdminTourEditorScreen.forTour(it)) },
            onNewTourClick = { navController.navigate(Screen.AdminTourEditorScreen.forTour("")) },
            onGuideClick = { navController.navigate(Screen.AdminGuideScreen.forSection(GuideSection.TOUR.name)) }
        )
    }
    composable(
        route = Screen.AdminTourEditorScreen.pattern,
        arguments = listOf(
            navArgument(Screen.AdminTourEditorScreen.TOUR_ID_ARG) {
                type = NavType.StringType
                defaultValue = ""
            }
        )
    ) {
        TourEditorScreen(onBackClick = { navController.popBackStack() })
    }
}
