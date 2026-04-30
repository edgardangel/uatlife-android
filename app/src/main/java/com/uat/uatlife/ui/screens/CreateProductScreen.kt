package com.uat.uatlife.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uat.uatlife.network.RetrofitClient
import com.uat.uatlife.network.models.Categoria
import com.uat.uatlife.network.models.CrearProductoRequest
import com.uat.uatlife.ui.theme.UATBlueDark
import com.uat.uatlife.ui.theme.UATOrange
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProductScreen(
    initialNombre: String = "",
    initialPrecio: String = "",
    initialDescripcion: String = "",
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = remember { RetrofitClient.getApiService(context) }
    val scrollState = rememberScrollState()

    var titulo by remember { mutableStateOf(initialNombre) }
    var precio by remember { mutableStateOf(initialPrecio) }
    var categoria by remember { mutableStateOf<Categoria?>(null) }
    var condicion by remember { mutableStateOf("nuevo") }
    var descripcion by remember { mutableStateOf(initialDescripcion) }
    var isLoading by remember { mutableStateOf(false) }

    val categorias = remember { mutableStateListOf<Categoria>() }
    val isEditing = initialNombre.isNotEmpty()

    // Cargar categorías desde la API
    LaunchedEffect(Unit) {
        try {
            val resp = apiService.getCategorias()
            if (resp.isSuccessful) {
                categorias.clear()
                categorias.addAll(resp.body() ?: emptyList())
            }
        } catch (_: Exception) {}
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
                    .height(120.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.AddPhotoAlternate, contentDescription = "Agregar fotos", tint = Color.Gray, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Agregar Fotos (0/5)", color = Color.Gray, fontWeight = FontWeight.Medium)
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
                singleLine = true
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
                    isLoading = true
                    scope.launch {
                        try {
                            val resp = apiService.crearProducto(
                                CrearProductoRequest(
                                    titulo = titulo.trim(),
                                    descripcion = descripcion.ifBlank { null },
                                    precio = precioNum,
                                    categoriaId = categoria?.id,
                                    condicion = condicion,
                                    facultadId = null
                                )
                            )
                            if (resp.isSuccessful) {
                                Toast.makeText(context, "¡Producto publicado exitosamente!", Toast.LENGTH_SHORT).show()
                                onBack()
                            } else {
                                Toast.makeText(context, "Error al publicar producto", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Sin conexión. Intenta de nuevo.", Toast.LENGTH_SHORT).show()
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
