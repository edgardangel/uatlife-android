package com.uat.uatlife.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uat.uatlife.data.TokenManager
import com.uat.uatlife.network.RetrofitClient
import com.uat.uatlife.network.models.LoginRequest
import com.uat.uatlife.ui.theme.UATBlue
import com.uat.uatlife.ui.theme.UATBlueDark
import com.uat.uatlife.ui.theme.UATBlueLight
import com.uat.uatlife.ui.theme.UATOrange
import com.uat.uatlife.ui.theme.UATOnPrimaryLight
import com.uat.uatlife.ui.theme.UATSurfaceLight
import kotlinx.coroutines.launch

/**
 * Pantalla de inicio de sesión.
 * Permite al usuario autenticarse con su matrícula/correo y contraseña.
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tokenManager = remember { TokenManager(context) }
    val apiService = remember { RetrofitClient.getApiService(context) }

    var identificador by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = UATOrange,
        unfocusedBorderColor = UATBlueLight.copy(alpha = 0.5f),
        focusedLabelColor = UATOrange,
        unfocusedLabelColor = UATOnPrimaryLight.copy(alpha = 0.6f),
        cursorColor = UATOrange,
        focusedTextColor = UATOnPrimaryLight,
        unfocusedTextColor = UATOnPrimaryLight.copy(alpha = 0.8f)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(UATBlue)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Logo
            Icon(
                imageVector = Icons.Filled.School,
                contentDescription = "UATLife",
                modifier = Modifier.size(80.dp),
                tint = UATOrange
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "UATLife",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = UATOnPrimaryLight
            )

            Text(
                text = "Inicia sesión con tu cuenta UAT",
                fontSize = 14.sp,
                color = UATOnPrimaryLight.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Campo: Correo Institucional
            OutlinedTextField(
                value = identificador,
                onValueChange = { identificador = it },
                label = { Text("Correo institucional") },
                placeholder = { Text("ej: a2210000@alumnos.uat.edu.mx") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                colors = textFieldColors,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo: Contraseña
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible) "Ocultar" else "Mostrar",
                            tint = UATOrange
                        )
                    }
                },
                colors = textFieldColors,
                shape = RoundedCornerShape(12.dp)
            )

            // Olvidaste tu contraseña
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "¿Olvidaste tu contraseña?",
                    color = UATOrange,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable {
                        onNavigateToForgotPassword()
                    }
                )
            }

            // Botón de Login
            Button(
                onClick = {
                    if (identificador.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isLoading = true
                    scope.launch {
                        try {
                            val response = apiService.login(
                                LoginRequest(identificador.trim(), password)
                            )
                            if (response.isSuccessful) {
                                val body = response.body()!!
                                tokenManager.saveSession(
                                    token = body.token,
                                    nombre = body.usuario.nombreCompleto,
                                    matricula = body.usuario.matricula,
                                    tipoUsuario = body.usuario.tipoUsuario,
                                    userId = body.usuario.id,
                                    banPermanente = body.usuario.banPermanente,
                                    suspensionHasta = body.usuario.suspensionHasta
                                )
                                Toast.makeText(context, body.mensaje, Toast.LENGTH_SHORT).show()
                                onLoginSuccess()
                            } else {
                                val errorMsg = when (response.code()) {
                                    401 -> "Credenciales incorrectas"
                                    else -> "Error al iniciar sesión"
                                }
                                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error de conexión: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = UATOrange,
                    contentColor = UATBlueDark
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = UATBlueDark,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Iniciar Sesión",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Link a Registro
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "¿No tienes cuenta? ",
                    color = UATOnPrimaryLight.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                Text(
                    text = "Regístrate aquí",
                    color = UATOrange,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToRegister() }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Footer
            Text(
                text = "Universidad Autónoma de Tamaulipas\nCampus Tampico",
                fontSize = 11.sp,
                color = UATOnPrimaryLight.copy(alpha = 0.4f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
