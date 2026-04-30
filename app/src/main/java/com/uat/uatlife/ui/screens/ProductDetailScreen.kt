package com.uat.uatlife.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uat.uatlife.ui.theme.UATBlueDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    producto: com.uat.uatlife.data.mock.ProductoMock?,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold() { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            // Custom Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Cerrar",
                    tint = Color.Black,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable { onBack() }
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Buscar",
                    tint = Color.Black,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable { /*TODO*/ }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Opciones",
                    tint = Color.Black,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable { /*TODO*/ }
                )
            }

            // Imagen del producto
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color(0xFFE5E7EB)) // Placeholder de foto
            ) {
                // Simulación de los puntitos de paginación (Carousel dots)
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .background(Color.Black.copy(alpha=0.3f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) { i ->
                        Box(
                            modifier = Modifier
                                .size(if (i == 0) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(if (i == 0) Color.White else Color.White.copy(alpha=0.5f))
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Título y Precio
                Text(
                    text = producto?.nombre ?: "Producto Desconocido",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = producto?.precio ?: "$0",
                    fontSize = 18.sp,
                    color = Color.DarkGray
                )
                
                Spacer(modifier = Modifier.height(20.dp))

                // Descripción del producto
                Text(
                    text = "Descripción",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = producto?.descripcion ?: "Sin descripción.",
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))
                Divider(color = Color(0xFFE5E7EB), thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // Mensaje Rápido
                Text("Enviar mensaje al vendedor", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(Color(0xFFF3F4F6), RoundedCornerShape(24.dp))
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hola, ¿estás disponible?",
                        color = Color.DarkGray,
                        modifier = Modifier.weight(1f),
                        fontSize = 15.sp
                    )
                    IconButton(onClick = { /*TODO*/ }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Send, contentDescription = "Enviar", tint = UATBlueDark, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Divider(color = Color(0xFFE5E7EB), thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // Sección del Vendedor
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Vendedor", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Ver Más", tint = Color.Gray)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar Vendedor
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE5E7EB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Info Vendedor
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Carlos Hernandez", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                            Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                            Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                            Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                            Icon(Icons.Filled.StarHalf, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("4.7", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// Extractor para borde sutil en Card
@Composable
private fun borderStroke() = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))


