package com.uat.uatlife.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.uat.uatlife.network.models.Comunidad
import com.uat.uatlife.data.TokenManager
import com.uat.uatlife.network.models.Publicacion
import com.uat.uatlife.network.models.ReaccionRequest
import com.uat.uatlife.ui.components.*
import com.uat.uatlife.ui.theme.UATBlueDark
import com.uat.uatlife.ui.theme.UATOrange
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@Composable
fun CommunityDetailScreen(
    communityIdStr: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = remember { RetrofitClient.getApiService(context) }
    val tokenManager = remember { TokenManager(context) }
    val currentUserId by tokenManager.getUserId().collectAsState(initial = null)
    val currentUserName by tokenManager.getUserName().collectAsState(initial = null)
    val communityId = communityIdStr.toIntOrNull() ?: 0

    var comunidad by remember { mutableStateOf<Comunidad?>(null) }
    var publicaciones = remember { mutableStateListOf<Publicacion>() }
    var isLoading by remember { mutableStateOf(true) }
    var isPostsLoading by remember { mutableStateOf(true) }
    var showLeaveDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    var showCommentsForPostId by remember { mutableStateOf<Int?>(null) }

    // Estado para nueva publicación
    var nuevoTexto by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isPosting by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: Uri? -> selectedImageUri = uri }

    fun cargarDatos() {
        scope.launch {
            try {
                val respC = apiService.getComunidadById(communityId)
                if (respC.isSuccessful) comunidad = respC.body()
                
                val respP = apiService.getPublicaciones(comunidadId = communityId)
                if (respP.isSuccessful) {
                    publicaciones.clear()
                    publicaciones.addAll(respP.body() ?: emptyList())
                }
            } catch (e: Exception) {}
            finally { 
                isLoading = false
                isPostsLoading = false
            }
        }
    }

    LaunchedEffect(communityId) {
        cargarDatos()
    }

    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text("¿Salir de la comunidad?", fontWeight = FontWeight.Bold) },
            text = { Text("Dejarás de ver las publicaciones de este grupo en tu feed.") },
            confirmButton = {
                Button(
                    onClick = {
                        showLeaveDialog = false
                        scope.launch {
                            try {
                                val resp = apiService.salirDeComunidad(communityId)
                                if (resp.isSuccessful) {
                                    comunidad = comunidad?.copy(esMiembro = false, totalMiembros = (comunidad?.totalMiembros ?: 1) - 1)
                                    Toast.makeText(context, "Has salido de la comunidad", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {}
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                ) {
                    Text("Salir", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) { Text("Cancelar") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
    ) {
        // Banner y Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                // Banner Background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color(0xFF1E293B))
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.padding(top=32.dp, start=8.dp)) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                }

                // Profile Avatar Box overlapping
                Box(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .align(Alignment.BottomStart)
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .border(3.dp, Color.White, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Groups, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(40.dp))
                }
            }
        }

        // Info and Buttons Row
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(comunidad?.nombre ?: "Cargando...", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Public, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${comunidad?.tipo?.replaceFirstChar { it.uppercase() } ?: ""} • ${comunidad?.totalMiembros ?: 0} Miembros", color = Color.Gray, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    if (comunidad?.esMiembro == false) {
                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        val resp = apiService.unirseAComunidad(communityId)
                                        if (resp.isSuccessful) {
                                            comunidad = comunidad?.copy(esMiembro = true, totalMiembros = (comunidad?.totalMiembros ?: 0) + 1)
                                            Toast.makeText(context, "¡Te has unido!", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {}
                                }
                            },
                            modifier = Modifier.weight(1f).height(40.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = UATOrange),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Unirse a la comunidad", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Si ya está unido, mostramos un indicador discreto o nada aquí, 
                        // ya que el botón de salir estará abajo. Pero el usuario pidió salir al lado.
                        // "en lugar del boton grande que dice unido, ahi pon para hacer publicacion y al lado un boton de salir"
                        // Ok, moveré la lógica de publicación a un item del LazyColumn
                        Text("Eres miembro de esta comunidad", color = UATBlueDark, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Tabs
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = UATBlueDark
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("Feed", modifier = Modifier.padding(16.dp), fontWeight = if (selectedTab==0) FontWeight.Bold else FontWeight.Normal)
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("Miembros", modifier = Modifier.padding(16.dp), fontWeight = if (selectedTab==1) FontWeight.Bold else FontWeight.Normal)
                }
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                    Text("Info", modifier = Modifier.padding(16.dp), fontWeight = if (selectedTab==2) FontWeight.Bold else FontWeight.Normal)
                }
            }
            Divider(color = Color(0xFFE5E7EB))
        }

        // --- FEED ---
        if (selectedTab == 0) {
            if (comunidad?.esMiembro == true) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = nuevoTexto,
                                    onValueChange = { nuevoTexto = it },
                                    placeholder = { Text("Escribe algo para el grupo...", fontSize = 13.sp) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = UATOrange)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                                    Icon(Icons.Filled.Image, null, tint = if (selectedImageUri != null) UATOrange else Color.Gray)
                                }
                            }
                            
                            if (selectedImageUri != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp))) {
                                    AsyncImage(model = selectedImageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                    IconButton(onClick = { selectedImageUri = null }, modifier = Modifier.align(Alignment.TopEnd).size(20.dp).background(Color.Black.copy(alpha=0.5f), CircleShape)) {
                                        Icon(Icons.Filled.Close, null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = {
                                        if (nuevoTexto.isBlank() && selectedImageUri == null) return@Button
                                        isPosting = true
                                        scope.launch {
                                            try {
                                                val textPart = nuevoTexto.toRequestBody("text/plain".toMediaTypeOrNull())
                                                val comIdPart = communityId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                                                
                                                val imagePart = selectedImageUri?.let { uri ->
                                                    val inputStream = context.contentResolver.openInputStream(uri)
                                                    val bytes = inputStream?.readBytes() ?: return@let null
                                                    MultipartBody.Part.createFormData("imagen", "post.jpg", bytes.toRequestBody("image/jpeg".toMediaTypeOrNull()))
                                                }

                                                val resp = apiService.crearPublicacion(textPart, comIdPart, imagePart)
                                                if (resp.isSuccessful) {
                                                    nuevoTexto = ""
                                                    selectedImageUri = null
                                                    cargarDatos()
                                                    Toast.makeText(context, "Publicado con éxito", Toast.LENGTH_SHORT).show()
                                                }
                                            } catch (e: Exception) {}
                                            finally { isPosting = false }
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = UATOrange),
                                    enabled = !isPosting
                                ) {
                                    if (isPosting) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                                    else Text("Publicar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                TextButton(
                                    onClick = { showLeaveDialog = true },
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFE11D48))
                                ) {
                                    Text("Salir", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            if (isPostsLoading) {
                item { Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = UATOrange) } }
            } else if (publicaciones.isEmpty()) {
                item { Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { Text("Aún no hay publicaciones en esta comunidad.", color = Color.Gray, fontSize = 14.sp) } }
            } else {
                items(publicaciones, key = { it.id }) { pub ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        PostCard(
                            publicacion = pub,
                            esModerador = false, // TODO: Check if user is admin of community
                            esPropietario = (pub.autorId == currentUserId) || (pub.autorNombre == currentUserName),
                            onReaccion = {
                                scope.launch {
                                    try {
                                        apiService.reaccionar(pub.id, ReaccionRequest("like"))
                                        val idx = publicaciones.indexOfFirst { it.id == pub.id }
                                        if (idx >= 0) publicaciones[idx] = publicaciones[idx].copy(totalReacciones = publicaciones[idx].totalReacciones + 1, miReaccion = "like")
                                    } catch (e: Exception) {}
                                }
                            },
                            onComentar = { showCommentsForPostId = pub.id },
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
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCommentsForPostId != null) {
        CommentsBottomSheet(
            postId = showCommentsForPostId!!,
            apiService = apiService,
            onDismiss = { showCommentsForPostId = null },
            onCommentAdded = {
                val idx = publicaciones.indexOfFirst { it.id == showCommentsForPostId }
                if (idx >= 0) publicaciones[idx] = publicaciones[idx].copy(totalComentarios = publicaciones[idx].totalComentarios + 1)
            }
        )
    }
}

