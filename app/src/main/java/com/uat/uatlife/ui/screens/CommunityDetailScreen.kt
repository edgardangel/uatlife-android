package com.uat.uatlife.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

private data class GroupPostMock(
    val author: String,
    val timeAgo: String,
    val content: String,
    val isNotice: Boolean = false,
    val attachmentName: String? = null,
    val likes: Int,
    val comments: Int
)

private val postsMock = listOf(
    GroupPostMock(
        author = "Carlos Mendoza",
        timeAgo = "HACE 2 HORAS",
        content = "¿Alguien tiene los apuntes de la clase de Algoritmos Avanzados de hoy? Tuve problemas con mi conexión y me perdí la última media hora.\n\n¡Se los agradecería mucho!",
        likes = 12,
        comments = 4
    ),
    GroupPostMock(
        author = "Ana Pérez",
        timeAgo = "AYER A LAS 14:30",
        content = "Recordatorio: El proyecto final de Bases de Datos se entrega el próximo viernes. Asegúrense de revisar los requisitos en la plataforma virtual.",
        isNotice = true,
        attachmentName = "Requisitos_Proyecto_Final_BD.pdf",
        likes = 45,
        comments = 8
    )
)

@Composable
fun CommunityDetailScreen(
    communityId: String,
    onBack: () -> Unit
) {
    // Datos básicos de la comunidad (se mostrarán los reales cuando se integre el detalle)
    val nombreComunidad = "Comunidad #$communityId"
    var isJoined by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }

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
                Text(nombreComunidad, fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Public, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pública • Miembros", color = Color.Gray, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { isJoined = !isJoined },
                        modifier = Modifier.weight(1f).height(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isJoined) UATBlueDark else UATOrange
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (isJoined) "Unido" else "Unirse", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { /* TODO */ },
                        modifier = Modifier
                            .background(Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                            .size(40.dp)
                    ) {
                        Icon(Icons.Filled.MoreHoriz, contentDescription = "Más", tint = Color.Black)
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
            // Escribe algo... Box
            if (isJoined) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(36.dp).clip(CircleShape).background(UATBlueDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("JD", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .background(Color(0xFFF3F4F6), RoundedCornerShape(20.dp))
                                    .padding(start = 16.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text("Escribe algo para el grupo...", color = Color.Gray, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(Icons.Filled.Image, contentDescription = "Adjuntar Foto", tint = Color.Gray)
                        }
                    }
                }
            }

            // Posts
            items(postsMock.size) { index ->
                CommunityPostCard(postsMock[index])
            }
        }
    }
}

@Composable
private fun CommunityPostCard(post: GroupPostMock) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Author + Time + Aviso tag
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF2C3E50)))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(post.author, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(post.timeAgo, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (post.isNotice) {
                    Box(modifier = Modifier.background(Color(0xFFFFEDD5), RoundedCornerShape(4.dp)).padding(horizontal=6.dp, vertical=2.dp)) {
                        Text("AVISO", color = Color(0xFFC2410C), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Icon(Icons.Filled.MoreHoriz, contentDescription = "Opciones", tint = Color.Gray)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Text Content
            Text(post.content, fontSize = 14.sp, color = Color.DarkGray, lineHeight = 20.sp)

            // Attachment
            if (post.attachmentName != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Description, contentDescription = null, tint = UATBlueDark)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(post.attachmentName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                        Text("2.4 MB", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color(0xFFF3F4F6))
            Spacer(modifier = Modifier.height(12.dp))

            // Interactions
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.ThumbUp, contentDescription = "Me gusta", tint = UATBlueDark, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(post.likes.toString(), fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                
                Spacer(modifier = Modifier.width(24.dp))
                
                Icon(Icons.Filled.Comment, contentDescription = "Comentar", tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(post.comments.toString(), fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            }
        }
    }
}
