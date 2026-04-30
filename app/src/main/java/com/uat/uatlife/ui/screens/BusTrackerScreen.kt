package com.uat.uatlife.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import com.uat.uatlife.R
import com.uat.uatlife.ui.theme.UATBlueDark
import com.uat.uatlife.ui.theme.UATOrange

// Coordenadas reales del Centro Universitario Sur UAT (Tampico)
private val CU_TAMPICO_CENTER = LatLng(22.2760, -97.8630)

// Límites geográficos exactos que encierran el campus
private val UAT_BOUNDS = LatLngBounds(
    LatLng(22.2710, -97.8680), // Suroeste
    LatLng(22.2800, -97.8580)  // Noreste
)

private data class StopLocation(val name: String, val location: LatLng)

private val facultiesPoints = listOf(
    StopLocation("Facultad de Ingeniería (FIT)", LatLng(22.276880, -97.865295)),
    StopLocation("Facultad de Comercio y Administración (FCAT)", LatLng(22.275232, -97.862442)),
    StopLocation("Facultad de Medicina", LatLng(22.277637, -97.861114)),
    StopLocation("Facultad de Enfermería", LatLng(22.278463, -97.861186)),
    StopLocation("Facultad de Odontología", LatLng(22.276393, -97.859397)),
    StopLocation("Facultad de Derecho y Ciencias Sociales (FADYCS)", LatLng(22.275532, -97.865486)),
    StopLocation("Facultad de Arquitectura, Diseño y Urbanismo (FADU)", LatLng(22.275085, -97.863973)),
    StopLocation("Facultad de Música y Artes", LatLng(22.278664, -97.863501))
)

// Posición inicial del Lobo Bús (simulada)
private val BUS_INITIAL_POS = LatLng(22.2750, -97.8635)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusTrackerScreen() {
    val context = LocalContext.current

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(CU_TAMPICO_CENTER, 15.5f)
    }
    
    var uiSettings by remember { 
        mutableStateOf(
            MapUiSettings(
                zoomControlsEnabled = false, 
                myLocationButtonEnabled = false,
                scrollGesturesEnabled = false,
                zoomGesturesEnabled = true, // Permitir pellizco o doble toque para acercar
                tiltGesturesEnabled = false,
                rotationGesturesEnabled = false
            )
        ) 
    }
    var mapProperties by remember { 
        mutableStateOf(
            MapProperties(
                isMyLocationEnabled = false,
                minZoomPreference = 14f, // Límite para no alejarse mucho de la UAT
                latLngBoundsForCameraTarget = UAT_BOUNDS // Encerrar la cámara para que no huya a otra ciudad
            )
        ) 
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Objeto Google Map envuelto en Jetpack Compose
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = uiSettings
        ) {
            // Marcadores de Paradas (Facultades)
            facultiesPoints.forEach { stop ->
                Marker(
                    state = MarkerState(position = stop.location),
                    title = stop.name,
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )
            }

            val iconMarker = remember { 
                try {
                    bitmapDescriptorFromVector(context, R.drawable.ic_bus_marker)
                } catch (e: Exception) {
                    null
                }
            }

            // Marcador del Autobús (Lobo Bús) animado/dinámico
            Marker(
                state = MarkerState(position = BUS_INITIAL_POS),
                title = "Lobo Bús - Unidad 1",
                snippet = "En ruta",
                icon = iconMarker ?: BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
            )
        }

        // Overlay UI sobre el mapa (Header)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .windowInsetsPadding(WindowInsets.statusBars)
                .align(Alignment.TopCenter),
            color = Color.White.copy(alpha = 0.95f),
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(UATOrange, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.DirectionsBus, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Seguimiento Bus UAT", fontSize = 18.sp, fontWeight = FontWeight.Black, color = UATBlueDark)
                    Text("Campus Tampico - Unidad 1 Activa", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
        
        // Reportar Sin Servicio (FAB Bottom Right)
        ExtendedFloatingActionButton(
            onClick = { /* TODO: Sistema de reportes de confianza */ },
            icon = { Icon(Icons.Filled.WarningAmber, "Reporte") },
            text = { Text("Sin Servicio", fontWeight = FontWeight.Bold) },
            containerColor = Color.White,
            contentColor = Color(0xFFD97706), // Naranja Oscuro preventivo
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 32.dp, end = 16.dp)
                .windowInsetsPadding(WindowInsets.navigationBars)
        )
    }
}

// Convertidor de Vectores XML Nativos de Android a Icono compatible con Google Maps SDK
private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): com.google.android.gms.maps.model.BitmapDescriptor? {
    return ContextCompat.getDrawable(context, vectorResId)?.run {
        setBounds(0, 0, intrinsicWidth, intrinsicHeight)
        val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
        draw(Canvas(bitmap))
        BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}
