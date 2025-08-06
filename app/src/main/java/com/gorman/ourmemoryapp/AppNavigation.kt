package com.gorman.ourmemoryapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.gorman.ourmemoryapp.data.Screen
import com.gorman.ourmemoryapp.ui.screens.DetailsScreen
import com.gorman.ourmemoryapp.ui.screens.InfoScreen
import com.gorman.ourmemoryapp.ui.screens.IntroScreen
import com.gorman.ourmemoryapp.ui.screens.MainScreen

@Composable
fun AppNavigation(onChangeLangClick: (String) -> Unit)
{
    val systemUiController = rememberSystemUiController()

    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Red,
            darkIcons = true
        )
    }
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.IntroScreen.route) {
        composable(Screen.HomeScreen.route){
            MainScreen (onItemClick = { veteranId ->
                navController.navigate("${Screen.DetailScreen.route}/${veteranId}")
            }, navigateToInfoScreen = {
                navController.navigate(Screen.InfoScreen.route)
            })
        }
        composable("${Screen.DetailScreen.route}/{veteranId}"){
            val veteranId = it.arguments?.getString("veteranId")
            DetailsScreen(id = veteranId!!) {
                navController.navigate(Screen.HomeScreen.route)
            }
        }
        composable(Screen.InfoScreen.route) {
            InfoScreen (
                navigateToBack = { navController.popBackStack() },
                navigateToImage = {},
                onChangeLangClick = onChangeLangClick
            )
        }
        composable(Screen.IntroScreen.route){
            IntroScreen {
                navController.navigate(Screen.HomeScreen.route){
                    popUpTo(Screen.IntroScreen.route) {
                        inclusive = true
                    }
                }
            }
        }
    }
}