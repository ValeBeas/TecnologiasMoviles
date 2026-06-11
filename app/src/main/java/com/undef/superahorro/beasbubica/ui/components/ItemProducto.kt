package com.undef.superahorro.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.undef.superahorro.domain.model.Producto
import java.text.NumberFormat
import java.util.Locale

/**
 * Fila de un producto para mostrar en el detalle de una compra.
 * Muestra el nombre, el código de barras, la cantidad y el precio unitario y total.
 */
@Composable
/**
 * Fila de un producto en el detalle de una compra.
 * Muestra nombre, código de barras, cantidad, precio unitario y total.
 */
fun ItemProducto(producto: Producto, alEliminar: (() -> Unit)? = null) {
    val formateador = NumberFormat.getNumberInstance(Locale("es", "AR"))
    HorizontalDivider()
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(producto.nombre, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            if (producto.descripcion.isNotBlank()) {
                Text(producto.descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("Cód: ${producto.codigo}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("x${producto.cantidad}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("$ ${formateador.format(producto.precio)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            if (producto.cantidad > 1) {
                Text("Total: $ ${formateador.format(producto.precioTotal)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (alEliminar != null) {
            IconButton(onClick = alEliminar) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
