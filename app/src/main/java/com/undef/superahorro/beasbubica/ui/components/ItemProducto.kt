package com.undef.superahorro.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.undef.superahorro.R
import com.undef.superahorro.domain.model.Producto
import java.text.NumberFormat
import java.util.Locale


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
            Text(stringResource(R.string.product_code_prefix, producto.codigo), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(stringResource(R.string.product_qty_short, producto.cantidad), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(stringResource(R.string.amount_format, formateador.format(producto.precio)), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            if (producto.cantidad > 1) {
                Text(stringResource(R.string.product_total_short, formateador.format(producto.precioTotal)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (alEliminar != null) {
            IconButton(onClick = alEliminar) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = stringResource(R.string.action_delete), tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
