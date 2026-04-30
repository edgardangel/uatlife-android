package com.uat.uatlife.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uat.uatlife.network.RetrofitClient
import com.uat.uatlife.network.models.Conversacion
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

    LaunchedEffect(Unit) { cargarConversaciones() }

    val filtradas = if (searchQuery.isBlank()) conversaciones
    else conversaciones.filter { it.otroUsuarioNombre.contains(searchQuery, true) || it.ultimoMensaje?.contains(searchQuery, true) == true }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF3F4F9))) {
        // ── Top Bar ───────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().background(UATBlue)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Mensajes", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp, modifier = Modifier.weight(1f))
            // Refrescar
            IconButton(onClick = { cargarConversaciones() }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.Refresh, "Actualizar", tint = Color.White, modifier = Modifier.size(20.dp))
            }
            // Nuevo mensaje (TODO: navegar a búsqueda de usuario)
            IconButton(onClick = { }, modifier = Modifier.background(UATOrange, RoundedCornerShape(12.dp)).size(40.dp)) {
                Icon(Icons.Filled.Edit, "Nuevo Mensaje", tint = Color.White)
            }
        }

        // ── Contenido ─────────────────────────────────────────────
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Buscador
            item {
                OutlinedTextField(
                    value = searchQuery, onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar conversación o usuario", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Filled.Search, null, tint = Color.Gray) },
                    trailingIcon = { if (searchQuery.isNotBlank()) IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Filled.Close, null, tint = Color.Gray, modifier = Modifier.size(18.dp)) } },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = UATBlueDark, unfocusedBorderColor = Color.Transparent, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White),
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
                                if (searchQuery.isBlank()) "Sin conversaciones aún.\nInicia un chat desde el perfil de un usuario."
                                else "Sin resultados para \"$searchQuery\"",
                                color = Color.Gray, fontSize = 14.sp
                            )
                        }
                    }
                }

                else -> items(filtradas, key = { it.id }) { conv ->
                    ChatConversacionCard(conv) { onNavigateToChat(conv.id.toString()) }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
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
            // Avatar
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
                    // Badge de no leídos
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
