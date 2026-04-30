package com.uat.uatlife.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uat.uatlife.ui.theme.UATBlueDark
import com.uat.uatlife.ui.theme.UATOrange

private data class MessageMock(
    val text: String,
    val time: String,
    val isSentByMe: Boolean
)

private val chatMessagesMock = listOf(
    MessageMock("Hey! Is the calculus book still available?", "10:30 AM", false),
    MessageMock("Hi Alex, yes it is! It's in great condition, barely used.", "10:35 AM", true),
    MessageMock("Awesome. Could we meet up on campus today? I'm near the Student Union until 2 PM.", "10:40 AM", false),
    MessageMock("Yeah, I can swing by the union around 1:30. Does that work?", "10:41 AM", true),
    MessageMock("Yeah, the textbook is still available.", "10:42 AM", false)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chatId: String,
    onBack: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    
    // Condicionar la vista del producto asociado simulando que el chat "2" y "4" son sobre Market
    val showProductContext = chatId == "2" || chatId == "4" || chatId == "1"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB)) // Fondo extra claro de la app
    ) {
        // Top Bar Custom - UATLife general branding
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp)
                .windowInsetsPadding(WindowInsets.statusBars),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Hub, contentDescription = null, tint = UATBlueDark)
            Spacer(modifier = Modifier.width(8.dp))
            Text("UATLife", fontWeight = FontWeight.Black, fontSize = 20.sp, color = UATBlueDark, modifier = Modifier.weight(1f))
            Icon(Icons.Filled.Notifications, contentDescription = "Alertas", tint = UATBlueDark)
        }
        Divider(color = Color(0xFFE5E7EB))

        // Info del contacto
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.DarkGray)
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE5E7EB)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Person, contentDescription = null, tint = Color.Gray)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Alex Rivera", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Text("Online", fontSize = 12.sp, color = Color(0xFF10B981)) // Verde
            }
        }
        Divider(color = Color(0xFFE5E7EB))

        // Zona de Mensajes y Contexto
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tarjeta de Contexto de Producto (Solo si aplica)
            if (showProductContext) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = borderStroke(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.DarkGray) // placeholder img
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("DISCUSSING ITEM", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Text("Calculus Early Transcendentals 8th Ed", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("$45.00", fontWeight = FontWeight.Bold, color = UATOrange, fontSize = 14.sp)
                            }
                            Icon(Icons.Filled.OpenInNew, contentDescription = "Ver Producto", tint = Color.Gray)
                        }
                    }
                }
            }

            // Etiqueta "Today"
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.background(Color(0xFFE5E7EB), RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                        Text("Today", fontSize = 11.sp, color = Color.DarkGray)
                    }
                }
            }

            // Burbujas de Chat
            items(chatMessagesMock) { msg ->
                ChatBubble(msg)
            }
        }

        // Bottom Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .windowInsetsPadding(WindowInsets.navigationBars),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.AddCircle, contentDescription = "Adjuntar", tint = Color.Gray, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("Type a message...", color = Color.Gray) },
                modifier = Modifier.weight(1f).heightIn(min = 45.dp, max = 100.dp),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.LightGray,
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color(0xFFF3F4F6),
                    unfocusedContainerColor = Color(0xFFF3F4F6)
                ),
                trailingIcon = {
                    Icon(Icons.Filled.EmojiEmotions, contentDescription = "Emojis", tint = Color.Gray)
                }
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = { /*TODO*/ },
                modifier = Modifier
                    .background(UATOrange, CircleShape)
                    .size(44.dp)
            ) {
                Icon(Icons.Filled.Send, contentDescription = "Enviar", tint = Color.White)
            }
        }
    }
}

@Composable
private fun ChatBubble(message: MessageMock) {
    val alignment = if (message.isSentByMe) Alignment.CenterEnd else Alignment.CenterStart
    val bgColor = if (message.isSentByMe) UATBlueDark else Color(0xFFF3F4F6)
    val textColor = if (message.isSentByMe) Color.White else Color.Black
    val shape = if (message.isSentByMe) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .background(bgColor, shape)
                .padding(12.dp)
        ) {
            Text(
                text = message.text,
                color = textColor,
                fontSize = 15.sp,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.align(Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message.time,
                    fontSize = 10.sp,
                    color = if (message.isSentByMe) Color.White.copy(alpha=0.7f) else Color.Gray
                )
                if (message.isSentByMe) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Filled.DoneAll, contentDescription = null, tint = Color.White.copy(alpha=0.8f), modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

// Extractor para borde sutil en Card
@Composable
private fun borderStroke() = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
