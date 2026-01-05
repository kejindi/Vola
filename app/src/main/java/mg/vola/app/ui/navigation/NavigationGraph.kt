package com.vola.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vola.app.ui.screens.budget.BudgetScreen
import com.vola.app.ui.screens.dashboard.DashboardScreen
import com.vola.app.ui.screens.goal.GoalsScreen
import com.vola.app.ui.screens.onboarding.OnboardingScreen
import com.vola.app.ui.screens.settings.SettingsScreen
import com.vola.app.ui.screens.splash.SplashScreen
import com.vola.app.ui.screens.transaction.AddTransactionScreen
import com.vola.app.ui.screens.transaction.TransactionsScreen
import com.vola.app.viewmodels.BudgetViewModel
import com.vola.app.viewmodels.GoalViewModel
import com.vola.app.viewmodels.MainViewModel
import com.vola.app.viewmodels.TransactionViewModel

@Composable
fun VolaNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = "splash"
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        composable("splash") {
            SplashScreen(
                onTimeout = { navController.navigate("onboarding") }
            )
        }
        
        composable("onboarding") {
            OnboardingScreen(
                navController = navController
            )
        }
        
        composable("dashboard") {
            val viewModel: MainViewModel = hiltViewModel()
            DashboardScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
        
        composable("transactions") {
            val viewModel: TransactionViewModel = hiltViewModel()
            TransactionsScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
        
        composable("add_transaction") {
            AddTransactionScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable("budget") {
            val viewModel: BudgetViewModel = hiltViewModel()
            BudgetScreen(
                onBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }
        
        composable("goals") {
            val viewModel: GoalViewModel = hiltViewModel()
            GoalsScreen(
                onBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }
        
        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        // Add more screens here as needed
    }
}