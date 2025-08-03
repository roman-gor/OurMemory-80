package com.gorman.ourmemoryapp

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import com.gorman.ourmemoryapp.data.Screen
import com.gorman.ourmemoryapp.ui.screens.DetailsScreen
import com.gorman.ourmemoryapp.ui.screens.InfoScreen
import com.gorman.ourmemoryapp.ui.screens.MainScreen

@Composable
fun AppNavigation()
{
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.HomeScreen.route) {
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
            InfoScreen {
                navController.navigateUp()
            }
        }
    }
}