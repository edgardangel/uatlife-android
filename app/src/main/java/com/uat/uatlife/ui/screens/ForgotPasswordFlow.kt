package com.uat.uatlife.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uat.uatlife.network.RetrofitClient
import com.uat.uatlife.ui.theme.UATBlue
import com.uat.uatlife.ui.theme.UATBlueDark
import com.uat.uatlife.ui.theme.UATOrange
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    onNavigateToVerifyOtp: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = remember { RetrofitClient.getApiService(context) }

    var identificador by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = UATOrange,
        unfocusedBorderColor = Color.LightGray,
        cursorColor = UATOrange,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recuperar Cuenta", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = UATBlue)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9FAFB))
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "Ingresa tu Matrícula o Correo Institucional",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = UATBlueDark,
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                text = "Te enviaremos un código de recuperación a tu correo secundario registrado.",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 24.dp)
            )

            OutlinedTextField(
                value = identificador,
                onValueChange = { identificador = it },
                label = { Text("Matrícula o Correo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                colors = textFieldColors,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (identificador.isBlank()) {
                        Toast.makeText(context, "Ingresa tu identificador", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    scope.launch {
                        isLoading = true
                        try {
                            val json = JSONObject().apply { put("identificador", identificador.trim()) }
                            val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
                            val response = apiService.forgotPassword(body)
                            
                            if (response.isSuccessful) {
                                Toast.makeText(context, "Código enviado", Toast.LENGTH_SHORT).show()
                                onNavigateToVerifyOtp(identificador.trim())
                            } else {
                                val errorStr = response.errorBody()?.string() ?: ""
                                Toast.makeText(context, "Error: $errorStr", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = UATOrange),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Enviar Código", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyOtpScreen(
    identificador: String,
    onBack: () -> Unit,
    onNavigateToReset: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = remember { RetrofitClient.getApiService(context) }

    var codigo by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = UATOrange,
        unfocusedBorderColor = Color.LightGray,
        cursorColor = UATOrange,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verificar Código", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = UATBlue)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9FAFB))
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "Ingresa el Código",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = UATBlueDark,
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                text = "Hemos enviado un código de 6 dígitos a tu correo secundario. Ingrésalo a continuación.",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 24.dp)
            )

            OutlinedTextField(
                value = codigo,
                onValueChange = { if (it.length <= 6) codigo = it },
                label = { Text("Código de 6 dígitos") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                colors = textFieldColors,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (codigo.length != 6) {
                        Toast.makeText(context, "El código debe tener 6 dígitos", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    scope.launch {
                        isLoading = true
                        try {
                            val json = JSONObject().apply { 
                                put("identificador", identificador)
                                put("codigo", codigo)
                            }
                            val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
                            val response = apiService.verifyOtp(body)
                            
                            if (response.isSuccessful) {
                                val resetToken = response.body()?.resetToken
                                if (resetToken != null) {
                                    onNavigateToReset(resetToken)
                                } else {
                                    Toast.makeText(context, "Error: Token no recibido", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                val errorStr = response.errorBody()?.string() ?: ""
                                Toast.makeText(context, "Código inválido", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading && codigo.length == 6,
                colors = ButtonDefaults.buttonColors(containerColor = UATOrange),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Verificar", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    resetToken: String,
    onBackToLogin: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = remember { RetrofitClient.getApiService(context) }

    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = UATOrange,
        unfocusedBorderColor = Color.LightGray,
        cursorColor = UATOrange,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Contraseña", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = UATBlue)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9FAFB))
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "Crea una nueva contraseña",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = UATBlueDark,
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                text = "La contraseña debe tener al menos 6 caracteres.",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 24.dp)
            )

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("Nueva Contraseña") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                colors = textFieldColors,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirmar Contraseña") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                colors = textFieldColors,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (newPassword.length < 6) {
                        Toast.makeText(context, "La contraseña debe tener 6 caracteres", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (newPassword != confirmPassword) {
                        Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    scope.launch {
                        isLoading = true
                        try {
                            val json = JSONObject().apply { 
                                put("resetToken", resetToken)
                                put("newPassword", newPassword)
                            }
                            val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
                            val response = apiService.resetPassword(body)
                            
                            if (response.isSuccessful) {
                                Toast.makeText(context, "Contraseña actualizada exitosamente", Toast.LENGTH_LONG).show()
                                onBackToLogin()
                            } else {
                                Toast.makeText(context, "Error al actualizar contraseña", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = UATOrange),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Cambiar Contraseña", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
