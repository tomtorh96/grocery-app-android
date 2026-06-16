package com.Tomtor.groceryapp.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.Tomtor.groceryapp.viewmodel.AuthViewModel
import com.Tomtor.groceryapp.viewmodel.HomeViewModel
@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.state.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (authState.isLoggedIn) "home" else "login"
    ) {
        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate("register") },
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("register") {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate("home") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen(
                authViewModel = authViewModel,
                onNavigateToList = { listId ->
                    navController.navigate("list/$listId")
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "list/{listId}",
            arguments = listOf(navArgument("listId") { type = NavType.StringType })
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getString("listId") ?: return@composable
            val homeViewModel: HomeViewModel = viewModel()
            ListDetailScreen(
                listId = listId,
                onNavigateBack = {
                    homeViewModel.loadLists()
                    navController.popBackStack()
                },
                onNavigateToHistory = { navController.navigate("history/$listId") },
                onNavigateToMembers = { navController.navigate("members/$listId") }
            )
        }

        composable(
            route = "history/{listId}",
            arguments = listOf(navArgument("listId") { type = NavType.StringType })
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getString("listId") ?: return@composable
            HistoryScreen(
                listId = listId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "members/{listId}",
            arguments = listOf(navArgument("listId") { type = NavType.StringType })
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getString("listId") ?: return@composable
            MembersScreen(
                listId = listId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}