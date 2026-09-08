package com.gorman.ourmemoryapp.ui

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gorman.ourmemoryapp.domain.models.Screen
import com.gorman.ourmemoryapp.ui.screens.DetailsScreen
import com.gorman.ourmemoryapp.ui.screens.InfoScreen
import com.gorman.ourmemoryapp.ui.screens.IntroScreen
import com.gorman.ourmemoryapp.ui.screens.MainScreen
import com.gorman.ourmemoryapp.ui.viewModel.DetailsViewModel

@Composable
fun AppNavigation(onChangeLangClick: (String) -> Unit) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.IntroScreen.route) {
        composable(Screen.HomeScreen.route) {
            MainScreen(onItemClick = { veteranId ->
                navController.navigate("${Screen.DetailScreen.route}/$veteranId")
            }, navigateToInfoScreen = {
                navController.navigate(Screen.InfoScreen.route)
            })
        }
        composable("${Screen.DetailScreen.route}/{veteranId}") {
            val veteranId = it.arguments?.getString("veteranId")

            val detailsViewModel = hiltViewModel<DetailsViewModel, DetailsViewModel.Factory>(
                creationCallback = { factory -> factory.create(veteranId.orEmpty()) }
            )

            DetailsScreen(
                detailsViewModel = detailsViewModel,
                navigateToMainScreen = {
                    navController.navigate(Screen.HomeScreen.route)
                }
            )
        }
        composable(Screen.InfoScreen.route) {
            InfoScreen(
                navigateToBack = { navController.popBackStack() },
                navigateToImage = {},
                onChangeLangClick = onChangeLangClick
            )
        }
        composable(Screen.IntroScreen.route) {
            IntroScreen {
                navController.navigate(Screen.HomeScreen.route) {
                    popUpTo(Screen.IntroScreen.route) {
                        inclusive = true
                    }
                }
            }
        }
    }
}
