package com.uat.uatlife.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.uat.uatlife.data.TokenManager
import com.uat.uatlife.network.RetrofitClient
import com.uat.uatlife.network.models.Categoria
import com.uat.uatlife.network.models.Producto
import com.uat.uatlife.ui.theme.*
import kotlinx.coroutines.launch
import coil.ImageLoader
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(
    onNavigateToSellerProfile: () -> Unit,
    onNavigateToCreateProduct: () -> Unit,
    onNavigateToProduct: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = remember { RetrofitClient.getApiService(context) }
    val tokenManager = remember { TokenManager(context) }
    val userType by tokenManager.getUserType().collectAsState(initial = "alumno")
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .okHttpClient { RetrofitClient.getUnsafeOkHttpClient() }
            .build()
    }

    // Estado del mercado
    val productos = remember { mutableStateListOf<Producto>() }
    val categorias = remember { mutableStateListOf<Categoria>() }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Filtros
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoriaId by remember { mutableStateOf<Int?>(null) }
    var selectedCategoriaNombre by remember { mutableStateOf("Todos") }

    // --- Función de carga ---
    fun cargarProductos() {
        scope.launch {
            isRefreshing = true
            errorMsg = null
            try {
                val resp = apiService.getProductos(
                    categoriaId = selectedCategoriaId,
                    busqueda = searchQuery.ifBlank { null }
                )
                if (resp.isSuccessful) {
                    productos.clear()
                    productos.addAll(resp.body() ?: emptyList())
                } else {
                    errorMsg = "Error al cargar productos"
                }
            } catch (e: Exception) {
                errorMsg = "Sin conexión. Verifica tu red."
            } finally {
                isLoading = false
                isRefreshing = false
            }
        }
    }

    // Cargar categorías (una sola vez)
    LaunchedEffect(Unit) {
        try {
            val respCat = apiService.getCategorias()
            if (respCat.isSuccessful) {
                categorias.clear()
                categorias.addAll(respCat.body() ?: emptyList())
            }
        } catch (_: Exception) {}
        cargarProductos()
    }

    // Re-cargar cuando cambia el filtro de categoría
    LaunchedEffect(selectedCategoriaId) {
        if (!isLoading) cargarProductos()
    }

    // Filtro local por texto (no recarga el servidor)
    val productosFiltrados = if (searchQuery.isBlank()) {
        productos
    } else {
        productos.filter {
            it.titulo.contains(searchQuery, ignoreCase = true) ||
            it.descripcion?.contains(searchQuery, ignoreCase = true) == true ||
            it.vendedorNombre.contains(searchQuery, ignoreCase = true) ||
            it.categoria?.contains(searchQuery, ignoreCase = true) == true
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF9FAFB))
    ) {
        // ── Top App Bar ────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF9FAFB))
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Market",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = UATBlueDark,
                modifier = Modifier.weight(1f)
            )
            // Refrescar
            IconButton(onClick = { cargarProductos() }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.Refresh, "Actualizar", tint = UATBlueDark, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(4.dp))
            // Crear producto
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(UATOrange)
                    .clickable(onClick = onNavigateToCreateProduct),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Add, "Publicar producto", tint = Color.White, modifier = Modifier.size(22.dp))
            }
            // Perfil vendedor
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable(onClick = onNavigateToSellerProfile),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Person, "Mi perfil vendedor", tint = UATBlueDark, modifier = Modifier.size(22.dp))
            }
        }

        // ── Contenido ─────────────────────────────────────────────
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = UATOrange, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Cargando productos...", color = UATBlueDark.copy(alpha = 0.6f), fontSize = 14.sp)
                    }
                }
            }

            errorMsg != null && productos.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Icon(Icons.Filled.WifiOff, null, tint = UATBlueLight, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(errorMsg!!, color = UATBlueDark, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { cargarProductos() }, colors = ButtonDefaults.buttonColors(containerColor = UATOrange)) {
                            Text("Reintentar", color = Color.White)
                        }
                    }
                }
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Buscador
                    item(span = { GridItemSpan(2) }) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Buscar productos, comida, servicios...", color = Color.Gray, fontSize = 14.sp) },
                            leadingIcon = { Icon(Icons.Filled.Search, "Buscar", tint = Color.Gray) },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Filled.Close, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = UATOrange,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color(0xFFF0F2F5),
                                unfocusedContainerColor = Color(0xFFF0F2F5)
                            ),
                            shape = RoundedCornerShape(26.dp)
                        )
                    }

                    // Categorías desde la API
                    item(span = { GridItemSpan(2) }) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            // Chip "Todos"
                            item {
                                val isTodosSelected = selectedCategoriaId == null
                                FilterChip(
                                    selected = isTodosSelected,
                                    onClick = {
                                        selectedCategoriaId = null
                                        selectedCategoriaNombre = "Todos"
                                    },
                                    label = { Text("Todos", fontWeight = if (isTodosSelected) FontWeight.SemiBold else FontWeight.Medium) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = UATBlueDark,
                                        selectedLabelColor = Color.White,
                                        containerColor = Color.White,
                                        labelColor = Color.DarkGray
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true, selected = isTodosSelected,
                                        borderColor = if (isTodosSelected) UATBlueDark else Color.LightGray,
                                        disabledBorderColor = Color.LightGray
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                )
                            }
                            // Chips de categorías reales
                            items(categorias.size) { index ->
                                val cat = categorias[index]
                                val isSelected = selectedCategoriaId == cat.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedCategoriaId = cat.id
                                        selectedCategoriaNombre = cat.nombre
                                    },
                                    label = { Text(cat.nombre, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = UATBlueDark,
                                        selectedLabelColor = Color.White,
                                        containerColor = Color.White,
                                        labelColor = Color.DarkGray
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true, selected = isSelected,
                                        borderColor = if (isSelected) UATBlueDark else Color.LightGray,
                                        disabledBorderColor = Color.LightGray
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                )
                            }
                        }
                    }

                    // Header de resultados
                    item(span = { GridItemSpan(2) }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (searchQuery.isBlank()) selectedCategoriaNombre else "Resultados: \"$searchQuery\"",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = UATBlueDark,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "${productosFiltrados.size} productos",
                                fontSize = 12.sp, color = Color.Gray
                            )
                            if (isRefreshing) {
                                Spacer(modifier = Modifier.width(8.dp))
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = UATOrange)
                            }
                        }
                    }

                    // Estado vacío
                    if (productosFiltrados.isEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Filled.ShoppingBag, null, tint = UATBlueLight.copy(alpha = 0.4f), modifier = Modifier.size(56.dp))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        if (searchQuery.isNotBlank()) "Sin resultados para \"$searchQuery\""
                                        else "Aún no hay productos en esta categoría.\n¡Publica el primero!",
                                        color = Color.Gray, fontSize = 14.sp
                                    )
                                    if (searchQuery.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        TextButton(onClick = { searchQuery = "" }) {
                                            Text("Limpiar búsqueda", color = UATOrange)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Grilla de productos
                    items(productosFiltrados, key = { it.id }) { producto ->
                        ProductoGridCard(
                            producto = producto,
                            esModerador = userType == "moderador",
                            imageLoader = imageLoader,
                            onClick = { onNavigateToProduct(producto.id.toString()) },
                            onEliminar = {
                                scope.launch {
                                    try {
                                        val resp = apiService.eliminarProducto(producto.id)
                                        if (resp.isSuccessful) {
                                            productos.remove(producto)
                                            Toast.makeText(context, "Producto eliminado", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }

                    item(span = { GridItemSpan(2) }) { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ProductoGridCard(
    producto: Producto,
    esModerador: Boolean,
    imageLoader: ImageLoader,
    onClick: () -> Unit,
    onEliminar: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var isFavorito by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("¿Eliminar producto?", fontWeight = FontWeight.Bold) },
            text = { Text("Se ocultará del marketplace. Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = { showDeleteConfirm = false; onEliminar() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                ) { Text("Eliminar", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancelar") }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            // Imagen / Placeholder
            Box(
                modifier = Modifier.fillMaxWidth().height(140.dp).background(Color(0xFFE5E7EB))
            ) {
                if (!producto.urlFotoPrincipal.isNullOrEmpty()) {
                    AsyncImage(
                        model = RetrofitClient.BASE_URL + producto.urlFotoPrincipal!!.removePrefix("/"),
                        contentDescription = producto.titulo,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        imageLoader = imageLoader
                    )
                } else {
                    // Ícono de categoría como placeholder
                    val iconForBg = when (producto.categoria?.lowercase()) {
                        "comida"       -> Icons.Filled.Fastfood
                        "libros"       -> Icons.Filled.MenuBook
                        "apuntes"      -> Icons.Filled.Description
                        "electrónica"  -> Icons.Filled.Computer
                        "servicios"    -> Icons.Filled.Build
                        "ropa"         -> Icons.Filled.Checkroom
                        "deporte"      -> Icons.Filled.SportsHandball
                        "arte"         -> Icons.Filled.Palette
                        else           -> Icons.Filled.ShoppingBag
                    }
                    Icon(iconForBg, null, tint = Color.Gray.copy(alpha = 0.4f),
                        modifier = Modifier.size(60.dp).align(Alignment.Center))
                }

                // Badge Premium
                if (producto.esPremium) {
                    Box(
                        modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
                            .background(UATOrange, RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Premium", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Favorito / Eliminar
                if (esModerador) {
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(28.dp)
                            .background(Color(0xFFE11D48).copy(alpha = 0.9f), CircleShape)
                    ) {
                        Icon(Icons.Filled.Delete, "Eliminar", tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                } else {
                    IconButton(
                        onClick = { isFavorito = !isFavorito },
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(28.dp)
                            .background(Color.White.copy(alpha = 0.9f), CircleShape)
                    ) {
                        Icon(
                            if (isFavorito) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            null,
                            tint = if (isFavorito) UATOrange else UATBlueDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                // Precio y Rating
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${String.format("%.0f", producto.precio)}",
                        fontWeight = FontWeight.Black,
                        color = UATBlueDark,
                        fontSize = 18.sp
                    )
                    if (producto.calificacionPromedio > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, null, tint = UATOrange, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("${producto.calificacionPromedio}", fontSize = 11.sp, color = Color.DarkGray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Título
                Text(
                    text = producto.titulo,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp,
                    modifier = Modifier.height(34.dp)
                )

                // Condición badge
                Box(
                    modifier = Modifier.padding(vertical = 4.dp)
                        .background(
                            if (producto.condicion == "nuevo") Color(0xFFDCFCE7) else Color(0xFFFEF9C3),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = producto.condicion.replaceFirstChar { it.uppercase() },
                        fontSize = 9.sp,
                        color = if (producto.condicion == "nuevo") Color(0xFF166534) else Color(0xFF854D0E),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Vendedor
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Person, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        producto.vendedorNombre, fontSize = 11.sp, color = Color.Gray,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }

                // Confianza del vendedor
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                    Icon(Icons.Filled.Shield, null, tint = when {
                        producto.vendedorConfianza >= 80 -> Color(0xFF16A34A)
                        producto.vendedorConfianza >= 50 -> UATOrange
                        else -> Color.Gray
                    }, modifier = Modifier.size(11.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        "${producto.vendedorConfianza}% confianza",
                        fontSize = 10.sp, color = Color.Gray
                    )
                }

                // Ubicación de entrega
                if (!producto.facultadEntrega.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                        Icon(Icons.Filled.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            producto.facultadEntrega, fontSize = 9.sp, color = Color.Gray,
                            maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
