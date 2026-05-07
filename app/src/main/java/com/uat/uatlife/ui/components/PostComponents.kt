package com.uat.uatlife.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Groups
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
import com.uat.uatlife.network.models.*
import com.uat.uatlife.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun PostCard(
    publicacion: Publicacion,
    esModerador: Boolean,
    esPropietario: Boolean = false,
    onReaccion: () -> Unit,
    onComentar: () -> Unit,
    onEliminar: () -> Unit,
    onReportar: (motivo: String, desc: String) -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("¿Eliminar publicación?", fontWeight = FontWeight.Bold) },
            text = { Text("Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(onClick = { showDeleteConfirm = false; onEliminar() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))) {
                    Text("Eliminar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancelar") }
            }
        )
    }

    if (showReportDialog) {
        ReportDialog(
            onDismiss = { showReportDialog = false },
            onConfirm = { motivo, desc ->
                showReportDialog = false
                onReportar(motivo, desc)
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(UATBlueLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Person, null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(publicacion.autorNombre, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = UATBlueDark)
                        if (publicacion.autorTipo == "moderador") {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Filled.Security, null, tint = UATOrange, modifier = Modifier.size(14.dp))
                        }
                    }
                    Text(
                        text = "${publicacion.autorFacultad ?: ""} • ${formatFecha(publicacion.fechaCreacion)}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    if (!publicacion.comunidadNombre.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Groups, null, tint = UATOrange, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "en ${publicacion.comunidadNombre}",
                                fontSize = 11.sp,
                                color = UATOrange,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.MoreVert, "Opciones", tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        if (esModerador || esPropietario) {
                            DropdownMenuItem(
                                text = { Text("Eliminar", color = Color(0xFFE11D48)) },
                                onClick = { showMenu = false; showDeleteConfirm = true },
                                leadingIcon = { Icon(Icons.Filled.Delete, null, tint = Color(0xFFE11D48), modifier = Modifier.size(18.dp)) }
                            )
                        }
                        if (!esPropietario) {
                            DropdownMenuItem(
                                text = { Text("Reportar") },
                                onClick = { showMenu = false; showReportDialog = true },
                                leadingIcon = { Icon(Icons.Filled.Report, null, tint = Color.Gray, modifier = Modifier.size(18.dp)) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Contenido Texto
            if (!publicacion.contenidoTexto.isNullOrBlank()) {
                Text(publicacion.contenidoTexto, fontSize = 14.sp, lineHeight = 20.sp, color = UATBlueDark)
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Imagen Multimedia
            if (!publicacion.urlMultimedia.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray.copy(alpha = 0.2f))
                ) {
                    AsyncImage(
                        model = RetrofitClient.BASE_URL + publicacion.urlMultimedia.removePrefix("/"),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.FillWidth,
                        imageLoader = RetrofitClient.getImageLoader(LocalContext.current)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Contadores
            if (publicacion.totalReacciones > 0 || publicacion.totalComentarios > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (publicacion.totalReacciones > 0) {
                        Text("${publicacion.totalReacciones} me gusta", fontSize = 12.sp, color = Color.Gray)
                    }
                    if (publicacion.totalComentarios > 0) {
                        Text("${publicacion.totalComentarios} comentarios", fontSize = 12.sp, color = Color.Gray)
                    }
                }
                HorizontalDivider(color = Color(0xFFE5E7EB))
            }

            // Acciones
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceAround) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onReaccion() }.padding(8.dp)
                ) {
                    Icon(
                        if (publicacion.miReaccion != null) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        null,
                        tint = if (publicacion.miReaccion != null) UATOrange else UATBlueLight,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Me gusta", fontSize = 12.sp, color = if (publicacion.miReaccion != null) UATOrange else UATBlueLight)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onComentar() }.padding(8.dp)
                ) {
                    Icon(Icons.Filled.ChatBubbleOutline, null, tint = UATBlueLight, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Comentar", fontSize = 12.sp, color = UATBlueLight)
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                    Icon(Icons.Filled.Share, null, tint = UATBlueLight, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Compartir", fontSize = 12.sp, color = UATBlueLight)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsBottomSheet(
    postId: Int,
    apiService: com.uat.uatlife.network.ApiService,
    onDismiss: () -> Unit,
    onCommentAdded: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val comments = remember { mutableStateListOf<com.uat.uatlife.network.models.Comentario>() }
    var isLoading by remember { mutableStateOf(true) }
    var nuevoComentario by remember { mutableStateOf("") }
    var isPosting by remember { mutableStateOf(false) }

    LaunchedEffect(postId) {
        try {
            val resp = apiService.getComentarios(postId)
            if (resp.isSuccessful) {
                comments.clear()
                comments.addAll(resp.body() ?: emptyList())
            }
        } catch (e: Exception) {}
        finally { isLoading = false }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth().heightIn(min = 300.dp, max = 600.dp).padding(16.dp)) {
            Text("Comentarios", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = UATBlueDark)
            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = UATOrange)
                }
            } else if (comments.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Aún no hay comentarios. ¡Sé el primero!", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(comments) { com ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(UATBlueLight), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Person, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(com.autorNombre, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = UATBlueDark)
                                Text(com.contenido, fontSize = 14.sp, color = Color.Black)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = nuevoComentario,
                    onValueChange = { nuevoComentario = it },
                    placeholder = { Text("Escribe un comentario...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = UATOrange)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (nuevoComentario.isBlank()) return@IconButton
                        isPosting = true
                        scope.launch {
                            try {
                                val resp = apiService.comentar(postId, com.uat.uatlife.network.models.ComentarRequest(nuevoComentario.trim()))
                                if (resp.isSuccessful) {
                                    val created = resp.body()
                                    if (created != null) comments.add(created)
                                    nuevoComentario = ""
                                    onCommentAdded()
                                }
                            } catch (e: Exception) {}
                            finally { isPosting = false }
                        }
                    },
                    enabled = !isPosting
                ) {
                    if (isPosting) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = UATOrange)
                    else Icon(Icons.Filled.Send, contentDescription = "Enviar", tint = UATOrange)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

fun formatFecha(fechaIso: String): String {
    return try {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val date = sdf.parse(fechaIso) ?: return fechaIso
        val diff = System.currentTimeMillis() - date.time
        val mins = diff / 60000
        val hours = mins / 60
        val days = hours / 24
        when {
            mins < 1 -> "Justo ahora"
            mins < 60 -> "Hace $mins min"
            hours < 24 -> "Hace $hours h"
            days == 1L -> "Ayer"
            days < 7 -> "Hace $days días"
            else -> java.text.SimpleDateFormat("dd MMM", java.util.Locale("es")).format(date)
        }
    } catch (e: Exception) { fechaIso }
}
