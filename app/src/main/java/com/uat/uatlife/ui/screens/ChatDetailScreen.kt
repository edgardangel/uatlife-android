package com.uat.uatlife.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uat.uatlife.data.TokenManager
import com.uat.uatlife.network.RetrofitClient
import com.uat.uatlife.network.models.EnviarMensajeRequest
import com.uat.uatlife.network.models.Mensaje
import com.uat.uatlife.ui.theme.UATBlueDark
import com.uat.uatlife.ui.theme.UATOrange
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chatId: String,         // conversacionId
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = remember { RetrofitClient.getApiService(context) }
    val tokenManager = remember { TokenManager(context) }

    // Mi ID para distinguir mis mensajes
    val miMatricula by tokenManager.getUserName().collectAsState(initial = "")

    val mensajes = remember { mutableStateListOf<Mensaje>() }
    var isLoading by remember { mutableStateOf(true) }
    var nombreContacto by remember { mutableStateOf("Conversación") }
    var otroUsuarioId by remember { mutableStateOf(0) }
    var messageText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // --- Cargar mensajes de la conversación ---
    fun cargarMensajes(scrollToBottom: Boolean = false) {
        scope.launch {
            try {
                val convId = chatId.toIntOrNull() ?: return@launch
                val resp = apiService.getMensajes(convId)
                if (resp.isSuccessful) {
                    val lista = resp.body() ?: emptyList()
                    mensajes.clear()
                    mensajes.addAll(lista)
                    if (scrollToBottom && mensajes.isNotEmpty()) {
                        listState.animateScrollToItem(mensajes.size - 1)
                    }
                }
            } catch (_: Exception) { }
            finally { isLoading = false }
        }
    }

    // Cargar nombre del contacto desde la lista de conversaciones
    LaunchedEffect(chatId) {
        try {
            val convId = chatId.toIntOrNull() ?: return@LaunchedEffect
            val resp = apiService.getConversaciones()
            if (resp.isSuccessful) {
                val conv = resp.body()?.find { it.id == convId }
                if (conv != null) {
                    nombreContacto = conv.otroUsuarioNombre
                    otroUsuarioId = conv.otroUsuarioId
                }
            }
        } catch (_: Exception) { }
        cargarMensajes(scrollToBottom = true)
    }

    // Auto-scroll cuando llegan nuevos mensajes
    LaunchedEffect(mensajes.size) {
        if (mensajes.isNotEmpty()) {
            listState.animateScrollToItem(mensajes.size - 1)
        }
    }

    // Función de envío
    fun enviarMensaje() {
        val texto = messageText.trim()
        if (texto.isBlank() || otroUsuarioId == 0) return
        isSending = true
        scope.launch {
            try {
                val resp = apiService.enviarMensaje(
                    EnviarMensajeRequest(destinatarioId = otroUsuarioId, contenido = texto)
                )
                if (resp.isSuccessful) {
                    messageText = ""
                    cargarMensajes(scrollToBottom = true)
                } else {
                    Toast.makeText(context, "No se pudo enviar el mensaje", Toast.LENGTH_SHORT).show()
                }
            } catch (_: Exception) {
                Toast.makeText(context, "Sin conexión", Toast.LENGTH_SHORT).show()
            } finally {
                isSending = false
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF9FAFB))
    ) {
        // ── Header del chat ───────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "Volver", tint = Color.DarkGray)
            }
            // Avatar
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFE5E7EB)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Person, null, tint = Color.Gray, modifier = Modifier.size(26.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(nombreContacto, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                if (isLoading) {
                    Text("Cargando...", fontSize = 12.sp, color = Color.Gray)
                } else {
                    Text("Toca para ver perfil", fontSize = 12.sp, color = Color(0xFF10B981))
                }
            }
            // Refrescar
            IconButton(onClick = { cargarMensajes(scrollToBottom = true) }) {
                Icon(Icons.Filled.Refresh, "Actualizar", tint = UATBlueDark, modifier = Modifier.size(20.dp))
            }
        }
        Divider(color = Color(0xFFE5E7EB))

        // ── Zona de Mensajes ──────────────────────────────────────
        when {
            isLoading -> {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = UATOrange, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Cargando mensajes...", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            }
            mensajes.isEmpty() -> {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Icon(Icons.Filled.ChatBubbleOutline, null, tint = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Aún no hay mensajes.\n¡Inicia la conversación!", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }
            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Etiqueta de fecha
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier.background(Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("Hoy", fontSize = 11.sp, color = Color.DarkGray)
                            }
                        }
                    }

                    items(mensajes, key = { it.id }) { msg ->
                        // Si emisorId coincide con miId (comparamos por nombre como fallback)
                        val esMio = msg.emisorNombre == miMatricula || run {
                            // Fallback: el último de la conversación en el rol correcto
                            val convId = chatId.toIntOrNull() ?: 0
                            msg.emisorId != otroUsuarioId && otroUsuarioId != 0
                        }
                        ChatBubble(mensaje = msg, esMio = esMio)
                    }
                }
            }
        }

        // ── Input Bar ─────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .windowInsetsPadding(WindowInsets.navigationBars),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("Escribe un mensaje...", color = Color.Gray) },
                modifier = Modifier.weight(1f).heightIn(min = 45.dp, max = 120.dp),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = UATBlueDark.copy(alpha = 0.4f),
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color(0xFFF3F4F6),
                    unfocusedContainerColor = Color(0xFFF3F4F6)
                ),
                trailingIcon = {
                    Icon(Icons.Filled.EmojiEmotions, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                },
                maxLines = 4
            )
            Spacer(modifier = Modifier.width(8.dp))
            // Botón enviar
            IconButton(
                onClick = { enviarMensaje() },
                modifier = Modifier.background(
                    if (messageText.isBlank() || isSending) Color.LightGray else UATOrange,
                    CircleShape
                ).size(46.dp),
                enabled = messageText.isNotBlank() && !isSending
            ) {
                if (isSending) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Filled.Send, "Enviar", tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(mensaje: Mensaje, esMio: Boolean) {
    val bgColor = if (esMio) UATBlueDark else Color(0xFFEEF2FF)
    val textColor = if (esMio) Color.White else Color.Black
    val alignment = if (esMio) Alignment.CenterEnd else Alignment.CenterStart
    val shape = if (esMio) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .background(bgColor, shape)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Nombre remitente (solo si no es mío)
            if (!esMio) {
                Text(
                    mensaje.emisorNombre,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = UATOrange,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
            Text(mensaje.contenido, color = textColor, fontSize = 15.sp, lineHeight = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.align(Alignment.End), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    formatHora(mensaje.fechaEnvio),
                    fontSize = 10.sp,
                    color = if (esMio) Color.White.copy(alpha = 0.7f) else Color.Gray
                )
                if (esMio) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        if (mensaje.leido) Icons.Filled.DoneAll else Icons.Filled.Done,
                        null,
                        tint = if (mensaje.leido) Color(0xFF60D0FF) else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

private fun formatHora(fechaIso: String): String {
    return try {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val date = sdf.parse(fechaIso) ?: return ""
        val local = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
        local.format(date)
    } catch (_: Exception) { "" }
}
