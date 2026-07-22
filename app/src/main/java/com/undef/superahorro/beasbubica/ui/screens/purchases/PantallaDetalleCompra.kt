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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.undef.superahorro.R
import com.undef.superahorro.data.repository.RepositorioComprasSupabase
import com.undef.superahorro.ui.components.BarraSuperior
import com.undef.superahorro.ui.components.IndicadorCarga
import com.undef.superahorro.ui.theme.SuperAhorroTheme
import com.undef.superahorro.viewmodel.ViewModelCompras
import com.undef.superahorro.viewmodel.ViewModelMoneda
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale


@Composable
/**
 * Detalle completo de una compra: datos, foto del ticket y productos.
 * Permite editar, borrar y compartir la compra.
 */
fun PantallaDetalleCompra(
    compraId: String,
    viewModelMoneda: ViewModelMoneda,
    alVolverAtras: () -> Unit,
    alEditarCompra: (String) -> Unit = {}
) {
    val contexto        = LocalContext.current
    val viewModel       = remember { ViewModelCompras(contexto) }
    val estadoCompra    by viewModel.compraSeleccionada.collectAsState()
    val estadoProductos by viewModel.estadoProductos.collectAsState()
    val scope           = rememberCoroutineScope()

    var mostrarDialogoBorrar  by remember { mutableStateOf(false) }
    var borrando              by remember { mutableStateOf(false) }

    LaunchedEffect(compraId) {
        if (compraId.isBlank()) return@LaunchedEffect
        viewModel.cargarCompraPorId(compraId)
        viewModel.cargarProductosPorCompra(compraId)
    }

    val compra    = estadoCompra.datos
    val productos = estadoProductos.datos ?: emptyList()

    // Diálogo de confirmación para borrar
    if (mostrarDialogoBorrar && compra != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoBorrar = false },
            icon    = { Icon(Icons.Outlined.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
            title   = { Text(stringResource(R.string.purchase_delete_title)) },
            text    = { Text(stringResource(R.string.purchase_delete_confirm, compra.supermercado, compra.fecha)) },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        borrando = true
                        RepositorioComprasSupabase(contexto).eliminarCompra(compra)
                        mostrarDialogoBorrar = false
                        alVolverAtras()
                    }
                }) { Text(stringResource(R.string.action_erase), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoBorrar = false }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo        = stringResource(R.string.purchase_detail),
                mostrarVolver = true,
                alVolverAtras = alVolverAtras,
                acciones = {
                    // Botón editar
                    IconButton(onClick = { alEditarCompra(compraId) }) {
                        Icon(Icons.Outlined.Edit, stringResource(R.string.action_edit), tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    // Botón borrar
                    IconButton(onClick = { mostrarDialogoBorrar = true }, enabled = !borrando) {
                        Icon(Icons.Outlined.DeleteOutline, stringResource(R.string.action_erase), tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    // Botón compartir
                    IconButton(onClick = {
                        compra?.let {
                            val texto = contexto.getString(
                                R.string.purchase_share_text,
                                it.supermercado, it.fecha, it.hora, viewModelMoneda.convertir(it.total)
                            )
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, texto)
                            }
                            contexto.startActivity(Intent.createChooser(intent, contexto.getString(R.string.purchase_share_chooser)))
                        }
                    }) {
                        Icon(Icons.Outlined.Share, null, tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        }
    ) { padding ->
        if (estadoCompra.cargando) {
            IndicadorCarga(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            if (compra == null) return@LazyColumn

            // Encabezado con datos de la compra
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape    = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(compra.supermercado, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ChipInfo(Icons.Outlined.CalendarMonth, compra.fecha)
                            ChipInfo(Icons.Outlined.Schedule, compra.hora)
                        }
                        HorizontalDivider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.purchase_total_paid), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                viewModelMoneda.convertir(compra.total),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Foto del ticket — solo si existe
            item {
                if (!compra.imagenTicket.isNullOrBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                stringResource(R.string.purchase_ticket_photo),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            AsyncImage(
                                model = compra.imagenTicket,
                                contentDescription = stringResource(R.string.purchase_ticket_photo),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                    }
                }
            }

            // Título lista de productos
            item {
                Text(
                    stringResource(R.string.purchase_products_count, productos.size),
                    style    = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            // Productos con precios en moneda activa
            items(productos) { prod ->
                ListItem(
                    headlineContent   = { Text(prod.nombre, fontWeight = FontWeight.SemiBold) },
                    supportingContent = {
                        Column {
                            if (prod.codigo.isNotBlank()) Text(stringResource(R.string.product_code_prefix, prod.codigo), style = MaterialTheme.typography.labelSmall)
                            Text(stringResource(R.string.product_qty_price, prod.cantidad, viewModelMoneda.convertir(prod.precio)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    trailingContent = {
                        Text(
                            viewModelMoneda.convertir(prod.precioTotal),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            // Total de los productos (con desglose de descuento si hubo)
            item {
                val sumaProductos = productos.sumOf { it.precioTotal }
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(12.dp),
                     colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Si la compra tuvo descuento, mostramos Subtotal y Descuento
                        if (compra.descuento > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(stringResource(R.string.purchase_subtotal), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(viewModelMoneda.convertir(sumaProductos))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(stringResource(R.string.purchase_discount), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("− " + viewModelMoneda.convertir(compra.descuento), color = MaterialTheme.colorScheme.error)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                stringResource(if (compra.descuento > 0) R.string.purchase_total else R.string.purchase_total_calculated),
                                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
                            )
                            Text(
                                viewModelMoneda.convertir(if (compra.descuento > 0) compra.total else sumaProductos),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChipInfo(icono: androidx.compose.ui.graphics.vector.ImageVector, texto: String) {
    Row(
        modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icono, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
        Text(texto, style = MaterialTheme.typography.labelMedium)
    }
}

@Preview(showBackground = true)
@Composable
private fun Vista() = SuperAhorroTheme { Text("Preview - PantallaDetalleCompra") }
