package com.uat.uatlife.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.uat.uatlife.data.TokenManager
import com.uat.uatlife.network.RetrofitClient
import com.uat.uatlife.network.models.CrearPublicacionRequest
import com.uat.uatlife.network.models.Publicacion
import com.uat.uatlife.network.models.ReaccionRequest
import com.uat.uatlife.ui.components.*
import com.uat.uatlife.ui.theme.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Pantalla principal - Feed de publicaciones conectado a la API real.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tokenManager = remember { TokenManager(context) }
    val apiService = remember { RetrofitClient.getApiService(context) }
    val userType by tokenManager.getUserType().collectAsState(initial = "alumno")
    val currentUserId by tokenManager.getUserId().collectAsState(initial = null)
    val currentUserName by tokenManager.getUserName().collectAsState(initial = null)

    // Estados del Feed
    val publicaciones = remember { mutableStateListOf<Publicacion>() }
    var isLoadingFeed by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Estados de búsqueda
    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val recentSearches = remember { mutableStateListOf("Venta de libros", "Tutorías cálculo", "Eventos FADU") }

    var showCreateDialog by remember { mutableStateOf(false) }
    var nuevoTexto by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var isPosting by remember { mutableStateOf(false) }

    // Estado para comentarios
    var showCommentsForPostId by remember { mutableStateOf<Int?>(null) }

    // --- Función para cargar el feed ---
    fun cargarFeed() {
        scope.launch {
            isRefreshing = true
            errorMsg = null
            try {
                val response = apiService.getPublicaciones()
                if (response.isSuccessful) {
                    val lista = response.body() ?: emptyList()
                    publicaciones.clear()
                    publicaciones.addAll(lista)
                } else {
                    errorMsg = "No se pudo cargar el feed"
                }
            } catch (e: Exception) {
                errorMsg = "Sin conexión. Verifica tu red."
            } finally {
                isLoadingFeed = false
                isRefreshing = false
            }
        }
    }

    // Cargar al entrar
    LaunchedEffect(Unit) { cargarFeed() }

    // Publicaciones filtradas por búsqueda
    val publicacionesFiltradas = if (searchQuery.isBlank()) {
        publicaciones
    } else {
        publicaciones.filter {
            it.contenidoTexto?.contains(searchQuery, ignoreCase = true) == true ||
            it.autorNombre.contains(searchQuery, ignoreCase = true)
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) selectedImageUri = uri
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false; nuevoTexto = ""; selectedImageUri = null },
            title = { Text("Nueva Publicación", fontWeight = FontWeight.Bold, color = UATBlueDark) },
            text = {
                Column {
                    OutlinedTextField(
                        value = nuevoTexto,
                        onValueChange = { nuevoTexto = it },
                        placeholder = { Text("¿Qué está pasando en el campus?") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = UATOrange,
                            unfocusedBorderColor = UATBlueLight.copy(alpha = 0.4f)
                        ),
                        maxLines = 5
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Vista previa de imagen seleccionada
                    if (selectedImageUri != null) {
                        Box(modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(8.dp))) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                            IconButton(
                                onClick = { selectedImageUri = null },
                                modifier = Modifier.align(Alignment.TopEnd).background(Color.Black.copy(0.5f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, null, tint = Color.White)
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, UATBlueLight.copy(0.3f))
                        ) {
                            Icon(Icons.Default.Image, null, tint = UATOrange)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Agregar Foto", color = UATBlueDark)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nuevoTexto.isBlank() && selectedImageUri == null) return@Button
                        isPosting = true
                        scope.launch {
                            try {
                                val textBody = nuevoTexto.trim().toRequestBody("text/plain".toMediaTypeOrNull())
                                val imagePart = if (selectedImageUri != null) {
                                    com.uat.uatlife.utils.ImageUtils.uriToMultipart(context, selectedImageUri!!, "imagen")
                                } else null

                                val response = apiService.crearPublicacion(
                                    textBody, null, imagePart
                                )
                                if (response.isSuccessful) {
                                    showCreateDialog = false
                                    nuevoTexto = ""
                                    selectedImageUri = null
                                    cargarFeed()
                                } else {
                                    Toast.makeText(context, "Error al publicar", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            } finally {
                                isPosting = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UATOrange),
                    enabled = !isPosting
                ) {
                    if (isPosting) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    else Text("Publicar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false; nuevoTexto = ""; selectedImageUri = null }) {
                    Text("Cancelar", color = UATBlueDark)
                }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().background(UATSurfaceLight)
    ) {
        // ── Top Bar ──────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(UATBlue)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSearching) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar publicación...", color = Color.White.copy(alpha = 0.7f)) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Cerrar búsqueda",
                    tint = UATOnPrimaryLight,
                    modifier = Modifier.size(28.dp).clip(CircleShape).clickable {
                        isSearching = false
                        searchQuery = ""
                    }
                )
            } else {
                Text(
                    text = "UATLife",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = UATOnPrimaryLight,
                    modifier = Modifier.weight(1f)
                )
                // Refrescar
                IconButton(onClick = { cargarFeed() }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Actualizar", tint = UATOnPrimaryLight, modifier = Modifier.size(22.dp))
                }
                // Buscar
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Buscar",
                    tint = UATOnPrimaryLight,
                    modifier = Modifier.size(28.dp).clip(CircleShape).clickable { isSearching = true }
                )
            }
        }

        // ── Contenido del Feed ───────────────────────────────────
        when {
            // Modo búsqueda con historial
            isSearching && searchQuery.isBlank() -> {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        Text("Búsquedas recientes", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                            color = UATBlueDark, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    items(recentSearches) { search ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { searchQuery = search }.padding(vertical = 8.dp)
                        ) {
                            Icon(Icons.Filled.History, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(search, fontSize = 15.sp, color = Color.DarkGray, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Cargando
            isLoadingFeed -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = UATOrange, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Cargando publicaciones...", color = UATBlueDark.copy(alpha = 0.6f), fontSize = 14.sp)
                    }
                }
            }

            // Error de red
            errorMsg != null && publicaciones.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Icon(Icons.Filled.WifiOff, null, tint = UATBlueLight, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(errorMsg!!, color = UATBlueDark, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { cargarFeed() }, colors = ButtonDefaults.buttonColors(containerColor = UATOrange)) {
                            Text("Reintentar", color = Color.White)
                        }
                    }
                }
            }

            // Feed principal (con o sin búsqueda activa)
            else -> {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    // Caja Crear Publicación
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { showCreateDialog = true },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(40.dp).clip(CircleShape).background(UATBlueLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Person, null, tint = UATOnPrimaryLight, modifier = Modifier.size(24.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Box(
                                    modifier = Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(22.dp)).background(UATSurfaceLight),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text("¿Qué está pasando en el campus?",
                                        color = UATBlueLight.copy(alpha = 0.8f), fontSize = 14.sp,
                                        modifier = Modifier.padding(horizontal = 16.dp))
                                }
                            }
                        }
                    }

                    // Título de sección
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Muro UATLife", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = UATBlueDark)
                            if (isRefreshing) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = UATOrange, strokeWidth = 2.dp)
                            }
                        }
                    }

                    // Sin resultados en búsqueda
                    if (publicacionesFiltradas.isEmpty() && searchQuery.isNotBlank()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("No hay publicaciones que coincidan con \"$searchQuery\"",
                                    color = Color.Gray, fontSize = 14.sp)
                            }
                        }
                    }

                    // Feed vacío (sin publicaciones en la BD aún)
                    if (publicaciones.isEmpty() && !isLoadingFeed) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Filled.DynamicFeed, null, tint = UATBlueLight, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Aún no hay publicaciones.\n¡Sé el primero en publicar!",
                                        color = Color.Gray, fontSize = 14.sp)
                                }
                            }
                        }
                    }

                    // Tarjetas del Feed
                    items(publicacionesFiltradas, key = { it.id }) { pub ->
                        PostCard(
                            publicacion = pub,
                            esModerador = userType == "moderador",
                            esPropietario = (pub.autorId == currentUserId) || (pub.autorNombre == currentUserName),
                            onReaccion = {
                                scope.launch {
                                    try {
                                        apiService.reaccionar(pub.id, ReaccionRequest("like"))
                                        // Actualizar contador localmente para feedback inmediato
                                        val idx = publicaciones.indexOfFirst { it.id == pub.id }
                                        if (idx >= 0) {
                                            val updated = publicaciones[idx].copy(
                                                totalReacciones = publicaciones[idx].totalReacciones + 1
                                            )
                                            publicaciones[idx] = updated
                                        }
                                    } catch (e: Exception) { /* Silencioso */ }
                                }
                            },
                            onComentar = {
                                showCommentsForPostId = pub.id
                            },
                            onEliminar = {
                                scope.launch {
                                    try {
                                        val resp = apiService.eliminarPublicacion(pub.id)
                                        if (resp.isSuccessful) {
                                            publicaciones.remove(pub)
                                            Toast.makeText(context, "Publicación eliminada", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onReportar = { motivo, desc ->
                                scope.launch {
                                    try {
                                        val resp = apiService.crearReporte(CrearReporteRequest("publicacion", pub.id, motivo, desc))
                                        if (resp.isSuccessful) {
                                            Toast.makeText(context, "Reporte enviado correctamente", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "No se pudo enviar el reporte", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error de red", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }

                }
            }
        }

        // --- BottomSheet de Comentarios ---
        if (showCommentsForPostId != null) {
            CommentsBottomSheet(
                postId = showCommentsForPostId!!,
                apiService = apiService,
                onDismiss = { showCommentsForPostId = null },
                onCommentAdded = {
                    // Actualizar contador local
                    val idx = publicaciones.indexOfFirst { it.id == showCommentsForPostId }
                    if (idx >= 0) {
                        publicaciones[idx] = publicaciones[idx].copy(
                            totalComentarios = publicaciones[idx].totalComentarios + 1
                        )
                    }
                }
            )
        }
    }
}

