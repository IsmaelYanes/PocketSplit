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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.develop.pocketsplit.ui.components.BottomNavigationBar
import com.develop.pocketsplit.ui.navigation.Routes
import com.develop.pocketsplit.ui.screen.AccountScreen
import com.develop.pocketsplit.ui.screen.FinanceScreen
import com.develop.pocketsplit.ui.screen.HomeScreen
import com.develop.pocketsplit.ui.screen.LoginScreen
import com.develop.pocketsplit.ui.screen.SettingsScreen
import com.develop.pocketsplit.ui.screen.StatisticsScreen
import com.develop.pocketsplit.ui.theme.PocketSplitTheme
import com.develop.pocketsplit.ui.viewmodel.AuthViewModel

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
                    // Ámbito de la Activity: la sesión se comparte entre Login, Ajustes y Cuenta.
                    val authViewModel: AuthViewModel = viewModel()
                    // Se calcula una sola vez: cambiarlo en recomposición recrearía el grafo.
                    val startDestination = remember {
                        if (authViewModel.shouldSkipLogin) Routes.HOME else Routes.LOGIN
                    }
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
                            startDestination = startDestination,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable(Routes.LOGIN) {
                                LoginScreen(
                                    viewModel = authViewModel,
                                    onContinue = { nav.goHomeClearingStack() },
                                    onContinueAsGuest = { nav.goHomeClearingStack() }
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
                                SettingsScreen(
                                    viewModel = authViewModel,
                                    onOpenAccount = { nav.navigate(Routes.SETTINGS_ACCOUNT) },
                                    // Cerrar sesión: vuelve al acceso sin historial.
                                    onSignedOut = { nav.goToLoginClearingStack() },
                                    // Invitado que quiere iniciar sesión o crear cuenta: se puede volver atrás.
                                    onSignIn = {
                                        nav.navigate(Routes.LOGIN) { launchSingleTop = true }
                                    }
                                )
                            }
                            composable(Routes.SETTINGS_ACCOUNT) {
                                AccountScreen(
                                    viewModel = authViewModel,
                                    onBack = { nav.popBackStack() },
                                    onSignedOut = { nav.goToLoginClearingStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Va a Inicio dejando como única pantalla en la pila (evita volver al login con "atrás"). */
private fun NavController.goHomeClearingStack() {
    navigate(Routes.HOME) {
        popUpTo(graph.id) { inclusive = true }
    }
}

/** Va al acceso vaciando la pila: tras cerrar sesión o borrar la cuenta no se puede volver atrás. */
private fun NavController.goToLoginClearingStack() {
    navigate(Routes.LOGIN) {
        popUpTo(graph.id) { inclusive = true }
    }
}