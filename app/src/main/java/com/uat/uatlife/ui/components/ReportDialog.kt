package com.uat.uatlife.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uat.uatlife.ui.theme.UATOrange

@Composable
fun ReportDialog(
    onDismiss: () -> Unit,
    onConfirm: (motivo: String, descripcion: String) -> Unit
) {
    val motivos = listOf("spam", "fraude", "contenido_inapropiado", "acoso", "otro")
    val motivosLabels = listOf("Spam", "Fraude", "Contenido Inapropiado", "Acoso", "Otro")
    
    var selectedMotivo by remember { mutableStateOf(motivos[0]) }
    var descripcion by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reportar contenido", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("¿Por qué quieres reportar esta publicación?", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(Modifier.selectableGroup()) {
                    motivos.forEachIndexed { index, motivo ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .selectable(
                                    selected = (selectedMotivo == motivo),
                                    onClick = { selectedMotivo = motivo },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 0.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (selectedMotivo == motivo),
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = UATOrange)
                            )
                            Text(
                                text = motivosLabels[index],
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 8.dp),
                                fontSize = 15.sp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Detalles adicionales (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedMotivo, descripcion) },
                colors = ButtonDefaults.buttonColors(containerColor = UATOrange)
            ) {
                Text("Enviar Reporte", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}
