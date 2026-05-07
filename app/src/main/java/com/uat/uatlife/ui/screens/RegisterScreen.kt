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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uat.uatlife.data.TokenManager
import com.uat.uatlife.network.RetrofitClient
import com.uat.uatlife.network.models.ParadaAutobus
import com.uat.uatlife.network.models.RegisterRequest
import com.uat.uatlife.ui.theme.UATBlue
import com.uat.uatlife.ui.theme.UATBlueDark
import com.uat.uatlife.ui.theme.UATBlueLight
import com.uat.uatlife.ui.theme.UATOrange
import com.uat.uatlife.ui.theme.UATOnPrimaryLight
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import com.uat.uatlife.utils.ImageUtils
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody


/**
 * Pantalla de registro de nuevo alumno.
 * Valida correo institucional y recopila datos para validación por moderador.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tokenManager = remember { TokenManager(context) }
    val apiService = remember { RetrofitClient.getApiService(context) }

    var nombreCompleto by remember { mutableStateOf("") }
    var matricula by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var facultadId by remember { mutableIntStateOf(0) }
    var facultadExpanded by remember { mutableStateOf(false) }
    var facultadLabel by remember { mutableStateOf("Selecciona tu facultad") }
    var aceptaTerminos by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var fichaPagoUri by remember { mutableStateOf<Uri?>(null) }

    // Lista de facultades cargada desde la API
    val facultades = remember { mutableStateListOf<ParadaAutobus>() }
    var facultadesLoading by remember { mutableStateOf(true) }

    // Cargar facultades al entrar a la pantalla
    LaunchedEffect(Unit) {
        try {
            val response = apiService.getParadas()
            if (response.isSuccessful) {
                val lista = response.body() ?: emptyList()
                facultades.clear()
                facultades.addAll(lista)
            }
        } catch (e: Exception) {
            // Si falla la red, el dropdown quedará vacío con mensaje informativo
        } finally {
            facultadesLoading = false
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        fichaPagoUri = uri
    }

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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Botón atrás
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onNavigateToLogin() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = UATOnPrimaryLight
                    )
                }
            }

            // Título
            Text(
                text = "Crear Cuenta",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = UATOnPrimaryLight
            )
            Text(
                text = "Regístrate con tu correo institucional",
                fontSize = 13.sp,
                color = UATOnPrimaryLight.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Nombre completo
            OutlinedTextField(
                value = nombreCompleto,
                onValueChange = { nombreCompleto = it },
                label = { Text("Nombre completo") },
                placeholder = { Text("ej: Juan Pérez López") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                colors = textFieldColors,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Matrícula
            OutlinedTextField(
                value = matricula,
                onValueChange = { matricula = it },
                label = { Text("Matrícula") },
                placeholder = { Text("ej: a2210000") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                colors = textFieldColors,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Correo institucional
            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo institucional") },
                placeholder = { Text("tu.correo@alumnos.uat.edu.mx") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                colors = textFieldColors,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Dropdown de Facultad
            ExposedDropdownMenuBox(
                expanded = facultadExpanded,
                onExpandedChange = { facultadExpanded = !facultadExpanded }
            ) {
                OutlinedTextField(
                    value = facultadLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Facultad") },
                    trailingIcon = {
                        if (facultadesLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = UATOrange)
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = facultadExpanded)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = textFieldColors,
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = facultadExpanded,
                    onDismissRequest = { facultadExpanded = false }
                ) {
                    if (facultades.isEmpty() && !facultadesLoading) {
                        DropdownMenuItem(
                            text = { Text("No se pudieron cargar las facultades", color = UATOrange) },
                            onClick = { facultadExpanded = false }
                        )
                    } else {
                        facultades.forEach { facultad ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(facultad.nombre, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                        if (!facultad.abreviatura.isNullOrBlank()) {
                                            Text(facultad.abreviatura, fontSize = 11.sp, color = UATOrange)
                                        }
                                    }
                                },
                                onClick = {
                                    facultadId = facultad.id
                                    facultadLabel = facultad.nombre
                                    facultadExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Contraseña
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                placeholder = { Text("Mínimo 6 caracteres") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = null,
                            tint = UATOrange
                        )
                    }
                },
                colors = textFieldColors,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Confirmar contraseña
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirmar contraseña") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = null,
                            tint = UATOrange
                        )
                    }
                },
                colors = textFieldColors,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subir Ficha de Pago
            Text(
                text = "Ficha de pago (Obligatorio)",
                color = UATOnPrimaryLight,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(
                        color = if (fichaPagoUri != null) UATBlueLight.copy(alpha = 0.2f) else UATBlueDark.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        filePickerLauncher.launch("*/*")
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (fichaPagoUri != null) Icons.Filled.CheckCircle else Icons.Filled.UploadFile,
                        contentDescription = null,
                        tint = if (fichaPagoUri != null) UATOrange else UATOnPrimaryLight.copy(alpha = 0.6f),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (fichaPagoUri != null) "Archivo seleccionado" else "Toca para agregar ficha de pago",
                            color = if (fichaPagoUri != null) UATOrange else UATOnPrimaryLight,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Formato PDF o Imagen (Máx. 5MB)",
                            color = UATOnPrimaryLight.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Términos y condiciones
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = aceptaTerminos,
                    onCheckedChange = { aceptaTerminos = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = UATOrange,
                        uncheckedColor = UATOnPrimaryLight.copy(alpha = 0.5f),
                        checkmarkColor = UATBlueDark
                    )
                )
                Text(
                    text = "Acepto los Términos y Condiciones de uso de UATLife",
                    fontSize = 12.sp,
                    color = UATOnPrimaryLight.copy(alpha = 0.7f),
                    modifier = Modifier.clickable { aceptaTerminos = !aceptaTerminos }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de Registro
            Button(
                onClick = {
                    // Validaciones locales
                    when {
                        nombreCompleto.isBlank() || matricula.isBlank() || correo.isBlank() || password.isBlank() ->
                            Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                        fichaPagoUri == null ->
                            Toast.makeText(context, "Debes subir tu ficha de pago para continuar", Toast.LENGTH_SHORT).show()
                        !correo.trim().endsWith("@alumnos.uat.edu.mx") ->
                            Toast.makeText(context, "Usa tu correo institucional (@alumnos.uat.edu.mx)", Toast.LENGTH_SHORT).show()
                        password.length < 6 ->
                            Toast.makeText(context, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                        password != confirmPassword ->
                            Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                        !aceptaTerminos ->
                            Toast.makeText(context, "Debes aceptar los Términos y Condiciones", Toast.LENGTH_SHORT).show()
                        else -> {
                            isLoading = true
                            scope.launch {
                                try {
                                    val nombreBody = nombreCompleto.trim().toRequestBody("text/plain".toMediaTypeOrNull())
                                    val matriculaBody = matricula.trim().toRequestBody("text/plain".toMediaTypeOrNull())
                                    val correoBody = correo.trim().toRequestBody("text/plain".toMediaTypeOrNull())
                                    val passBody = password.toRequestBody("text/plain".toMediaTypeOrNull())
                                    val facuBody = if (facultadId > 0) facultadId.toString().toRequestBody("text/plain".toMediaTypeOrNull()) else null
                                    
                                    val fichaPart = fichaPagoUri?.let { ImageUtils.uriToMultipart(context, it, "ficha") }

                                    val response = apiService.register(
                                        nombre = nombreBody,
                                        matricula = matriculaBody,
                                        correo = correoBody,
                                        password = passBody,
                                        facultadId = facuBody,
                                        ficha = fichaPart
                                    )
                                    if (response.isSuccessful) {
                                        val body = response.body()!!
                                        tokenManager.saveSession(
                                            token = body.token,
                                            nombre = body.usuario.nombreCompleto,
                                            matricula = body.usuario.matricula,
                                            tipoUsuario = body.usuario.tipoUsuario,
                                            userId = body.usuario.id
                                        )
                                        Toast.makeText(context, body.mensaje, Toast.LENGTH_LONG).show()
                                        onRegisterSuccess()
                                    } else {
                                        val errorMsg = when (response.code()) {
                                            409 -> "La matrícula o correo ya están registrados"
                                            400 -> "Datos inválidos, revisa tus campos"
                                            else -> "Error al registrar"
                                        }
                                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error de conexión: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                } finally {
                                    isLoading = false
                                }
                            }
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
                        text = "Crear Cuenta",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Link a Login
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "¿Ya tienes cuenta? ",
                    color = UATOnPrimaryLight.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                Text(
                    text = "Inicia sesión",
                    color = UATOrange,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
