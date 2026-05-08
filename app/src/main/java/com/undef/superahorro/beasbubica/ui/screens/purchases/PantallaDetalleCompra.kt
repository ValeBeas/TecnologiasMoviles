package com.undef.superahorro.ui.screens.purchases

import android.content.Intent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.undef.superahorro.data.repository.RepositorioComprasImpl
import com.undef.superahorro.ui.components.*
import com.undef.superahorro.ui.theme.SuperAhorroTheme
import com.undef.superahorro.viewmodel.ViewModelCompras
import java.text.NumberFormat
import java.util.Locale

/**
 * Pantalla con el detalle completo de una compra: fecha, supermercado, foto del ticket,
 * lista de productos y el total. El botón de compartir abre WhatsApp, Gmail o lo que tenga el usuario.
 */
@Composable
fun PantallaDetalleCompra(
    compraId: Int,
    alVolverAtras: () -> Unit
) {
    val viewModel = remember { ViewModelCompras(RepositorioComprasImpl()) }
    val estadoCompra   by viewModel.compraSeleccionada.collectAsState()
    val estadoProductos by viewModel.estadoProductos.collectAsState()
    val contexto = LocalContext.current
    val formateador = NumberFormat.getNumberInstance(Locale("es", "AR"))

    // Carga los datos al entrar a la pantalla
    LaunchedEffect(compraId) {
        viewModel.cargarCompraPorId(compraId)
        viewModel.cargarProductosPorCompra(compraId)
    }

    val compra    = estadoCompra.datos
    val productos = estadoProductos.datos ?: emptyList()

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = compra?.supermercado ?: "Detalle de compra",
                mostrarVolver = true,
                alVolverAtras = alVolverAtras,
                acciones = {
                    // --- Intent implícito: compartir resumen de la compra ---
                    IconButton(onClick = {
                        compra?.let {
                            val texto = "Compra en ${it.supermercado}\nFecha: ${it.fecha} ${it.hora}\nTotal: $ ${formateador.format(it.total)}\nProductos: ${productos.size}"
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, texto)
                            }
                            contexto.startActivity(Intent.createChooser(intent, "Compartir compra"))
                        }
                    }) { Icon(Icons.Outlined.Share, null, tint = MaterialTheme.colorScheme.onPrimary) }
                    IconButton(onClick = {}) { Icon(Icons.Outlined.Edit, null, tint = MaterialTheme.colorScheme.onPrimary) }
                }
            )
        }
    ) { padding ->
        if (compra == null) { IndicadorCarga(modifier = Modifier.padding(padding)); return@Scaffold }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 80.dp)) {

            // --- Tarjeta resumen de la compra ---
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            ChipInfo(Icons.Outlined.CalendarMonth, compra.fecha)
                            ChipInfo(Icons.Outlined.Schedule, compra.hora)
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Total abonado", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$ ${formateador.format(compra.total)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Productos", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${productos.size}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }

            // --- Placeholder foto del ticket ---
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.Receipt, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(onClick = {}) {
                                Icon(Icons.Outlined.CameraAlt, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Agregar foto del ticket")
                            }
                        }
                    }
                }
            }

            // --- Lista de productos ---
            item {
                Text("Productos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }
            items(productos) { producto ->
                ItemProducto(producto = producto)
            }

            // --- Total calculado de los productos ---
            item {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total calculado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "$ ${formateador.format(productos.sumOf { it.precioTotal })}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/** Chip pequeño para mostrar la fecha o la hora con un ícono al lado. */
@Composable
private fun ChipInfo(icono: androidx.compose.ui.graphics.vector.ImageVector, texto: String) {
    Row(
        modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(6.dp))
        Text(texto, style = MaterialTheme.typography.bodySmall)
    }
}

@Preview
@Composable
private fun Vista() = SuperAhorroTheme { PantallaDetalleCompra(1, {}) }
