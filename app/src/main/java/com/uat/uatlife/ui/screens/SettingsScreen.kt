package com.uat.uatlife.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uat.uatlife.ui.theme.UATBlueDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = UATBlueDark) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = UATBlueDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF9FAFB))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9FAFB))
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            
            // Menu Tarjeta
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column {
                    SettingsOptionRow(
                        icon = Icons.Outlined.Lock,
                        label = "Privacidad y Seguridad",
                        onClick = { /* TODO */ }
                    )
                    Divider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                    
                    SettingsOptionRow(
                        icon = Icons.Outlined.Notifications,
                        label = "Notificaciones",
                        onClick = { /* TODO */ }
                    )
                    Divider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                    
                    SettingsOptionRow(
                        icon = Icons.Outlined.HelpOutline,
                        label = "Ayuda y Soporte",
                        onClick = { /* TODO */ }
                    )
                    Divider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                    
                    SettingsOptionRow(
                        icon = Icons.Outlined.Info,
                        label = "Acerca de UATLife",
                        onClick = { /* TODO */ }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Logout Button
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFE4E6)), // Rojo muy claro
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Logout,
                        contentDescription = null,
                        tint = Color(0xFFE11D48), // Rojo oscuro
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "CERRAR SESIÓN",
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE11D48),
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 15.sp,
            color = Color.DarkGray,
            modifier = Modifier.weight(1f)
        )
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.LightGray)
    }
}
