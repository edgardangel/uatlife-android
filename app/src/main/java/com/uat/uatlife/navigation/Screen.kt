package com.uat.uatlife.navigation

/**
 * Define todas las rutas de navegación de la app UATLife.
 */
sealed class Screen(val route: String) {

    // --- Flujo de autenticación ---
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Register : Screen("register")
    object TermsAndConditions : Screen("terms")
    object ForgotPassword : Screen("forgot_password")
    object VerifyOtp : Screen("verify_otp")
    object ResetPassword : Screen("reset_password")

    // --- Pantallas principales (Bottom Navigation) ---
    object Home : Screen("home")           // 🏠 Inicio
    object Communities : Screen("communities") // 👥 Comunidades
    object Market : Screen("market")       // 🛒 Marketplace
    object BusTracker : Screen("bus")      // 🚌 LoboBús Tracker
    object Messages : Screen("messages")   // 💬 Centro de Mensajes / Chat
    object Profile : Screen("profile")     // 👤 Perfil
    
    // --- Pantallas secundarias ---
    object SellerProfile : Screen("seller_profile")
    object ProductDetail : Screen("product_detail")
    object CreateProduct : Screen("create_product")
    object EditProduct : Screen("edit_product")
    object ChatDetail : Screen("chat_detail")
    object EditProfile : Screen("edit_profile")
    object SecuritySettings : Screen("security_settings")
    object CommunityDetail : Screen("community_detail")
}
