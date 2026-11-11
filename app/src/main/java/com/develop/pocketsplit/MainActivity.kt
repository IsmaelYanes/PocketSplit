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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.develop.pocketsplit.ui.navigation.Routes
import com.develop.pocketsplit.ui.screen.home.HomeScreen
import com.develop.pocketsplit.ui.screen.login.LoginScreen
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
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        val nav = rememberNavController()

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
                                HomeScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}