package com.undef.superahorro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.undef.superahorro.domain.model.Compra
import com.undef.superahorro.ui.theme.*
import com.undef.superahorro.viewmodel.ViewModelMoneda

// Devuelve el color que le corresponde a cada supermercado.
/**
 * Devuelve el color de la franja lateral según el supermercado.
 * Se usa en las tarjetas del historial y en las estadísticas.
 */
fun colorSupermercado(nombre: String): Color = when (nombre.lowercase()) {
    "coto"         -> ColorCoto
    "carrefour"    -> ColorCarrefour
    "día", "dia"   -> ColorDia
    "jumbo"        -> ColorJumbo
    "walmart"      -> ColorWalmart
    else           -> ColorDefault
}

/**
 * Tarjeta que muestra el resumen de una compra en la lista.
 * La franja de color a la izquierda identifica el supermercado.
 * Muestra el precio en la moneda activa usando ViewModelMoneda.
 *
 * @param alHacerClick acción que se ejecuta cuando el usuario toca la tarjeta
 */
@Composable
fun TarjetaCompra(
    compra: Compra,
    viewModelMoneda: ViewModelMoneda,
    alHacerClick: () -> Unit
) {
    val colorAcento = colorSupermercado(compra.supermercado)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = alHacerClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                    .background(colorAcento)
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colorAcento.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.ShoppingBag, null, tint = colorAcento, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(compra.supermercado, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("${compra.fecha} · ${compra.hora}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${compra.cantidadProductos} productos", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(
                    viewModelMoneda.convertir(compra.total),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
