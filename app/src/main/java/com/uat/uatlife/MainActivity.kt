package com.uat.uatlife

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.uat.uatlife.navigation.AppNavGraph
import com.uat.uatlife.navigation.BottomNavBar
import com.uat.uatlife.navigation.Screen
import com.uat.uatlife.ui.theme.UATLifeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UATLifeTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Pantallas donde se muestra la BottomBar (solo las principales)
                val mainScreens = listOf(
                    Screen.Home.route,
                    Screen.Communities.route,
                    Screen.BusTracker.route,
                    Screen.Messages.route,
                    Screen.Market.route,
                    Screen.Profile.route
                )
                val showBottomBar = currentRoute in mainScreens

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavBar(
                                currentRoute = currentRoute,
                                onItemClick = { route ->
                                    navController.navigate(route) {
                                        // Evitar múltiples copias del mismo destino
                                        popUpTo(Screen.Home.route) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    AppNavGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}