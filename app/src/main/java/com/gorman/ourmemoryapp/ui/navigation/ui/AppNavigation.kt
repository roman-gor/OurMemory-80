package com.gorman.ourmemoryapp.ui.navigation.ui

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.util.Consumer
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.gorman.ourmemoryapp.domain.models.Screen
import com.gorman.ourmemoryapp.ui.common.models.VeteranLink
import com.gorman.ourmemoryapp.ui.details.ui.DetailsScreen
import com.gorman.ourmemoryapp.ui.details.viewmodels.DetailsViewModel
import com.gorman.ourmemoryapp.ui.info.ui.InfoScreen
import com.gorman.ourmemoryapp.ui.map.ui.MapScreen
import com.gorman.ourmemoryapp.ui.navigation.models.TopLevelTab
import com.gorman.ourmemoryapp.ui.screens.IntroScreen
import com.gorman.ourmemoryapp.ui.screens.MainScreen

@Composable
fun AppNavigation(openedFromLink: Boolean, onChangeLangClick: (String) -> Unit) {
    val navController = rememberNavController()
    val activity = LocalActivity.current as? ComponentActivity

    DisposableEffect(navController, activity) {
        val listener = Consumer<Intent> { navController.handleDeepLink(it) }
        activity?.addOnNewIntentListener(listener)
        onDispose { activity?.removeOnNewIntentListener(listener) }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentTab = TopLevelTab.entries.firstOrNull { it.screen.route == backStackEntry?.destination?.route }
    val openVeteran: (String) -> Unit = { veteranId ->
        navController.navigate("${Screen.DetailScreen.route}/$veteranId")
    }

    Scaffold(
        bottomBar = {
            if (currentTab != null) {
                AppBottomBar(
                    selectedTab = currentTab,
                    onTabClick = { navController.navigateToTab(it) }
                )
            }
        },
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        val bottomPadding = PaddingValues(bottom = padding.calculateBottomPadding())
        NavHost(
            navController = navController,
            startDestination = if (openedFromLink) Screen.HomeScreen.route else Screen.IntroScreen.route,
            modifier = Modifier
                .padding(bottomPadding)
                .consumeWindowInsets(bottomPadding)
        ) {
            composable(Screen.IntroScreen.route) {
                IntroScreen {
                    navController.navigate(Screen.HomeScreen.route) {
                        popUpTo(Screen.IntroScreen.route) { inclusive = true }
                    }
                }
            }
            composable(Screen.HomeScreen.route) {
                MainScreen(onItemClick = openVeteran)
            }
            composable(Screen.MapScreen.route) {
                MapScreen(onBackClick = null, onVeteranClick = openVeteran)
            }
            composable(Screen.InfoScreen.route) {
                InfoScreen(
                    onOpenMapClick = { navController.navigateToTab(TopLevelTab.MAP) },
                    onChangeLangClick = onChangeLangClick
                )
            }
            composable(
                route = "${Screen.DetailScreen.route}/{veteranId}",
                deepLinks = listOf(navDeepLink { uriPattern = "${VeteranLink.BASE_URL}/{veteranId}" })
            ) {
                val veteranId = it.arguments?.getString("veteranId")
                val detailsViewModel = hiltViewModel<DetailsViewModel, DetailsViewModel.Factory>(
                    creationCallback = { factory -> factory.create(veteranId.orEmpty()) }
                )
                DetailsScreen(
                    detailsViewModel = detailsViewModel,
                    onBackClick = { navController.popBackStack() },
                    onShowOnMapClick = { burialId ->
                        navController.navigate(Screen.BurialMapScreen.withBurial(burialId))
                    }
                )
            }
            composable(
                route = Screen.BurialMapScreen.pattern,
                arguments = listOf(navArgument(Screen.BurialMapScreen.BURIAL_ID_ARG) { type = NavType.StringType })
            ) {
                MapScreen(
                    onBackClick = { navController.popBackStack() },
                    onVeteranClick = openVeteran
                )
            }
        }
    }
}

private fun NavHostController.navigateToTab(tab: TopLevelTab) {
    navigate(tab.screen.route) {
        popUpTo(Screen.HomeScreen.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
