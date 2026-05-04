package com.uat.uatlife.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import com.uat.uatlife.network.RetrofitClient
import com.uat.uatlife.network.models.Categoria
import com.uat.uatlife.ui.theme.UATBlueDark
import com.uat.uatlife.ui.theme.UATOrange
import com.uat.uatlife.utils.ImageUtils
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProductScreen(
    productId: Int = 0,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = remember { RetrofitClient.getApiService(context) }
    val imageLoader = remember { RetrofitClient.getImageLoader(context) }
    val scrollState = rememberScrollState()

    var titulo by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf<Categoria?>(null) }
    var condicion by remember { mutableStateOf("nuevo") }
    var descripcion by remember { mutableStateOf("") }
    var horaInicio by remember { mutableStateOf<String?>(null) }
    var horaFin by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isFetchingDetails by remember { mutableStateOf(false) }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var existingImageUrl by remember { mutableStateOf<String?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    val categorias = remember { mutableStateListOf<Categoria>() }
    val isEditing = productId != 0

    // Cargar categorías y detalles del producto si es edición
    LaunchedEffect(Unit) {
        // Cargar categorías
        try {
            val resp = apiService.getCategorias()
            if (resp.isSuccessful) {
                categorias.clear()
                categorias.addAll(resp.body() ?: emptyList())
            }
        } catch (_: Exception) {}

        // Cargar detalles si es edición
        if (isEditing) {
            isFetchingDetails = true
            try {
                val resp = apiService.getProductoById(productId)
                if (resp.isSuccessful) {
                    val p = resp.body()
                    if (p != null) {
                        titulo = p.titulo
                        precio = p.precio.toString()
                        descripcion = p.descripcion ?: ""
                        condicion = p.condicion
                        existingImageUrl = p.urlFotoPrincipal
                        horaInicio = p.horaInicio
                        horaFin = p.horaFin
                        // Intentar encontrar la categoría en la lista cargada
                        categoria = categorias.find { it.nombre == p.categoria }
                    }
                }
            } catch (_: Exception) {}
            finally { isFetchingDetails = false }
        }
    }

    // Helper para mostrar TimePicker
    fun showTimePicker(onTimeSelected: (String) -> Unit) {
        val calendar = java.util.Calendar.getInstance()
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = calendar.get(java.util.Calendar.MINUTE)

        android.app.TimePickerDialog(context, { _, h, m ->
            val formatted = String.format("%02d:%02d", h, m)
            onTimeSelected(formatted)
        }, hour, minute, true).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Publicación" else "Crear Publicación", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        if (isFetchingDetails) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = UATOrange)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9FAFB))
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Sección de Fotos
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                    .clickable { imagePickerLauncher.launch("image/*") }
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Foto del producto",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        imageLoader = imageLoader
                    )
                } else if (existingImageUrl != null) {
                    AsyncImage(
                        model = RetrofitClient.BASE_URL + existingImageUrl!!.removePrefix("/"),
                        contentDescription = "Foto actual",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        imageLoader = imageLoader
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.AddPhotoAlternate, contentDescription = "Agregar foto", tint = Color.Gray, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Toca para agregar foto", color = Color.Gray, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Título
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título del producto") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Precio
            OutlinedTextField(
                value = precio,
                onValueChange = { precio = it },
                label = { Text("Precio ($)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Categoría desde API
            Column {
                Text("Categoría", fontSize = 14.sp, color = Color.DarkGray, modifier = Modifier.padding(bottom = 4.dp))
                if (categorias.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = UATOrange, strokeWidth = 2.dp)
                } else {
                    // Primera fila de categorías
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        categorias.take(4).forEach { cat ->
                            FilterChip(
                                selected = categoria?.id == cat.id,
                                onClick = { categoria = cat },
                                label = { Text(cat.nombre, fontSize = 12.sp) }
                            )
                        }
                    }
                    // Segunda fila
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                        categorias.drop(4).forEach { cat ->
                            FilterChip(
                                selected = categoria?.id == cat.id,
                                onClick = { categoria = cat },
                                label = { Text(cat.nombre, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }

            // Horario de Venta (Opcional)
            Column {
                Text("Horario de Venta (Opcional)", fontSize = 14.sp, color = Color.DarkGray, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Hora Inicio
                    OutlinedCard(
                        onClick = { showTimePicker { horaInicio = it } },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Desde", fontSize = 11.sp, color = Color.Gray)
                            Text(horaInicio ?: "--:--", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (horaInicio != null) UATOrange else Color.DarkGray)
                        }
                    }
                    // Hora Fin
                    OutlinedCard(
                        onClick = { showTimePicker { horaFin = it } },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Hasta", fontSize = 11.sp, color = Color.Gray)
                            Text(horaFin ?: "--:--", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (horaFin != null) UATOrange else Color.DarkGray)
                        }
                    }
                    // Limpiar
                    if (horaInicio != null || horaFin != null) {
                        IconButton(onClick = { horaInicio = null; horaFin = null }) {
                            Icon(Icons.Filled.Close, contentDescription = "Limpiar horario", tint = Color.Gray)
                        }
                    }
                }
                Text("Si activas un horario, el producto solo aparecerá en el mercado durante esas horas.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
            }

            // Condición
            Column {
                Text("Condición", fontSize = 14.sp, color = Color.DarkGray, modifier = Modifier.padding(bottom = 4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("nuevo", "como_nuevo", "usado", "para_piezas").forEach { cond ->
                        FilterChip(
                            selected = condicion == cond,
                            onClick = { condicion = cond },
                            label = { Text(cond.replace("_", " ").replaceFirstChar { it.uppercase() }, fontSize = 12.sp) }
                        )
                    }
                }
            }

            // Descripción
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 5,
                placeholder = { Text("Describe el estado del producto, especificaciones, etc.") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón Publicar
            Button(
                onClick = {
                    // Validar campos obligatorios
                    if (titulo.isBlank()) {
                        Toast.makeText(context, "El título es obligatorio", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val precioNum = precio.toDoubleOrNull()
                    if (precioNum == null || precioNum < 0) {
                        Toast.makeText(context, "Ingresa un precio válido", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (selectedImageUri == null && existingImageUrl == null) {
                        Toast.makeText(context, "Debes subir al menos una foto", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    isLoading = true
                    scope.launch {
                        try {
                            if (!isEditing) {
                                // Crear
                                val tituloBody = titulo.trim().toRequestBody("text/plain".toMediaTypeOrNull())
                                val descBody = descripcion.trim().toRequestBody("text/plain".toMediaTypeOrNull())
                                val precioBody = precioNum.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                                val condBody = condicion.toRequestBody("text/plain".toMediaTypeOrNull())
                                val catBody = categoria?.id?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
                                val hIniBody = horaInicio?.toRequestBody("text/plain".toMediaTypeOrNull())
                                val hFinBody = horaFin?.toRequestBody("text/plain".toMediaTypeOrNull())
                                
                                val fotoPart = ImageUtils.uriToMultipart(context, selectedImageUri!!, "foto")

                                val resp = apiService.crearProducto(
                                    tituloBody, descBody, precioBody, condBody, catBody, hIniBody, hFinBody, fotoPart
                                )

                                if (resp.isSuccessful) {
                                    Toast.makeText(context, "¡Producto publicado!", Toast.LENGTH_SHORT).show()
                                    onBack()
                                } else {
                                    Toast.makeText(context, "Error al publicar", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                // Actualizar (ahora también por Multipart)
                                val tituloBody = titulo.trim().toRequestBody("text/plain".toMediaTypeOrNull())
                                val descBody = descripcion.trim().toRequestBody("text/plain".toMediaTypeOrNull())
                                val precioBody = precioNum.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                                val condBody = condicion.toRequestBody("text/plain".toMediaTypeOrNull())
                                val catBody = categoria?.id?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
                                val hIniBody = horaInicio?.toRequestBody("text/plain".toMediaTypeOrNull())
                                val hFinBody = horaFin?.toRequestBody("text/plain".toMediaTypeOrNull())
                                
                                val fotoPart = if (selectedImageUri != null) {
                                    ImageUtils.uriToMultipart(context, selectedImageUri!!, "foto")
                                } else null

                                val resp = apiService.actualizarProducto(
                                    productId, tituloBody, descBody, precioBody, condBody, catBody, hIniBody, hFinBody, fotoPart
                                )

                                if (resp.isSuccessful) {
                                    Toast.makeText(context, "¡Producto actualizado!", Toast.LENGTH_SHORT).show()
                                    onBack()
                                } else {
                                    val errorMsg = resp.errorBody()?.string() ?: "Error desconocido"
                                    Toast.makeText(context, "Error al actualizar: $errorMsg", Toast.LENGTH_LONG).show()
                                }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UATOrange),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(if (isEditing) "Guardar Cambios" else "Publicar Producto", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
