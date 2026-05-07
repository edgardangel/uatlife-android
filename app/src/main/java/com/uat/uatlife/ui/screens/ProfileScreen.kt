package com.uat.uatlife.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uat.uatlife.data.TokenManager
import com.uat.uatlife.network.RetrofitClient
import com.uat.uatlife.network.models.UserProfile
import com.uat.uatlife.ui.theme.UATBlue
import com.uat.uatlife.ui.theme.UATBlueDark
import com.uat.uatlife.ui.theme.UATOrange
import kotlinx.coroutines.launch
import coil.ImageLoader
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.uat.uatlife.utils.ImageUtils
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToEditProfile: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    onNavigateToModeration: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val apiService = remember { RetrofitClient.getApiService(context) }
    val coroutineScope = rememberCoroutineScope()
    
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .okHttpClient { RetrofitClient.getUnsafeOkHttpClient() }
            .build()
    }

    val userType by tokenManager.getUserType().collectAsState(initial = "alumno")
    val scrollState = rememberScrollState()

    // Perfil cargado desde la API
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Mis productos vendidos (conteo)
    var totalVentas by remember { mutableStateOf(0) }
    var isUploadingDoc by remember { mutableStateOf(false) }

    fun cargarPerfil() {
        coroutineScope.launch {
            isLoading = true
            errorMsg = null
            try {
                val resp = apiService.getProfile()
                if (resp.isSuccessful) {
                    profile = resp.body()
                } else if (resp.code() == 401 || resp.code() == 403) {
                    // Token expirado o inválido (mock antiguo) → logout
                    Toast.makeText(context, "Sesión inválida, inicia sesión nuevamente", Toast.LENGTH_LONG).show()
                    onLogout()
                } else {
                    errorMsg = "No se pudo cargar el perfil"
                }
            } catch (_: Exception) {
                errorMsg = "Sin conexión"
            } finally {
                isLoading = false
            }
        }
    }

    // Cargar mis productos para el contador de ventas
    fun cargarVentas() {
        coroutineScope.launch {
            try {
                val resp = apiService.getMisProductos()
                if (resp.isSuccessful) {
                    totalVentas = resp.body()?.count { it.estaVendido } ?: 0
                }
            } catch (_: Exception) { }
        }
    }

    val docPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            isUploadingDoc = true
            coroutineScope.launch {
                try {
                    val part = ImageUtils.uriToMultipart(context, uri, "imagen")
                    if (part != null) {
                        val resp = apiService.uploadDocumento(part)
                        if (resp.isSuccessful) {
                            Toast.makeText(context, "Documento subido. En espera de validación.", Toast.LENGTH_LONG).show()
                            cargarPerfil() // Recargar para actualizar el estatus y la URL
                        } else {
                            Toast.makeText(context, "Error al subir documento", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    isUploadingDoc = false
                }
            }
        }
    }

    LaunchedEffect(Unit) { cargarPerfil(); cargarVentas() }

    // Nivel de confianza calculado
    val confianzaPct = ((profile?.puntosConfianza ?: 0).coerceIn(0, 100)) / 100f
    val nivelConfianza = when {
        (profile?.puntosConfianza ?: 0) >= 80 -> "Confiable" to Color(0xFF16A34A)
        (profile?.puntosConfianza ?: 0) >= 50 -> "En progreso" to UATOrange
        else                                   -> "Nuevo" to Color.Gray
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF1F5F9))
    ) {
        // ── Top Bar ───────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(UATBlue)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Mi perfil", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color.White, modifier = Modifier.weight(1f))
            // Botón refrescar
            IconButton(onClick = { cargarPerfil(); cargarVentas() }, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Filled.Refresh, "Actualizar", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }

        // ── Contenido ─────────────────────────────────────────────
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = UATOrange, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Cargando perfil...", color = UATBlueDark.copy(alpha = 0.6f), fontSize = 14.sp)
                    }
                }
            }

            errorMsg != null && profile == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Icon(Icons.Filled.WifiOff, null, tint = UATBlueLight(), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(errorMsg!!, color = UATBlueDark, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { cargarPerfil() }, colors = ButtonDefaults.buttonColors(containerColor = UATOrange)) {
                            Text("Reintentar", color = Color.White)
                        }
                    }
                }
            }

            else -> {
                val p = profile!!
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ── Tarjeta Principal ──────────────────────────
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(modifier = Modifier.height(12.dp))

                            // Avatar con Badge de edición
                            Box {
                                Box(
                                    modifier = Modifier.size(100.dp).clip(CircleShape)
                                        .background(Color(0xFFE5E7EB))
                                        .border(3.dp, UATOrange, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!p.urlFotoPerfil.isNullOrEmpty()) {
                                        AsyncImage(
                                            model = "https://bd-uat-bus-api-uatlife-xazfaa-1b2660-157-245-239-94.traefik.me${p.urlFotoPerfil}",
                                            contentDescription = "Foto de Perfil",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop,
                                            imageLoader = imageLoader
                                        )
                                    } else {
                                        Icon(Icons.Filled.Person, null, tint = Color.Gray, modifier = Modifier.size(60.dp))
                                    }
                                }
                                Box(
                                    modifier = Modifier.size(28.dp).align(Alignment.BottomEnd)
                                        .offset(x = (-4).dp, y = (-4).dp)
                                        .background(UATOrange, CircleShape)
                                        .border(2.dp, Color.White, CircleShape)
                                        .clickable { onNavigateToEditProfile() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Edit, "Editar", tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Nombre + badge rol
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(p.nombreCompleto, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                if (p.tipoUsuario == "moderador") {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.Filled.Security, "Moderador", tint = UATOrange, modifier = Modifier.size(20.dp))
                                } else if (p.tipoUsuario == "docente") {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.Filled.School, "Docente", tint = UATBlueDark, modifier = Modifier.size(20.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Matrícula + facultad
                            Text(
                                text = buildString {
                                    append(p.matricula)
                                    if (!p.facultadNombre.isNullOrBlank()) append(" • ${p.facultadNombre}")
                                    if (p.semestreActual != null) append(" • ${p.semestreActual}° Sem")
                                },
                                fontSize = 13.sp, color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            // Badge estatus validación
                            Spacer(modifier = Modifier.height(8.dp))
                            val (estatusLabel, estatusColor, estatusBg) = when (p.estatusValidacion) {
                                "aprobado"    -> Triple("✓ Validado", Color(0xFF166534), Color(0xFFDCFCE7))
                                "pendiente"   -> Triple("⏳ Pendiente", Color(0xFF854D0E), Color(0xFFFEF9C3))
                                "rechazado"   -> Triple("✗ Rechazado", Color(0xFF991B1B), Color(0xFFFEE2E2))
                                else          -> Triple("Desconocido", Color.Gray, Color(0xFFF3F4F6))
                            }
                            Box(
                                modifier = Modifier.background(estatusBg, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(estatusLabel, fontSize = 12.sp, color = estatusColor, fontWeight = FontWeight.SemiBold)
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Stats Row
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                StatBox("🔥", "RACHA", "${p.rachaDias ?: 0} días")
                                StatBox("🛡️", "CONFIANZA", "${p.puntosConfianza}/100")
                                StatBox("📦", "VENTAS", "$totalVentas")
                            }

                            // Bio si existe
                            if (!p.bio.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(p.bio, fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 8.dp))
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    // ── Nivel de Confianza ─────────────────────────
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Nivel de Confianza", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Basado en tus interacciones y validaciones en la comunidad.",
                                        fontSize = 12.sp, color = Color.Gray, lineHeight = 16.sp, modifier = Modifier.padding(end = 16.dp))
                                }
                                Text("${p.puntosConfianza}%", fontSize = 22.sp, fontWeight = FontWeight.Black, color = nivelConfianza.second)
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            // Progress Bar animada
                            Box(modifier = Modifier.fillMaxWidth().height(10.dp).background(Color(0xFFE5E7EB), CircleShape)) {
                                Box(modifier = Modifier.fillMaxWidth(confianzaPct).fillMaxHeight()
                                    .background(nivelConfianza.second, CircleShape))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Nuevo", fontSize = 11.sp, color = Color.Gray)
                                Text(nivelConfianza.first, fontSize = 11.sp, color = nivelConfianza.second, fontWeight = FontWeight.SemiBold)
                                Text("Confiable", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }

                    // ── Correo Institucional Info ──────────────────
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Información de Cuenta", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            InfoRow(Icons.Filled.Email, "Correo institucional", p.correoInstitucional)
                            InfoRow(Icons.Filled.Badge, "Tipo de usuario", p.tipoUsuario.replaceFirstChar { it.uppercase() })
                            if (p.banPermanente) {
                                InfoRow(Icons.Filled.Block, "Estado", "⛔ Cuenta suspendida permanentemente", valueColor = Color(0xFFE11D48))
                            } else if (!p.suspensionHasta.isNullOrBlank()) {
                                InfoRow(Icons.Filled.Warning, "Suspensión hasta", p.suspensionHasta, valueColor = UATOrange)
                            }
                        }
                    }

                    // ── Mi Horario ────────────────────────────────
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.CalendarToday, null, tint = UATBlueDark, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Mi Horario", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                                if (isUploadingDoc) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = UATBlueDark)
                                } else {
                                    Text("Subir horario", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = UATBlueDark, modifier = Modifier.clickable { docPickerLauncher.launch("image/*") })
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            if (!p.urlHorario.isNullOrBlank()) {
                                Box(modifier = Modifier.fillMaxWidth().height(48.dp).background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Image, null, tint = UATBlueDark, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Horario disponible", fontSize = 14.sp, color = UATBlueDark, fontWeight = FontWeight.Medium)
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(120.dp).background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Filled.CalendarMonth, null, tint = Color.Gray.copy(alpha = 0.5f), modifier = Modifier.size(36.dp))
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Sin horario subido aún", fontSize = 13.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ── Ajustes ───────────────────────────────────
                    Text("Ajustes", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = UATBlueDark)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column {
                            SettingsOptionRow(Icons.Filled.Person, "Configurar Perfil", onClick = onNavigateToEditProfile)
                            Divider(color = Color(0xFFF3F4F6))
                            SettingsOptionRow(Icons.Outlined.Lock, "Privacidad y Seguridad", onClick = onNavigateToSecurity)
                            Divider(color = Color(0xFFF3F4F6))
                            SettingsOptionRow(Icons.Outlined.Notifications, "Notificaciones", onClick = { })
                            Divider(color = Color(0xFFF3F4F6))
                            SettingsOptionRow(Icons.Outlined.HelpOutline, "Ayuda y Soporte", onClick = { })
                            Divider(color = Color(0xFFF3F4F6))
                            SettingsOptionRow(Icons.Outlined.Info, "Acerca de UATLife v2.0", onClick = { })
                        }
                    }

                    // Panel de Moderador
                    if (userType == "moderador") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, UATOrange),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            SettingsOptionRow(Icons.Filled.AdminPanelSettings, "Panel de Moderación 🚨", onClick = onNavigateToModeration)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Cerrar Sesión
                    Button(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFE4E6)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Logout, null, tint = Color(0xFFE11D48), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("CERRAR SESIÓN", fontWeight = FontWeight.SemiBold, color = Color(0xFFE11D48), letterSpacing = 1.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: Color = Color.DarkGray
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = UATBlueDark.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Medium, letterSpacing = 0.3.sp)
            Text(value, fontSize = 13.sp, color = valueColor, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SettingsOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, fontSize = 15.sp, color = Color.DarkGray, modifier = Modifier.weight(1f))
        Icon(Icons.Filled.ChevronRight, null, tint = Color.LightGray)
    }
}

@Composable
private fun StatBox(icon: String, label: String, value: String) {
    Column(
        modifier = Modifier.size(width = 96.dp, height = 80.dp)
            .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(icon, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 0.5.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color.Black)
    }
}

// Helper — evitar importar la clase directa en este scope
@Composable private fun UATBlueLight() = com.uat.uatlife.ui.theme.UATBlueLight
