package com.uat.uatlife.navigation

import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.uat.uatlife.ui.screens.*
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.uat.uatlife.data.mock.productosMock

/**
 * Grafo de navegación principal de UATLife.
 * 
 * Flujo: Splash → Welcome → Login/Register → Main (BottomBar)
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // --- Flujo de Autenticación ---

        // Splash: animación de 2.5s → verifica token → Welcome o Home
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Welcome: pantalla de bienvenida con imagen del campus
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        // Login
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onBack = { navController.popBackStack() },
                onNavigateToVerifyOtp = { identificador ->
                    val encoded = java.net.URLEncoder.encode(identificador, "UTF-8")
                    navController.navigate(Screen.VerifyOtp.route + "?id=$encoded")
                }
            )
        }

        composable(
            route = Screen.VerifyOtp.route + "?id={identificador}",
            arguments = listOf(navArgument("identificador") { type = NavType.StringType; defaultValue = "" })
        ) { backStackEntry ->
            val identificador = backStackEntry.arguments?.getString("identificador") ?: ""
            VerifyOtpScreen(
                identificador = identificador,
                onBack = { navController.popBackStack() },
                onNavigateToReset = { resetToken ->
                    val encoded = java.net.URLEncoder.encode(resetToken, "UTF-8")
                    navController.navigate(Screen.ResetPassword.route + "?token=$encoded") {
                        popUpTo(Screen.ForgotPassword.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.ResetPassword.route + "?token={resetToken}",
            arguments = listOf(navArgument("resetToken") { type = NavType.StringType; defaultValue = "" })
        ) { backStackEntry ->
            val resetToken = backStackEntry.arguments?.getString("resetToken") ?: ""
            ResetPasswordScreen(
                resetToken = resetToken,
                onBackToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = false }
                    }
                }
            )
        }

        // Register
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // --- Pantallas Principales (con BottomBar) ---
        composable(Screen.Home.route) {
            HomeScreen()
        }

        composable(Screen.Communities.route) {
            CommunitiesScreen(
                onNavigateToCommunity = { id -> navController.navigate(Screen.CommunityDetail.route + "/$id") }
            )
        }

        composable(Screen.BusTracker.route) {
            BusTrackerScreen()
        }

        composable(Screen.Messages.route) {
            MessagesScreen(
                onNavigateToChat = { chatId -> navController.navigate(Screen.ChatDetail.route + "/$chatId") }
            )
        }

        composable(Screen.Market.route) {
            MarketScreen(
                onNavigateToSellerProfile = { navController.navigate(Screen.SellerProfile.route) },
                onNavigateToCreateProduct = { navController.navigate(Screen.CreateProduct.route) },
                onNavigateToProduct = { nombre -> navController.navigate(Screen.ProductDetail.route + "/${nombre}") }
            )
        }

        composable(Screen.SellerProfile.route) {
            SellerProfileScreen(
                onNavigateToCreateProduct = { navController.navigate(Screen.CreateProduct.route) },
                onNavigateToEdit = { productId -> 
                    navController.navigate(Screen.EditProduct.route + "/$productId") 
                },
                onNavigateToProductDetail = { nombre -> navController.navigate(Screen.ProductDetail.route + "/$nombre") },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ProductDetail.route + "/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val idStr = backStackEntry.arguments?.getString("id") ?: ""
            val id = idStr.toIntOrNull() ?: 0
            ProductDetailScreen(
                productoId = id,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CreateProduct.route) {
            CreateProductScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditProduct.route + "/{productId}",
            arguments = listOf(
                navArgument("productId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: 0
            CreateProductScreen(
                productId = productId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ChatDetail.route + "/{chatId}",
            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            ChatDetailScreen(
                chatId = chatId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) },
                onNavigateToSecurity = { navController.navigate(Screen.SecuritySettings.route) },
                onNavigateToModeration = { navController.navigate(Screen.ModerationPanel.route) },
                onLogout = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.SecuritySettings.route) {
            SecuritySettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.CommunityDetail.route + "/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            CommunityDetailScreen(
                communityIdStr = id,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ModerationPanel.route) {
            ModerationPanelScreen(
                onBack = { navController.popBackStack() },
                onNavigateToChat = { chatId ->
                    navController.navigate(Screen.ChatDetail.route + "/$chatId")
                }
            )
        }
    }
}
