package com.uat.uatlife.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.uat.uatlife.ui.theme.UATBlue
import com.uat.uatlife.ui.theme.UATOrange

/**
 * Datos para cada ítem de la barra de navegación inferior.
 */
data class BottomNavItem(
    val label: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

/**
 * Lista de los 5 destinos principales de navegación.
 */
val bottomNavItems = listOf(
    BottomNavItem(
        label = "Inicio",
        route = Screen.Home.route,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    BottomNavItem(
        label = "Comunidad",
        route = Screen.Communities.route,
        selectedIcon = Icons.Filled.Groups,
        unselectedIcon = Icons.Outlined.Groups
    ),
    BottomNavItem(
        label = "Market",
        route = Screen.Market.route,
        selectedIcon = Icons.Filled.Store,
        unselectedIcon = Icons.Outlined.Store
    ),
    BottomNavItem(
        label = "Bus",
        route = Screen.BusTracker.route,
        selectedIcon = Icons.Filled.DirectionsBus,
        unselectedIcon = Icons.Outlined.DirectionsBus
    ),
    BottomNavItem(
        label = "Chat",
        route = Screen.Messages.route,
        selectedIcon = Icons.Filled.Message,
        unselectedIcon = Icons.Outlined.Message
    ),
    BottomNavItem(
        label = "Perfil",
        route = Screen.Profile.route,
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
)

/**
 * Barra de navegación inferior con los colores institucionales de la UAT.
 * Azul de fondo, naranja para el ítem seleccionado.
 */
@Composable
fun BottomNavBar(
    currentRoute: String?,
    onItemClick: (String) -> Unit
) {
    NavigationBar(
        containerColor = UATBlue,
        contentColor = UATOrange
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = currentRoute == item.route

            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemClick(item.route) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = UATOrange,
                    selectedTextColor = UATOrange,
                    unselectedIconColor = UATOrange.copy(alpha = 0.5f),
                    unselectedTextColor = UATOrange.copy(alpha = 0.5f),
                    indicatorColor = UATBlue.copy(alpha = 0.3f)
                )
            )
        }
    }
}
