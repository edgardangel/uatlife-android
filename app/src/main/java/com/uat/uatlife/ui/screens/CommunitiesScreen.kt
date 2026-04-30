package com.uat.uatlife.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.uat.uatlife.network.models.Comunidad
import com.uat.uatlife.network.models.CrearComunidadRequest
import com.uat.uatlife.ui.theme.UATBlue
import com.uat.uatlife.ui.theme.UATBlueDark
import com.uat.uatlife.ui.theme.UATOrange
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunitiesScreen(onNavigateToCommunity: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = remember { RetrofitClient.getApiService(context) }
    val tokenManager = remember { TokenManager(context) }
    val userType by tokenManager.getUserType().collectAsState(initial = "alumno")

    val comunidades = remember { mutableStateListOf<Comunidad>() }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var nuevoNombre by remember { mutableStateOf("") }
    var nuevaDesc by remember { mutableStateOf("") }
    var nuevoTipo by remember { mutableStateOf("publica") }
    var isCreating by remember { mutableStateOf(false) }

    fun cargarComunidades() {
        scope.launch {
            isLoading = true
            try {
                val resp = apiService.getComunidades()
                if (resp.isSuccessful) {
                    comunidades.clear()
                    comunidades.addAll(resp.body() ?: emptyList())
                }
            } catch (_: Exception) { } finally { isLoading = false }
        }
    }

    LaunchedEffect(Unit) { cargarComunidades() }

    val misComunidades = comunidades.filter { it.esMiembro && (searchQuery.isBlank() || it.nombre.contains(searchQuery, true)) }
    val sugeridas = comunidades.filter { !it.esMiembro && (searchQuery.isBlank() || it.nombre.contains(searchQuery, true)) }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Nueva Comunidad", fontWeight = FontWeight.Bold, color = UATBlueDark) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nuevoNombre, onValueChange = { nuevoNombre = it },
                        label = { Text("Nombre") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = UATOrange)
                    )
                    OutlinedTextField(
                        value = nuevaDesc, onValueChange = { nuevaDesc = it },
                        label = { Text("Descripción (opcional)") },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = UATOrange)
                    )
                    Text("Tipo:", fontSize = 13.sp, color = UATBlueDark, fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("publica", "privada").forEach { tipo ->
                            FilterChip(selected = nuevoTipo == tipo, onClick = { nuevoTipo = tipo },
                                label = { Text(tipo.replaceFirstChar { it.uppercase() }) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = UATBlueDark, selectedLabelColor = Color.White)
                            )
                        }
                        if (userType == "moderador") {
                            FilterChip(selected = nuevoTipo == "oficial", onClick = { nuevoTipo = "oficial" },
                                label = { Text("Oficial") },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = UATOrange, selectedLabelColor = Color.White)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nuevoNombre.isBlank()) return@Button
                        isCreating = true
                        scope.launch {
                            try {
                                val resp = apiService.crearComunidad(CrearComunidadRequest(nombre = nuevoNombre.trim(), descripcion = nuevaDesc.ifBlank { null }, tipo = nuevoTipo))
                                if (resp.isSuccessful) { showCreateDialog = false; nuevoNombre = ""; nuevaDesc = ""; cargarComunidades() }
                                else Toast.makeText(context, "Error al crear comunidad", Toast.LENGTH_SHORT).show()
                            } catch (_: Exception) { Toast.makeText(context, "Sin conexión", Toast.LENGTH_SHORT).show() }
                            finally { isCreating = false }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UATOrange), enabled = !isCreating
                ) {
                    if (isCreating) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    else Text("Crear", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text("Cancelar", color = UATBlueDark) } }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF3F4F9))) {
        Row(
            modifier = Modifier.fillMaxWidth().background(UATBlue)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Comunidades", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
            IconButton(onClick = { showCreateDialog = true },
                modifier = Modifier.size(40.dp).background(UATOrange, RoundedCornerShape(12.dp))
            ) { Icon(Icons.Filled.Add, "Nueva comunidad", tint = Color.White) }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = UATOrange, modifier = Modifier.size(44.dp))
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                item {
                    OutlinedTextField(
                        value = searchQuery, onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar grupos o comunidades...", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Filled.Search, null, tint = Color.Gray) },
                        trailingIcon = { if (searchQuery.isNotBlank()) IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Filled.Close, null, tint = Color.Gray, modifier = Modifier.size(18.dp)) } },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = UATBlueDark, unfocusedBorderColor = Color.Transparent, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White),
                        shape = RoundedCornerShape(26.dp), singleLine = true
                    )
                }

                if (misComunidades.isNotEmpty()) {
                    item { Spacer(modifier = Modifier.height(4.dp)); Text("Mis Comunidades (${misComunidades.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray) }
                    items(misComunidades, key = { it.id }) { com ->
                        ComunidadCard(com, esMiembro = true, onClick = { onNavigateToCommunity(com.id.toString()) }, onUnirse = null)
                    }
                }

                if (sugeridas.isNotEmpty()) {
                    item { Spacer(modifier = Modifier.height(4.dp)); Text("Descubrir Comunidades", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray) }
                    items(sugeridas, key = { it.id }) { com ->
                        ComunidadCard(com, esMiembro = false, onClick = { onNavigateToCommunity(com.id.toString()) },
                            onUnirse = {
                                scope.launch {
                                    try { apiService.unirseAComunidad(com.id); cargarComunidades() }
                                    catch (_: Exception) { Toast.makeText(context, "Error al unirse", Toast.LENGTH_SHORT).show() }
                                }
                            })
                    }
                }

                if (misComunidades.isEmpty() && sugeridas.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.Groups, null, tint = UATBlueDark.copy(alpha = 0.3f), modifier = Modifier.size(56.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(if (searchQuery.isBlank()) "Aún no hay comunidades.\n¡Crea la primera!" else "Sin resultados para \"$searchQuery\"", color = Color.Gray, fontSize = 14.sp)
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun ComunidadCard(comunidad: Comunidad, esMiembro: Boolean, onClick: () -> Unit, onUnirse: (() -> Unit)?) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = if (esMiembro) Color(0xFFF0F7FF) else Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(52.dp).clip(RoundedCornerShape(10.dp))
                    .background(when (comunidad.tipo) { "oficial" -> UATOrange; "privada" -> Color(0xFF6B7280); else -> UATBlueDark }),
                contentAlignment = Alignment.Center
            ) {
                Icon(if (comunidad.esOficial) Icons.Filled.Verified else Icons.Filled.Group, null, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(comunidad.nombre, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
                    if (comunidad.esOficial) { Spacer(modifier = Modifier.width(4.dp)); Icon(Icons.Filled.Verified, null, tint = UATOrange, modifier = Modifier.size(14.dp)) }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.background(if (comunidad.tipo == "oficial") UATOrange.copy(alpha = 0.15f) else Color(0xFFEFF6FF), RoundedCornerShape(4.dp)).padding(horizontal = 5.dp, vertical = 1.dp)) {
                        Text(comunidad.tipo.replaceFirstChar { it.uppercase() }, fontSize = 10.sp, color = if (comunidad.tipo == "oficial") UATOrange else UATBlueDark, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Filled.People, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("${comunidad.totalMiembros} miembros", fontSize = 12.sp, color = Color.Gray)
                }
                if (!comunidad.descripcion.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(comunidad.descripcion, fontSize = 12.sp, color = Color.Gray, maxLines = 1)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (onUnirse != null) {
                Button(onClick = { onUnirse() }, colors = ButtonDefaults.buttonColors(containerColor = UATBlueDark), shape = RoundedCornerShape(8.dp), modifier = Modifier.height(32.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)) {
                    Text("Unirse", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            } else {
                Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF16A34A), modifier = Modifier.size(22.dp))
            }
        }
    }
}
