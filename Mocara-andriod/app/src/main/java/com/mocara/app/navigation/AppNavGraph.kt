package com.mocara.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mocara.app.di.AppContainer
import com.mocara.app.ui.scanner.ScannerScreen
import com.mocara.app.ui.onboarding.OnboardingScreen
import com.mocara.app.ui.chat.ChatScreen
import com.mocara.app.ui.auth.LoginScreen
import com.mocara.app.ui.auth.RegisterScreen
import com.mocara.app.ui.escalation.EscalationScreen
import com.mocara.app.viewmodel.ScannerViewModel
import com.mocara.app.viewmodel.OnboardingViewModel
import com.mocara.app.viewmodel.ChatViewModel
import com.mocara.app.viewmodel.AuthViewModel

/**
 * Navigation Routes
 */
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Scanner : Screen("scanner")
    object Onboarding : Screen("onboarding/{drugId}") {
        fun createRoute(drugId: String) = "onboarding/$drugId"
    }
    object Chat : Screen("chat/{drugId}") {
        fun createRoute(drugId: String) = "chat/$drugId"
    }
    object Escalation : Screen("escalation/{reason}") {
        fun createRoute(reason: String) = "escalation/$reason"
    }
}

/**
 * AppNavGraph - Main navigation graph
 * Flow: Scanner → Onboarding → Chat → Escalation
 */
@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route
) {
    val accessToken by AppContainer.tokenManager.accessTokenFlow.collectAsState(initial = null)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(accessToken, currentRoute) {
        if (accessToken.isNullOrBlank() && currentRoute != null && currentRoute != Screen.Login.route && currentRoute != Screen.Register.route) {
            navController.navigate(Screen.Login.route) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            val viewModel: AuthViewModel = viewModel()
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Scanner.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onGoRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            val viewModel: AuthViewModel = viewModel()
            RegisterScreen(
                viewModel = viewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.Scanner.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onGoLogin = { navController.navigate(Screen.Login.route) }
            )
        }

        // Scanner Screen - Entry point
        composable(Screen.Scanner.route) {
            val scannerViewModel: ScannerViewModel = viewModel()
            val authViewModel: AuthViewModel = viewModel()
            val authState by authViewModel.uiState.collectAsState()
            ScannerScreen(
                viewModel = scannerViewModel,
                isLoggingOut = authState.isLoggingOut,
                logoutError = authState.logoutError,
                onLogout = {
                    authViewModel.logout {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                onDismissLogoutError = authViewModel::clearLogoutError,
                onScanSuccess = { drugId ->
                    navController.navigate(Screen.Onboarding.createRoute(drugId))
                }
            )
        }

        // Onboarding Screen - Step-based medication onboarding
        composable(
            route = Screen.Onboarding.route,
            arguments = listOf(navArgument("drugId") { type = NavType.StringType })
        ) { backStackEntry ->
            val drugId = backStackEntry.arguments?.getString("drugId") ?: ""
            val viewModel: OnboardingViewModel = viewModel()

            OnboardingScreen(
                drugId = drugId,
                viewModel = viewModel,
                onComplete = {
                    navController.navigate(Screen.Chat.createRoute(drugId)) {
                        popUpTo(Screen.Scanner.route) { inclusive = false }
                    }
                },
                onEscalation = { reason ->
                    navController.navigate(Screen.Escalation.createRoute(reason)) {
                        popUpTo(Screen.Scanner.route) { inclusive = false }
                    }
                }
            )
        }

        // Chat Screen - Controlled conversation UI
        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("drugId") { type = NavType.StringType })
        ) { backStackEntry ->
            val drugId = backStackEntry.arguments?.getString("drugId") ?: ""
            val viewModel: ChatViewModel = viewModel()

            ChatScreen(
                drugId = drugId,
                viewModel = viewModel,
                onEscalation = { reason ->
                    navController.navigate(Screen.Escalation.createRoute(reason)) {
                        popUpTo(Screen.Scanner.route) { inclusive = false }
                    }
                }
            )
        }

        // Escalation Screen - Contact human medical professional
        composable(
            route = Screen.Escalation.route,
            arguments = listOf(navArgument("reason") { type = NavType.StringType })
        ) { backStackEntry ->
            val reason = backStackEntry.arguments?.getString("reason") ?: ""

            EscalationScreen(
                reason = reason,
                onBackToStart = {
                    navController.navigate(Screen.Scanner.route) {
                        popUpTo(Screen.Scanner.route) { inclusive = true }
                    }
                }
            )
        }
    }
}