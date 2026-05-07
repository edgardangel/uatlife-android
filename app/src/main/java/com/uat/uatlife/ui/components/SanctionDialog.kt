package com.uat.uatlife.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uat.uatlife.ui.theme.UATBlueDark
import com.uat.uatlife.ui.theme.UATOrange
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SanctionDialog(
    usuarioNombre: String,
    onDismiss: () -> Unit,
    onConfirm: (tipoSancion: String, motivo: String, duracionHoras: Int?) -> Unit
) {
    var tipoSancion by remember { mutableStateOf("suspension_temporal") } // 'ban_permanente' o 'suspension_temporal'
    var motivo by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Estado para la fecha (si es temporal)
    val calendar = remember { Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) } }
    var selectedDate by remember { mutableStateOf(calendar.time) }
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth)
            selectedDate = cal.time
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Gavel, null, tint = Color(0xFFEF4444), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Aplicar Sanción", fontWeight = FontWeight.Bold, color = UATBlueDark)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Usuario: $usuarioNombre",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Selector de tipo
                Text("Tipo de sanción:", fontSize = 12.sp, color = Color.Gray)
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = tipoSancion == "suspension_temporal",
                        onClick = { tipoSancion = "suspension_temporal" },
                        colors = RadioButtonDefaults.colors(selectedColor = UATOrange)
                    )
                    Text("Suspensión Temporal", fontSize = 14.sp, modifier = Modifier.clickable { tipoSancion = "suspension_temporal" })
                }
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = tipoSancion == "ban_permanente",
                        onClick = { tipoSancion = "ban_permanente" },
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFEF4444))
                    )
                    Text("Baneo Permanente", fontSize = 14.sp, modifier = Modifier.clickable { tipoSancion = "ban_permanente" })
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Selector de fecha (solo si es temporal)
                if (tipoSancion == "suspension_temporal") {
                    Text("Suspender hasta:", fontSize = 12.sp, color = Color.Gray)
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { datePickerDialog.show() },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CalendarToday, null, tint = UATBlueDark, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(sdf.format(selectedDate), fontSize = 14.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Motivo
                Text("Motivo de la sanción:", fontSize = 12.sp, color = Color.Gray)
                OutlinedTextField(
                    value = motivo,
                    onValueChange = { motivo = it },
                    placeholder = { Text("Ej: Incumplimiento de normas de comunidad...", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = UATBlueDark)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val duracionHoras = if (tipoSancion == "suspension_temporal") {
                        val diff = selectedDate.time - System.currentTimeMillis()
                        (diff / 3600000).toInt().coerceAtLeast(1)
                    } else null
                    onConfirm(tipoSancion, motivo, duracionHoras)
                },
                enabled = motivo.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (tipoSancion == "ban_permanente") Color(0xFFEF4444) else UATOrange
                )
            ) {
                Text("Sancionar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.Gray)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}
