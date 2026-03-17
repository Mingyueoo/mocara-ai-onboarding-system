package com.mocara.app.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mocara.app.ui.scanner.ScannerScreen
import com.mocara.app.ui.onboarding.OnboardingScreen
import com.mocara.app.ui.chat.ChatScreen
import com.mocara.app.ui.escalation.EscalationScreen
import com.mocara.app.viewmodel.ScannerViewModel
import com.mocara.app.viewmodel.OnboardingViewModel
import com.mocara.app.viewmodel.ChatViewModel

/**
 * Navigation Routes
 */
sealed class Screen(val route: String) {
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
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Scanner.route
    ) {
        // Scanner Screen - Entry point
        composable(Screen.Scanner.route) {
            val viewModel: ScannerViewModel = viewModel()
            ScannerScreen(
                viewModel = viewModel,
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