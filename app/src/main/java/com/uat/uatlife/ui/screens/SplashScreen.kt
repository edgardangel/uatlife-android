package com.uat.uatlife.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uat.uatlife.data.TokenManager
import com.uat.uatlife.ui.theme.UATBlue
import com.uat.uatlife.ui.theme.UATOrange
import com.uat.uatlife.ui.theme.UATOnPrimaryLight
import kotlinx.coroutines.delay

/**
 * Pantalla de bienvenida con animación de fade-in.
 * Verifica si hay un token guardado para decidir si ir al Login o al Home.
 */
@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val token by tokenManager.getToken().collectAsState(initial = null)

    // Animación de aparición
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1500),
        label = "splash_alpha"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2500) // Esperar 2.5 segundos

        // Si hay token guardado, ir al Home; si no, al Login
        if (token != null) {
            onNavigateToHome()
        } else {
            onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(UATBlue),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.alpha(alphaAnim)
        ) {
            // Ícono principal
            Icon(
                imageVector = Icons.Filled.School,
                contentDescription = "UATLife Logo",
                modifier = Modifier.size(100.dp),
                tint = UATOrange
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Nombre de la app
            Text(
                text = "UATLife",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = UATOnPrimaryLight
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtítulo
            Text(
                text = "Universidad Autónoma de Tamaulipas",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = UATOrange
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Campus Tampico",
                fontSize = 12.sp,
                fontWeight = FontWeight.Light,
                color = UATOnPrimaryLight.copy(alpha = 0.7f)
            )
        }
    }
}
