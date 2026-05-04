package com.uat.uatlife.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import com.uat.uatlife.network.RetrofitClient
import com.uat.uatlife.network.models.Producto
import com.uat.uatlife.ui.theme.UATBlueDark
import com.uat.uatlife.ui.theme.UATOrange
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productoId: Int,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = remember { RetrofitClient.getApiService(context) }
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .okHttpClient { RetrofitClient.getUnsafeOkHttpClient() }
            .build()
    }

    var producto by remember { mutableStateOf<Producto?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(productoId) {
        if (productoId <= 0) {
            Toast.makeText(context, "ID de producto inválido", Toast.LENGTH_SHORT).show()
            onBack()
            return@LaunchedEffect
        }
        try {
            val response = apiService.getProductoById(productoId)
            if (response.isSuccessful) {
                producto = response.body()
            } else {
                Toast.makeText(context, "No se pudo cargar el producto", Toast.LENGTH_SHORT).show()
                onBack()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error de red", Toast.LENGTH_SHORT).show()
            onBack()
        } finally {
            isLoading = false
        }
    }

    Scaffold() { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = UATOrange)
            }
            return@Scaffold
        }

        val prod = producto
        if (prod == null) return@Scaffold

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
                    .background(Color(0xFFE5E7EB))
            ) {
                if (!prod.urlFotoPrincipal.isNullOrEmpty()) {
                    AsyncImage(
                        model = RetrofitClient.BASE_URL + prod.urlFotoPrincipal!!.removePrefix("/"),
                        contentDescription = prod.titulo,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        imageLoader = imageLoader
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.ShoppingBag,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(80.dp).align(Alignment.Center)
                    )
                }

                // Condición Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha=0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        prod.condicion.replaceFirstChar { it.uppercase() },
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Título y Precio
                Text(
                    text = prod.titulo,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$${String.format("%.0f", prod.precio)}",
                    fontSize = 18.sp,
                    color = UATBlueDark,
                    fontWeight = FontWeight.Black
                )

                if (prod.horaInicio != null && prod.horaFin != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0xFFFFF7ED), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Filled.Schedule, contentDescription = null, tint = UATOrange, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Disponible hoy: ${prod.horaInicio} - ${prod.horaFin}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF9A3412)
                        )
                    }
                }
                
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
                    text = prod.descripcion ?: "Sin descripción proporcionada.",
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
                        Icon(Icons.Filled.Send, contentDescription = "Enviar", tint = UATOrange, modifier = Modifier.size(20.dp))
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
                if (!prod.vendedorFoto.isNullOrEmpty()) {
                    AsyncImage(
                        model = RetrofitClient.BASE_URL + prod.vendedorFoto!!.removePrefix("/"),
                        contentDescription = prod.vendedorNombre,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        imageLoader = imageLoader
                    )
                        } else {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Info Vendedor
                    Column(modifier = Modifier.weight(1f)) {
                        Text(prod.vendedorNombre, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Shield, contentDescription = null, tint = UATOrange, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("${prod.vendedorConfianza}% confianza", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}


