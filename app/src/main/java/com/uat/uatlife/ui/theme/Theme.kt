package com.uat.uatlife.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Esquema de colores claro con identidad UAT
private val UATLightColorScheme = lightColorScheme(
    primary = UATBlue,
    onPrimary = UATOnPrimaryLight,
    primaryContainer = UATContainerLight,
    onPrimaryContainer = UATBlueDark,
    secondary = UATOrange,
    onSecondary = UATOnPrimaryLight,
    secondaryContainer = UATOrangeContainer,
    onSecondaryContainer = UATOnOrangeContainer,
    tertiary = UATBlueLight,
    background = UATBackgroundLight,
    onBackground = UATOnBackgroundLight,
    surface = UATSurfaceLight,
    onSurface = UATOnSurfaceLight,
    surfaceVariant = UATContainerLight,
    onSurfaceVariant = UATBlueLight,
    error = UATError,
    onError = UATOnError,
    outline = UATOutlineLight
)

// Esquema de colores oscuro con identidad UAT
private val UATDarkColorScheme = darkColorScheme(
    primary = UATOrangeLight,
    onPrimary = UATBlueDark,
    primaryContainer = UATBlueVariant,
    onPrimaryContainer = UATOrangeLight,
    secondary = UATOrange,
    onSecondary = UATBlueDark,
    secondaryContainer = UATOrangeDark,
    onSecondaryContainer = UATOrangeLight,
    tertiary = UATBlueLight,
    background = UATBackgroundDark,
    onBackground = UATOnBackgroundDark,
    surface = UATSurfaceDark,
    onSurface = UATOnSurfaceDark,
    surfaceVariant = UATContainerDark,
    onSurfaceVariant = UATOrangeLight,
    error = UATError,
    onError = UATOnError,
    outline = UATOutlineDark
)

@Composable
fun UATLifeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Siempre usar los colores institucionales UAT (no dynamic color)
    val colorScheme = if (darkTheme) UATDarkColorScheme else UATLightColorScheme

    // Configurar la barra de estado con colores UAT
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = UATBlue.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}