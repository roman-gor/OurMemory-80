package com.gorman.ourmemoryapp

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import com.gorman.ourmemoryapp.ui.screens.DetailsScreen
import com.gorman.ourmemoryapp.ui.screens.MainScreen

@Composable
fun AppNavigation()
{
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "mainscreen") {
        composable("mainscreen"){
            MainScreen { veteranId ->
                navController.navigate("detailsscreen/${veteranId}")
            }
        }
        composable("detailsscreen/{veteranId}"){
            val veteranId = it.arguments?.getString("veteranId")
            DetailsScreen(id = veteranId!!) {
                navController.navigate("mainscreen")
            }
        }
    }
}