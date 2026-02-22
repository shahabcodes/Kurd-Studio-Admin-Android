package com.crimsonedge.studioadmin.presentation.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.crimsonedge.studioadmin.data.local.ThemeDataStore
import com.crimsonedge.studioadmin.data.local.TokenDataStore
import com.crimsonedge.studioadmin.presentation.artworks.form.ArtworkFormScreen
import com.crimsonedge.studioadmin.presentation.auth.LoginScreen
import com.crimsonedge.studioadmin.presentation.content.ContentScreen
import com.crimsonedge.studioadmin.presentation.contacts.detail.ContactDetailScreen
import com.crimsonedge.studioadmin.presentation.contacts.list.ContactListScreen
import com.crimsonedge.studioadmin.presentation.dashboard.DashboardScreen
import com.crimsonedge.studioadmin.presentation.images.ImageListScreen
import com.crimsonedge.studioadmin.presentation.more.MoreScreen
import com.crimsonedge.studioadmin.presentation.siteconfig.SiteConfigScreen
import com.crimsonedge.studioadmin.presentation.social.SocialLinksScreen
import com.crimsonedge.studioadmin.presentation.writings.form.WritingFormScreen

private const val TRANSITION_DURATION = 300

private fun defaultEnterTransition(): EnterTransition =
    fadeIn(animationSpec = tween(TRANSITION_DURATION))

private fun defaultExitTransition(): ExitTransition =
    fadeOut(animationSpec = tween(TRANSITION_DURATION))

@Composable
fun AppNavigation(
    startDestination: String,
    tokenDataStore: TokenDataStore,
    themeDataStore: ThemeDataStore,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = setOf(
        Screen.Dashboard.route,
        Screen.Content.route,
        Screen.ImageList.route,
        Screen.SiteConfig.route,
        Screen.More.route
    )

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { defaultEnterTransition() },
        exitTransition = { defaultExitTransition() },
        popEnterTransition = { defaultEnterTransition() },
        popExitTransition = { defaultExitTransition() }
    ) {
        // Login - outside scaffold
        composable(route = Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Dashboard
        composable(route = Screen.Dashboard.route) {
            MainScaffold(navController = navController, showBottomBar = true) {
                DashboardScreen(navController = navController)
            }
        }

        // Content (Artworks + Writings tabs)
        composable(
            route = Screen.Content.route,
            arguments = listOf(navArgument("tab") {
                type = NavType.IntType
                defaultValue = 0
            })
        ) { backStackEntry ->
            val initialTab = backStackEntry.arguments?.getInt("tab") ?: 0
            MainScaffold(navController = navController, showBottomBar = true) {
                ContentScreen(navController = navController, initialTab = initialTab)
            }
        }

        composable(
            route = Screen.ArtworkForm.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            MainScaffold(navController = navController, showBottomBar = false) {
                ArtworkFormScreen(navController = navController)
            }
        }

        // Writing Form
        composable(
            route = Screen.WritingForm.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            MainScaffold(navController = navController, showBottomBar = false) {
                WritingFormScreen(navController = navController)
            }
        }

        // Images
        composable(route = Screen.ImageList.route) {
            MainScaffold(navController = navController, showBottomBar = true) {
                ImageListScreen()
            }
        }

        // Site Config
        composable(route = Screen.SiteConfig.route) {
            MainScaffold(navController = navController, showBottomBar = true) {
                SiteConfigScreen()
            }
        }

        // Navigation Links
        composable(route = Screen.NavList.route) {
            MainScaffold(navController = navController, showBottomBar = false) {
                NavListScreen(navController = navController)
            }
        }

        // Social Links
        composable(route = Screen.SocialLinks.route) {
            MainScaffold(navController = navController, showBottomBar = false) {
                SocialLinksScreen(navController = navController)
            }
        }

        // Contacts
        composable(route = Screen.ContactList.route) {
            MainScaffold(navController = navController, showBottomBar = false) {
                ContactListScreen(navController = navController)
            }
        }

        composable(
            route = Screen.ContactDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) {
            MainScaffold(navController = navController, showBottomBar = false) {
                ContactDetailScreen(navController = navController)
            }
        }

        // More
        composable(route = Screen.More.route) {
            MainScaffold(navController = navController, showBottomBar = true) {
                MoreScreen(
                    navController = navController,
                    tokenDataStore = tokenDataStore,
                    themeDataStore = themeDataStore
                )
            }
        }
    }
}

@Composable
private fun MainScaffold(
    navController: NavHostController,
    showBottomBar: Boolean,
    content: @Composable () -> Unit
) {
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            content()
        }
    }
}
