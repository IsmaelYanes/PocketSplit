package com.develop.pocketsplit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.develop.pocketsplit.ui.components.BottomNavigationBar
import com.develop.pocketsplit.ui.navigation.Routes
import com.develop.pocketsplit.ui.screen.FinanceScreen
import com.develop.pocketsplit.ui.screen.SettingsScreen
import com.develop.pocketsplit.ui.screen.StatisticsScreen
import com.develop.pocketsplit.ui.screen.HomeScreen
import com.develop.pocketsplit.ui.screen.LoginScreen
import com.develop.pocketsplit.ui.theme.PocketSplitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PocketSplitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val nav = rememberNavController()
                    val currentRoute = nav.currentBackStackEntryAsState().value?.destination?.route

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            if (currentRoute != null && currentRoute != Routes.LOGIN) {
                                BottomNavigationBar(
                                    currentRoute = currentRoute,
                                    onNavigate = { route ->
                                        nav.navigate(route) {
                                            popUpTo(Routes.HOME)
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = nav,
                            startDestination = Routes.LOGIN,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable(Routes.LOGIN) {
                                LoginScreen(
                                    onContinue = {
                                        nav.navigate(Routes.HOME) {
                                            popUpTo(Routes.LOGIN) { inclusive = true }
                                        }
                                    },
                                    onContinueAsGuest = {
                                        nav.navigate(Routes.HOME) {
                                            popUpTo(Routes.LOGIN) { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable(Routes.HOME) {
                                HomeScreen(onNavigateToFinance = { id -> nav.navigate(Routes.finance(id)) })
                            }
                            composable(
                                route = Routes.FINANCE,
                                arguments = listOf(navArgument(Routes.FINANCE_ID_ARG) { type = NavType.StringType })
                            ) {
                                FinanceScreen(onBack = { nav.popBackStack() })
                            }
                            composable(Routes.STATISTICS) {
                                StatisticsScreen()
                            }
                            composable(Routes.SETTINGS) {
                                SettingsScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}