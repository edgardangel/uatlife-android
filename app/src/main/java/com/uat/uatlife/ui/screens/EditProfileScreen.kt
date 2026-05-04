package com.uat.uatlife.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.uat.uatlife.network.RetrofitClient
import com.uat.uatlife.ui.theme.UATBlueDark
import com.uat.uatlife.ui.theme.UATOrange
import com.uat.uatlife.utils.ImageUtils
import kotlinx.coroutines.launch
import coil.ImageLoader
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = remember { RetrofitClient.getApiService(context) }

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .okHttpClient { RetrofitClient.getUnsafeOkHttpClient() }
            .build()
    }

    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var correoInstitucional by remember { mutableStateOf("") }
    
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var currentPhotoUrl by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    // Cargar perfil actual
    LaunchedEffect(Unit) {
        try {
            val response = apiService.getProfile()
            if (response.isSuccessful) {
                val profile = response.body()
                if (profile != null) {
                    nombre = profile.nombreCompleto
                    descripcion = profile.bio ?: ""
                    correoInstitucional = profile.correoInstitucional
                    currentPhotoUrl = profile.urlFotoPerfil
                }
            } else {
                Toast.makeText(context, "Error al cargar perfil", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

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
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            imageLoader = imageLoader
                        )
                    } else if (!currentPhotoUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = "https://bd-uat-bus-api-uatlife-xazfaa-1b2660-157-245-239-94.traefik.me${currentPhotoUrl}",
                            contentDescription = "Foto actual",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            imageLoader = imageLoader
                        )
                    } else {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(60.dp))
                    }
                }
                
                // Camera Badge overlay
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = (-4).dp, y = (-4).dp)
                        .background(UATOrange, CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                        .clickable { imagePickerLauncher.launch("image/*") },
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
                    value = correoInstitucional,
                    onValueChange = { },
                    label = { Text("Correo Institucional (No modificable)") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    isSaving = true
                    scope.launch {
                        try {
                            val nombreBody = nombre.toRequestBody("text/plain".toMediaTypeOrNull())
                            val bioBody = descripcion.toRequestBody("text/plain".toMediaTypeOrNull())
                            
                            val fotoPart = selectedImageUri?.let { uri ->
                                ImageUtils.uriToMultipart(context, uri, "foto")
                            }

                            val response = apiService.updateProfile(nombreBody, bioBody, fotoPart)
                            if (response.isSuccessful) {
                                Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                                onBack()
                            } else {
                                val errorStr = response.errorBody()?.string() ?: ""
                                Toast.makeText(context, "Error ${response.code()}: $errorStr", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            isSaving = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UATOrange),
                shape = RoundedCornerShape(12.dp),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Guardar Cambios", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
