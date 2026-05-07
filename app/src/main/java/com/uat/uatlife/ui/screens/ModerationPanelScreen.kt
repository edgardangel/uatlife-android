package com.uat.uatlife.ui.screens

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
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uat.uatlife.network.RetrofitClient
import com.uat.uatlife.network.models.ResolverValidacionRequest
import com.uat.uatlife.network.models.UsuarioSancionado
import com.uat.uatlife.network.models.ValidacionPendiente
import com.uat.uatlife.ui.theme.UATBlueDark
import com.uat.uatlife.ui.theme.UATOrange
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import coil.compose.AsyncImage
import coil.ImageLoader
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModerationPanelScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = remember { RetrofitClient.getApiService(context) }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Validaciones", "Sancionados")

    // Estados
    var isLoading by remember { mutableStateOf(false) }
    val validaciones = remember { mutableStateListOf<ValidacionPendiente>() }
    val sancionados = remember { mutableStateListOf<UsuarioSancionado>() }

    fun cargarDatos() {
        isLoading = true
        scope.launch {
            try {
                if (selectedTab == 0) {
                    val resp = apiService.getValidacionesPendientes()
                    if (resp.isSuccessful) {
                        validaciones.clear()
                        validaciones.addAll(resp.body() ?: emptyList())
                    }
                } else if (selectedTab == 1) {
                    val resp = apiService.getSancionados()
                    if (resp.isSuccessful) {
                        sancionados.clear()
                        sancionados.addAll(resp.body() ?: emptyList())
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al cargar datos", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(selectedTab) { cargarDatos() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Moderación", fontWeight = FontWeight.Bold, color = UATBlueDark) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = UATBlueDark)
                    }
                },
                actions = {
                    IconButton(onClick = { cargarDatos() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Actualizar", tint = UATBlueDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().background(Color(0xFFF9FAFB)).padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = UATBlueDark,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = UATOrange,
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = UATOrange)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (selectedTab == 0) {
                        if (validaciones.isEmpty()) {
                            item { Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { Text("No hay validaciones pendientes", color = Color.Gray) } }
                        } else {
                            items(validaciones) { v ->
                                ValidacionCard(
                                    validacion = v,
                                    onResolver = { estatus ->
                                        scope.launch {
                                            try {
                                                val resp = apiService.resolverValidacion(v.id, ResolverValidacionRequest(estatus))
                                                if (resp.isSuccessful) {
                                                    Toast.makeText(context, "Usuario $estatus", Toast.LENGTH_SHORT).show()
                                                    cargarDatos()
                                                }
                                            } catch (e: Exception) {}
                                        }
                                    }
                                )
                            }
                        }
                    } else if (selectedTab == 1) {
                        if (sancionados.isEmpty()) {
                            item { Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { Text("No hay usuarios sancionados", color = Color.Gray) } }
                        } else {
                            items(sancionados) { u ->
                                SancionadoCard(
                                    usuario = u,
                                    onRevocar = {
                                        scope.launch {
                                            try {
                                                val json = JSONObject().apply {
                                                    put("usuario_id", u.id)
                                                    put("tipo_sancion", "levantamiento")
                                                    put("motivo", "Revocado por moderador")
                                                }.toString()
                                                val body = json.toRequestBody("application/json".toMediaTypeOrNull())
                                                val resp = apiService.sancionarUsuario(body)
                                                if (resp.isSuccessful) {
                                                    Toast.makeText(context, "Sanción revocada", Toast.LENGTH_SHORT).show()
                                                    cargarDatos()
                                                }
                                            } catch (e: Exception) {}
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ValidacionCard(validacion: ValidacionPendiente, onResolver: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(validacion.nombreCompleto, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = UATBlueDark)
            Text("${validacion.matricula} • ${validacion.correoInstitucional}", fontSize = 13.sp, color = Color.Gray)
            
            if (!validacion.urlHorarioImg.isNullOrBlank() || !validacion.urlFichaPago.isNullOrBlank()) {
                val urlDoc = validacion.urlHorarioImg ?: validacion.urlFichaPago
                val labelDoc = if (!validacion.urlHorarioImg.isNullOrBlank()) "Horario subido:" else "Ficha de pago:"
                
                Spacer(modifier = Modifier.height(12.dp))
                Text(labelDoc, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                val context = LocalContext.current
                val imageLoader = remember {
                    ImageLoader.Builder(context).okHttpClient { RetrofitClient.getUnsafeOkHttpClient() }.build()
                }
                Box(modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFE5E7EB))) {
                    AsyncImage(
                        model = "https://157.245.239.94$urlDoc",
                        contentDescription = "Documento",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        imageLoader = imageLoader
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(
                    onClick = { onResolver("validado") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Aprobar") }
                
                Button(
                    onClick = { onResolver("rechazado") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Rechazar") }
            }
        }
    }
}

@Composable
private fun SancionadoCard(usuario: UsuarioSancionado, onRevocar: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)), // Fondo rojizo
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Block, null, tint = Color(0xFFEF4444), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(usuario.nombreCompleto, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF991B1B))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(usuario.matricula, fontSize = 13.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            
            val estado = if (usuario.banPermanente) "Ban Permanente" else "Suspendido hasta: ${usuario.suspensionHasta}"
            Text("Estado: $estado", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
            Text("Motivo: ${usuario.motivoSancion ?: "Sin especificar"}", fontSize = 13.sp, color = Color.DarkGray)
            
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onRevocar,
                colors = ButtonDefaults.buttonColors(containerColor = UATBlueDark),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Revocar Sanción", color = Color.White)
            }
        }
    }
}
