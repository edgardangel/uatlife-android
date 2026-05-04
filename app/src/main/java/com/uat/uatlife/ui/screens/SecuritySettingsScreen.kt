package com.uat.uatlife.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
fun SecuritySettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = remember { RetrofitClient.getApiService(context) }

    // Valor guardado en BD (fuente de verdad)
    var correoGuardado by remember { mutableStateOf("") }
    // Valor editable temporal
    var correoEditando by remember { mutableStateOf("") }
    // Modo edición activado o no
    var modoEdicion by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    // Cargar info actual al entrar
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val resp = apiService.getProfile()
            if (resp.isSuccessful) {
                correoGuardado = resp.body()?.correoSecundario ?: ""
            }
        } catch (_: Exception) {}
        finally { isLoading = false }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacidad y Seguridad", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = UATBlue)
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = UATOrange)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9FAFB))
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Encabezado
            Text(
                text = "Opciones de Recuperación",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = UATBlueDark
            )
            Text(
                text = "Tu correo secundario sirve para recuperar tu contraseña si pierdes acceso a tu correo institucional.",
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            // Card del correo secundario
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Email,
                            contentDescription = null,
                            tint = UATOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Correo Secundario",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = UATBlueDark
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Modo LECTURA: muestra el correo + botón Cambiar
                    AnimatedVisibility(visible = !modoEdicion, enter = fadeIn(), exit = fadeOut()) {
                        Column {
                            // Recuadro con el correo actual (solo lectura)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFF3F4F6))
                                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                            ) {
                                if (correoGuardado.isEmpty()) {
                                    Text(
                                        text = "No hay correo secundario registrado",
                                        color = Color(0xFF9CA3AF),
                                        fontSize = 14.sp
                                    )
                                } else {
                                    Text(
                                        text = correoGuardado,
                                        color = Color(0xFF374151),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Botón Cambiar
                            OutlinedButton(
                                onClick = {
                                    correoEditando = correoGuardado
                                    modoEdicion = true
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = UATOrange),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    width = 1.5.dp
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (correoGuardado.isEmpty()) "Agregar correo" else "Cambiar correo",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Modo EDICIÓN: campo editable + Confirmar / Cancelar
                    AnimatedVisibility(visible = modoEdicion, enter = fadeIn(), exit = fadeOut()) {
                        Column {
                            OutlinedTextField(
                                value = correoEditando,
                                onValueChange = { correoEditando = it },
                                label = { Text("Nuevo correo secundario") },
                                placeholder = { Text("ej: mi_correo@gmail.com") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = UATOrange,
                                    unfocusedBorderColor = Color.LightGray,
                                    cursorColor = UATOrange,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Cancelar
                                OutlinedButton(
                                    onClick = {
                                        correoEditando = correoGuardado
                                        modoEdicion = false
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
                                ) {
                                    Text("Cancelar", fontWeight = FontWeight.SemiBold)
                                }

                                // Confirmar
                                Button(
                                    onClick = {
                                        scope.launch {
                                            isSaving = true
                                            try {
                                                val json = JSONObject()
                                                json.put(
                                                    "correo_secundario",
                                                    correoEditando.trim().takeIf { it.isNotEmpty() }
                                                )
                                                val body = json.toString()
                                                    .toRequestBody("application/json".toMediaTypeOrNull())
                                                val response = apiService.updateSecurityInfo(body)

                                                if (response.isSuccessful) {
                                                    correoGuardado = correoEditando.trim()
                                                    modoEdicion = false
                                                    Toast.makeText(
                                                        context,
                                                        "Correo secundario actualizado ✓",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "Error al guardar",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            } catch (e: Exception) {
                                                Toast.makeText(
                                                    context,
                                                    "Error de conexión",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } finally {
                                                isSaving = false
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = !isSaving,
                                    colors = ButtonDefaults.buttonColors(containerColor = UATOrange),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    if (isSaving) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text("Confirmar", fontWeight = FontWeight.SemiBold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Info extra
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = null,
                        tint = UATOrange,
                        modifier = Modifier.size(18.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Este correo solo se usará para enviarte un código de recuperación de contraseña. No será visible para otros usuarios.",
                        fontSize = 12.sp,
                        color = Color(0xFF92400E),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
