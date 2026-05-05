package com.uat.uatlife.ui.screens

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uat.uatlife.network.RetrofitClient
import com.uat.uatlife.network.models.Conversacion
import com.uat.uatlife.network.models.EnviarMensajeRequest
import com.uat.uatlife.network.models.UsuarioBusqueda
import com.uat.uatlife.ui.theme.UATBlue
import com.uat.uatlife.ui.theme.UATBlueDark
import com.uat.uatlife.ui.theme.UATOrange
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(onNavigateToChat: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = remember { RetrofitClient.getApiService(context) }

    val conversaciones = remember { mutableStateListOf<Conversacion>() }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    // Estado del buscador de usuarios
    var showUserSearch by remember { mutableStateOf(false) }
    var userSearchQuery by remember { mutableStateOf("") }
    var userSearchResults by remember { mutableStateOf<List<UsuarioBusqueda>>(emptyList()) }
    var isSearchingUsers by remember { mutableStateOf(false) }

    fun cargarConversaciones() {
        scope.launch {
            isLoading = true
            try {
                val resp = apiService.getConversaciones()
                if (resp.isSuccessful) {
                    conversaciones.clear()
                    conversaciones.addAll(resp.body() ?: emptyList())
                }
            } catch (_: Exception) { } finally { isLoading = false }
        }
    }

    fun eliminarConversacion(conv: Conversacion) {
        scope.launch {
            try {
                val resp = apiService.eliminarConversacion(conv.id)
                if (resp.isSuccessful) {
                    conversaciones.remove(conv)
                    Toast.makeText(context, "Conversación eliminada", Toast.LENGTH_SHORT).show()
                }
            } catch (_: Exception) {
                Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun buscarUsuarios(q: String) {
        if (q.length < 2) { userSearchResults = emptyList(); return }
        scope.launch {
            isSearchingUsers = true
            try {
                val resp = apiService.buscarUsuarios(q)
                if (resp.isSuccessful) userSearchResults = resp.body() ?: emptyList()
            } catch (_: Exception) { } finally { isSearchingUsers = false }
        }
    }

    fun iniciarChatConUsuario(usuario: UsuarioBusqueda, mensajeInicial: String) {
        scope.launch {
            try {
                val resp = apiService.enviarMensaje(
                    EnviarMensajeRequest(destinatarioId = usuario.id, contenido = mensajeInicial)
                )
                if (resp.isSuccessful) {
                    showUserSearch = false
                    userSearchQuery = ""
                    userSearchResults = emptyList()
                    // Recargar conversaciones para que aparezca la nueva
                    cargarConversaciones()
                    Toast.makeText(context, "Chat iniciado con ${usuario.nombreCompleto}", Toast.LENGTH_SHORT).show()
                }
            } catch (_: Exception) {
                Toast.makeText(context, "Error al iniciar chat", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) { cargarConversaciones() }

    // Buscar mientras escribe
    LaunchedEffect(userSearchQuery) { buscarUsuarios(userSearchQuery) }

    val filtradas = if (searchQuery.isBlank()) conversaciones
    else conversaciones.filter {
        it.otroUsuarioNombre.contains(searchQuery, true) ||
        it.ultimoMensaje?.contains(searchQuery, true) == true
    }

    // ── Modal de Búsqueda de Usuarios ─────────────────────────────
    if (showUserSearch) {
        var mensajeInicial by remember { mutableStateOf("Hola! ¿Cómo estás?") }
        var usuarioSeleccionado by remember { mutableStateOf<UsuarioBusqueda?>(null) }

        AlertDialog(
            onDismissRequest = {
                showUserSearch = false
                userSearchQuery = ""
                userSearchResults = emptyList()
                usuarioSeleccionado = null
            },
            title = {
                Text(
                    "Nuevo mensaje",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = UATBlueDark
                )
            },
            text = {
                Column {
                    if (usuarioSeleccionado == null) {
                        // Buscador
                        OutlinedTextField(
                            value = userSearchQuery,
                            onValueChange = { userSearchQuery = it },
                            placeholder = { Text("Buscar por nombre o matrícula", color = Color.Gray, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Filled.Search, null, tint = Color.Gray) },
                            trailingIcon = {
                                if (userSearchQuery.isNotBlank()) {
                                    IconButton(onClick = { userSearchQuery = ""; userSearchResults = emptyList() }) {
                                        Icon(Icons.Filled.Close, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = UATBlueDark,
                                unfocusedBorderColor = Color.LightGray
                            ),
                            shape = RoundedCornerShape(20.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (isSearchingUsers) {
                            Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = UATOrange, modifier = Modifier.size(28.dp))
                            }
                        } else if (userSearchResults.isEmpty() && userSearchQuery.length >= 2) {
                            Text("Sin resultados", color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(8.dp))
                        } else {
                            LazyColumn(modifier = Modifier.heightIn(max = 250.dp)) {
                                items(userSearchResults) { usuario ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { usuarioSeleccionado = usuario }
                                            .padding(vertical = 10.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFE5E7EB)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Filled.Person, null, tint = Color.Gray, modifier = Modifier.size(24.dp))
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(usuario.nombreCompleto, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Black)
                                            Text(usuario.matricula, fontSize = 12.sp, color = Color.Gray)
                                        }
                                    }
                                    Divider(color = Color(0xFFF3F4F6))
                                }
                            }
                        }
                    } else {
                        // Confirmación con mensaje inicial
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(44.dp).clip(CircleShape).background(Color(0xFFE5E7EB)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Person, null, tint = Color.Gray, modifier = Modifier.size(28.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(usuarioSeleccionado!!.nombreCompleto, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(usuarioSeleccionado!!.matricula, fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = mensajeInicial,
                            onValueChange = { mensajeInicial = it },
                            label = { Text("Primer mensaje") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = UATBlueDark),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 3
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(onClick = { usuarioSeleccionado = null }) {
                            Icon(Icons.Filled.ArrowBack, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cambiar usuario")
                        }
                    }
                }
            },
            confirmButton = {
                if (usuarioSeleccionado != null) {
                    Button(
                        onClick = { iniciarChatConUsuario(usuarioSeleccionado!!, mensajeInicial) },
                        enabled = mensajeInicial.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = UATOrange)
                    ) {
                        Icon(Icons.Filled.Send, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Enviar")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showUserSearch = false
                    userSearchQuery = ""
                    userSearchResults = emptyList()
                    usuarioSeleccionado = null
                }) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF3F4F9))) {
        // ── Top Bar ───────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().background(UATBlue)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Mensajes", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp, modifier = Modifier.weight(1f))
            IconButton(onClick = { cargarConversaciones() }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.Refresh, "Actualizar", tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(6.dp))
            IconButton(
                onClick = { showUserSearch = true },
                modifier = Modifier.background(UATOrange, RoundedCornerShape(12.dp)).size(40.dp)
            ) {
                Icon(Icons.Filled.Edit, "Nuevo Mensaje", tint = Color.White)
            }
        }

        // ── Contenido ─────────────────────────────────────────────
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Buscador de conversaciones
            item {
                OutlinedTextField(
                    value = searchQuery, onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar conversación o usuario", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Filled.Search, null, tint = Color.Gray) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Close, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = UATBlueDark,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    shape = RoundedCornerShape(26.dp), singleLine = true
                )
            }

            when {
                isLoading -> item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = UATOrange, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Cargando mensajes...", color = UATBlueDark.copy(alpha = 0.6f), fontSize = 13.sp)
                        }
                    }
                }

                filtradas.isEmpty() -> item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.ChatBubbleOutline, null, tint = UATBlueDark.copy(alpha = 0.25f), modifier = Modifier.size(56.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                if (searchQuery.isBlank()) "Sin conversaciones aún.\nToca ✏️ para iniciar un chat."
                                else "Sin resultados para \"$searchQuery\"",
                                color = Color.Gray, fontSize = 14.sp
                            )
                        }
                    }
                }

                else -> items(filtradas, key = { it.id }) { conv ->
                    SwipeToDeleteConversacion(
                        conv = conv,
                        onDelete = { eliminarConversacion(conv) },
                        onClick = { onNavigateToChat(conv.id.toString()) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteConversacion(
    conv: Conversacion,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                showConfirmDialog = true
            }
            false // No dismiss automáticamente, esperar confirmación
        }
    )

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Eliminar conversación", fontWeight = FontWeight.Bold) },
            text = { Text("¿Eliminar el chat con ${conv.otroUsuarioNombre}? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = { showConfirmDialog = false; onDelete() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) { Text("Eliminar", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("Cancelar") }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                    Color(0xFFEF4444) else Color(0xFFFFCDD2),
                label = "swipe_color"
            )
            val scale by animateFloatAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) 1f else 0.8f,
                label = "icon_scale"
            )
            Box(
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)).background(color),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Filled.Delete, "Eliminar",
                    tint = Color.White,
                    modifier = Modifier.padding(end = 20.dp).scale(scale).size(28.dp)
                )
            }
        }
    ) {
        ChatConversacionCard(conv, onClick)
    }
}

@Composable
private fun ChatConversacionCard(conv: Conversacion, onClick: () -> Unit) {
    val tieneNoLeidos = conv.mensajesNoLeidos > 0
    val bgColor = if (tieneNoLeidos) Color(0xFFDCE6FB) else Color.White

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(52.dp).clip(CircleShape).background(Color(0xFFE5E7EB)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Person, null, tint = Color.Gray, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        conv.otroUsuarioNombre,
                        fontWeight = if (tieneNoLeidos) FontWeight.Bold else FontWeight.SemiBold,
                        color = UATBlueDark, fontSize = 15.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        formatFechaConversacion(conv.ultimaActividad),
                        fontSize = 11.sp,
                        color = if (tieneNoLeidos) UATBlueDark else Color.Gray,
                        fontWeight = if (tieneNoLeidos) FontWeight.Bold else FontWeight.Normal
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        conv.ultimoMensaje ?: "Inicia la conversación...",
                        fontSize = 13.sp,
                        color = if (tieneNoLeidos) UATBlueDark.copy(alpha = 0.8f) else Color.Gray,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        fontWeight = if (tieneNoLeidos) FontWeight.Medium else FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )
                    if (tieneNoLeidos) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier.size(20.dp).clip(CircleShape).background(UATOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (conv.mensajesNoLeidos > 9) "9+" else "${conv.mensajesNoLeidos}",
                                fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatFechaConversacion(fechaIso: String): String {
    return try {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val date = sdf.parse(fechaIso) ?: return fechaIso
        val diff = System.currentTimeMillis() - date.time
        val mins = diff / 60000
        val hours = mins / 60
        val days = hours / 24
        when {
            mins < 60  -> "$mins min"
            hours < 24 -> "${hours}h"
            days == 1L -> "Ayer"
            days < 7   -> "${days}d"
            else       -> java.text.SimpleDateFormat("dd MMM", java.util.Locale("es")).format(date)
        }
    } catch (_: Exception) { fechaIso }
}
