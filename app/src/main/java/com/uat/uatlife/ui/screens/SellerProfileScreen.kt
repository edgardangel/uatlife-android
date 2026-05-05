package com.uat.uatlife.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import com.uat.uatlife.network.RetrofitClient
import com.uat.uatlife.network.models.Producto
import com.uat.uatlife.ui.theme.UATBlueDark
import com.uat.uatlife.ui.theme.UATOrange
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerProfileScreen(
    onNavigateToCreateProduct: () -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    onNavigateToProductDetail: (Int) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = remember { RetrofitClient.getApiService(context) }
    val imageLoader = remember { RetrofitClient.getImageLoader(context) }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("En Venta", "Vendidos")

    val misProductos = remember { mutableStateListOf<Producto>() }
    var perfil by remember { mutableStateOf<com.uat.uatlife.network.models.UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    fun refreshProducts() {
        isLoading = true
        scope.launch {
            try {
                // Cargar perfil para mostrar foto y puntos
                val respP = apiService.getProfile()
                if (respP.isSuccessful) perfil = respP.body()

                val resp = apiService.getMisProductos()
                if (resp.isSuccessful) {
                    misProductos.clear()
                    misProductos.addAll(resp.body() ?: emptyList())
                }
            } catch (_: Exception) {}
            finally { isLoading = false }
        }
    }

    LaunchedEffect(Unit) {
        refreshProducts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil de Vendedor", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = UATBlueDark) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = UATBlueDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = onNavigateToCreateProduct,
                    containerColor = UATOrange,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Publicar Producto")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9FAFB))
                .padding(padding)
        ) {
            // Perfil Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(65.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE5E7EB)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (perfil?.urlFotoPerfil != null) {
                            AsyncImage(
                                model = RetrofitClient.BASE_URL + perfil?.urlFotoPerfil?.removePrefix("/"),
                                contentDescription = "Foto de perfil",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                imageLoader = imageLoader
                            )
                        } else {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(35.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = perfil?.nombreCompleto ?: "Cargando...",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = UATBlueDark
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.VerifiedUser, null, tint = UATOrange, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Confianza: ${perfil?.puntosConfianza ?: 0} pts",
                                fontSize = 13.sp,
                                color = UATOrange,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Tabs Venta / Vendidos
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
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Lista de productos
            val productos = if (selectedTab == 0) {
                misProductos.filter { !it.estaVendido }
            } else {
                misProductos.filter { it.estaVendido }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = UATOrange)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(productos) { prod ->
                        MiProductoCard(
                            producto = prod, 
                            imageLoader = imageLoader,
                            onEdit = { onNavigateToEdit(prod.id) },
                            onNavigate = { onNavigateToProductDetail(prod.id) },
                            onToggleStatus = {
                                scope.launch {
                                    try {
                                        val request = com.uat.uatlife.network.models.StatusRequest(!prod.estaVendido)
                                        val resp = apiService.patchProductStatus(prod.id, request)
                                        if (resp.isSuccessful) {
                                            refreshProducts()
                                            android.widget.Toast.makeText(context, resp.body()?.mensaje ?: "Estado actualizado", android.widget.Toast.LENGTH_SHORT).show()
                                        } else {
                                            android.widget.Toast.makeText(context, "Error al actualizar", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "Error de red: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                    
                    if (productos.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                                Text("No hay productos en esta lista", color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiProductoCard(
    producto: Producto, 
    imageLoader: ImageLoader,
    onEdit: () -> Unit, 
    onNavigate: () -> Unit,
    onToggleStatus: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onNavigate() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF0F2F5)),
                contentAlignment = Alignment.Center
            ) {
                if (producto.urlFotoPrincipal != null) {
                    AsyncImage(
                        model = RetrofitClient.BASE_URL + producto.urlFotoPrincipal.removePrefix("/"),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        imageLoader = imageLoader
                    )
                } else {
                    Icon(Icons.Filled.Inventory, contentDescription = null, tint = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = producto.titulo,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = UATBlueDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("$${producto.precio}", fontWeight = FontWeight.Bold, color = UATOrange)
                    if (producto.horaInicio != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Outlined.Schedule, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                        Text(" ${producto.horaInicio}", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
            
            if (!producto.estaVendido) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = Color.Gray, modifier = Modifier.size(20.dp))
                    }
                    Button(
                        onClick = onToggleStatus,
                        colors = ButtonDefaults.buttonColors(containerColor = UATOrange),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Marcar Vendido", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                OutlinedButton(
                    onClick = onToggleStatus,
                    border = BorderStroke(1.dp, UATOrange),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Poner Disponible", fontSize = 11.sp, color = UATOrange, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
