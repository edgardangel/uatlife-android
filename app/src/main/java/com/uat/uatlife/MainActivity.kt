package com.uat.uatlife

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.uat.uatlife.data.TokenManager
import com.uat.uatlife.navigation.AppNavGraph
import com.uat.uatlife.navigation.BottomNavBar
import com.uat.uatlife.navigation.Screen
import com.uat.uatlife.network.RetrofitClient
import com.uat.uatlife.ui.theme.UATBlueDark
import com.uat.uatlife.ui.theme.UATLifeTheme
import com.uat.uatlife.ui.theme.UATOrange
import kotlinx.coroutines.launch
import android.widget.Toast

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UATLifeTheme {
                val context = androidx.compose.ui.platform.LocalContext.current
                val navController = rememberNavController()
                val scope = rememberCoroutineScope()
                val tokenManager = remember { TokenManager(context) }
                val apiService = remember { RetrofitClient.getApiService(context) }

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Estado de sanción
                val banPermanente by tokenManager.getBanPermanente().collectAsState(initial = false)
                val suspensionHasta by tokenManager.getSuspensionHasta().collectAsState(initial = null)
                
                // Determinar si está bloqueado actualmente
                val isBlocked = remember(banPermanente, suspensionHasta) {
                    if (banPermanente) true
                    else if (!suspensionHasta.isNullOrBlank()) {
                        try {
                            // Comparar fechas si es necesario, o confiar en el backend (el backend borra el campo si ya expiró al consultar perfil)
                            // Por ahora, si hay un valor, asumimos que sigue activo
                            true
                        } catch (e: Exception) { false }
                    } else false
                }

                // Sincronizar estado de sanción con el servidor al iniciar o cambiar de pestaña importante
                LaunchedEffect(currentRoute) {
                    if (currentRoute != null && currentRoute != Screen.Login.route && currentRoute != Screen.Welcome.route) {
                        scope.launch {
                            try {
                                val resp = apiService.getProfile()
                                if (resp.isSuccessful) {
                                    val p = resp.body()
                                    if (p != null) {
                                        tokenManager.updateSanctionStatus(p.banPermanente, p.suspensionHasta)
                                    }
                                }
                            } catch (e: Exception) {}
                        }
                    }
                }

                // Pantallas donde se muestra la BottomBar
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
                                    if (isBlocked && route != Screen.Profile.route) {
                                        // No permitir navegar si está bloqueado, excepto a perfil
                                        android.widget.Toast.makeText(context, "Tu cuenta está suspendida. Solo puedes acceder a tu perfil.", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        navController.navigate(route) {
                                            popUpTo(Screen.Home.route) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        AppNavGraph(
                            navController = navController,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Overlay de bloqueo si está sancionado y no está en el perfil o pantallas de auth
                        val isAuthScreen = currentRoute == Screen.Login.route || currentRoute == Screen.Welcome.route || currentRoute == Screen.Register.route
                        val isProfileScreen = currentRoute == Screen.Profile.route
                        
                        if (isBlocked && !isAuthScreen && !isProfileScreen) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.85f))
                                    .clickable(enabled = true) { /* Bloquear clics */ },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(32.dp)
                                ) {
                                    androidx.compose.material3.Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Filled.Block,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(80.dp)
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text(
                                        text = if (banPermanente) "CUENTA SUSPENDIDA PERMANENTEMENTE" else "CUENTA SUSPENDIDA TEMPORALMENTE",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = if (banPermanente) "Has sido expulsado de la comunidad por incumplir las normas."
                                               else "Tu acceso está restringido hasta: $suspensionHasta",
                                        color = Color.LightGray,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(32.dp))
                                    Button(
                                        onClick = { navController.navigate(Screen.Profile.route) },
                                        colors = ButtonDefaults.buttonColors(containerColor = UATOrange)
                                    ) {
                                        Text("Ir a mi perfil", color = UATBlueDark)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}