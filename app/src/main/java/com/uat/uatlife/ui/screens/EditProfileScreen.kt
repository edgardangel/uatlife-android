package com.uat.uatlife.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uat.uatlife.ui.theme.UATBlueDark
import com.uat.uatlife.ui.theme.UATOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    var nombre by remember { mutableStateOf("Alex Rivero") }
    var descripcion by remember { mutableStateOf("Estudiante de Ingeniería de Software • 6to Semestre") }
    var correoSecundario by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración de Perfil", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9FAFB))
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Avatar Section
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE5E7EB))
                        .border(3.dp, UATOrange, CircleShape)
                        .clickable { /* TODO: Change Image */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(60.dp))
                }
                
                // Camera Badge overlay
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = (-4).dp, y = (-4).dp)
                        .background(UATOrange, CircleShape)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = "Cambiar Foto", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Toca para cambiar foto", color = Color.Gray, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(32.dp))

            // Datos del Perfil
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("DATOS DEL PERFIL", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = UATBlueDark, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre Completo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción (Bio)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Privacidad y Seguridad
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("PRIVACIDAD Y RECUPERACIÓN", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = UATBlueDark, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = "a.rivero@uat.edu.mx",
                    onValueChange = { },
                    label = { Text("Correo Institucional (No modificable)") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = correoSecundario,
                    onValueChange = { correoSecundario = it },
                    label = { Text("Correo Secundario (Opcional)") },
                    placeholder = { Text("ejemplo@gmail.com") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono de Recuperación") },
                    placeholder = { Text("10 dígitos") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { onBack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UATOrange),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar Cambios", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
