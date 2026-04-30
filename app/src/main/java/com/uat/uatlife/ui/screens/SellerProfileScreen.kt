package com.uat.uatlife.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uat.uatlife.ui.theme.UATBlueDark
import com.uat.uatlife.ui.theme.UATOrange

private data class MiProducto(
    val id: Int,
    val nombre: String,
    val precio: String,
    val fecha: String,
    val status: String // "Vendido" o "En Venta"
)

private val misProductosMock = listOf(
    MiProducto(1, "Libreta Universitaria UAT", "$120", "Hoy", "En Venta"),
    MiProducto(2, "Cargador Tipo C Fast Charge", "$150", "Ayer", "En Venta"),
    MiProducto(3, "Formateo de Laptops y PC", "$250", "Hace 3 días", "En Venta"),
    MiProducto(4, "Calculadora Científica Casio", "$300", "Hace 1 semana", "Vendido"),
    MiProducto(5, "Playera de Tarde UAT M", "$200", "Hace 2 semanas", "Vendido")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerProfileScreen(
    onNavigateToCreateProduct: () -> Unit,
    onNavigateToEdit: (String, String) -> Unit,
    onNavigateToProductDetail: (String) -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("En Venta", "Vendidos")

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
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE5E7EB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Store, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(30.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Edgar Prueba", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = UATBlueDark)
                        Text("Vendedor Activo • 4.9 ⭐", fontSize = 13.sp, color = Color.Gray)
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
            val filterStatus = if (selectedTab == 0) "En Venta" else "Vendido"
            val productos = misProductosMock.filter { it.status == filterStatus }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(productos) { prod ->
                    MiProductoCard(
                        producto = prod, 
                        onEdit = { onNavigateToEdit(prod.nombre, prod.precio) },
                        onNavigate = { onNavigateToProductDetail(prod.nombre) }
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

@Composable
private fun MiProductoCard(producto: MiProducto, onEdit: () -> Unit, onNavigate: () -> Unit) {
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
                Icon(Icons.Filled.Inventory, contentDescription = null, tint = Color.Gray)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = producto.nombre,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = UATBlueDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(producto.precio, fontWeight = FontWeight.Bold, color = UATOrange)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("• Publicado: ${producto.fecha}", fontSize = 11.sp, color = Color.Gray)
                }
            }
            if (producto.status == "En Venta") {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = Color.Gray)
                }
            } else {
                TextButton(onClick = { /* TODO: Backend - Marcar disponible */ }) {
                    Text("Poner Disponible", fontSize = 12.sp, color = UATOrange, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
