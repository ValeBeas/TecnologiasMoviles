package com.undef.superahorro.ui.screens.purchases

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.undef.superahorro.R
import com.undef.superahorro.domain.model.ProductoEnCompra
import java.text.NumberFormat

/**
 * Componentes que comparten PantallaNuevaCompra y PantallaEditarCompra.
 * Se extrajeron acá para achicar las pantallas y no duplicar código.
 */

/**
 * Dropdown de supermercados + campo libre cuando se elige "Otro".
 * El estado (seleccionado / otro) vive en la pantalla; acá solo se dibuja.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorSupermercado(
    seleccionado: String,
    otroMercado: String,
    alSeleccionar: (String) -> Unit,
    alCambiarOtro: (String) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ExposedDropdownMenuBox(expanded = expandido, onExpandedChange = { expandido = !expandido }) {
            OutlinedTextField(
                value = seleccionado, onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.purchase_supermarket)) },
                leadingIcon = { Icon(Icons.Outlined.Store, null) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandido) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = MaterialTheme.shapes.medium
            )
            ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
                SUPERMERCADOS.forEach { s ->
                    DropdownMenuItem(
                        text = { Text(s) },
                        onClick = {
                            alSeleccionar(s)
                            expandido = false
                            if (s != "Otro") alCambiarOtro("")
                        }
                    )
                }
            }
        }

        if (seleccionado == "Otro") {
            OutlinedTextField(
                value = otroMercado,
                onValueChange = alCambiarOtro,
                label = { Text(stringResource(R.string.purchase_other_market)) },
                leadingIcon = { Icon(Icons.Outlined.Edit, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )
        }
    }
}

/**
 * Sección de totales: campo de descuento editable, subtotal (si hay descuento)
 * y el total final de solo lectura (subtotal − descuento).
 */
@Composable
fun SeccionTotales(
    subtotal: Double,
    descuento: Double,
    descuentoTexto: String,
    alCambiarDescuento: (String) -> Unit,
    totalFinal: Double,
    formateador: NumberFormat,
    mostrarHintSuma: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Descuento (opcional): se puede cargar a mano o lo completa la IA
        OutlinedTextField(
            value = descuentoTexto,
            onValueChange = alCambiarDescuento,
            label = { Text(stringResource(R.string.purchase_discount_optional)) },
            leadingIcon = { Icon(Icons.Outlined.LocalOffer, null) },
            prefix = { Text(stringResource(R.string.currency_prefix)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )

        // Subtotal (solo se muestra si hay descuento, para ver la diferencia)
        if (descuento > 0) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.purchase_subtotal), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(R.string.amount_format, formateador.format(subtotal)))
            }
        }

        // Total de solo lectura (ya con el descuento restado)
        OutlinedTextField(
            value = if (totalFinal > 0) stringResource(R.string.amount_format, formateador.format(totalFinal)) else "",
            onValueChange = {}, readOnly = true,
            label = { Text(stringResource(R.string.purchase_total)) },
            leadingIcon = { Icon(Icons.Outlined.AttachMoney, null) },
            placeholder = { Text(stringResource(R.string.purchase_total_auto)) },
            supportingText = if (mostrarHintSuma) {
                { Text(stringResource(R.string.purchase_total_sum_hint)) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )
    }
}

/**
 * Encabezado + lista de productos del formulario de compra (alta y edición).
 * Muestra el contador, el botón de agregar y las filas editables (o un aviso si está vacía).
 */
@Composable
fun SeccionProductos(
    productos: List<ProductoEnCompra>,
    formateador: NumberFormat,
    etiquetaAgregar: String,
    textoVacio: String,
    alAgregar: () -> Unit,
    alAumentar: (Int) -> Unit,
    alDisminuir: (Int) -> Unit,
    alEliminar: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.purchase_products_count, productos.size),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary)
            FilledTonalButton(onClick = alAgregar) {
                Icon(Icons.Outlined.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(etiquetaAgregar)
            }
        }

        if (productos.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                    Text(textoVacio, style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                productos.forEachIndexed { indice, prod ->
                    FilaProductoEditable(
                        producto    = prod,
                        formateador = formateador,
                        alAumentar  = { alAumentar(indice) },
                        alDisminuir = { alDisminuir(indice) },
                        alEliminar  = { alEliminar(indice) }
                    )
                    if (indice < productos.lastIndex) HorizontalDivider()
                }
            }
        }
    }
}

/**
 * Fila de un producto dentro del formulario de compra (alta y edición).
 * Muestra nombre, precio unitario y código, con botones para cambiar la
 * cantidad y eliminar. La usan PantallaNuevaCompra y PantallaEditarCompra.
 */
@Composable
fun FilaProductoEditable(
    producto: ProductoEnCompra,
    formateador: NumberFormat,
    alAumentar: () -> Unit,
    alDisminuir: () -> Unit,
    alEliminar: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(producto.nombre, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.product_price_each, formateador.format(producto.costo)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (producto.codigoBarras.isNotBlank())
                Text(stringResource(R.string.product_code_prefix, producto.codigoBarras),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            FilledIconButton(
                onClick = alDisminuir,
                enabled = producto.cantidad > 1,
                modifier = Modifier.size(32.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(Icons.Outlined.Remove, null, modifier = Modifier.size(16.dp),
                    tint = if (producto.cantidad > 1) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("${producto.cantidad}", style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.widthIn(min = 24.dp),
                textAlign = TextAlign.Center)
            FilledIconButton(
                onClick = alAumentar,
                modifier = Modifier.size(32.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Outlined.Add, null, modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onPrimary)
            }
            Text(stringResource(R.string.amount_format, formateador.format(producto.subtotal)),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp).widthIn(min = 72.dp))
            IconButton(onClick = alEliminar, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Outlined.DeleteOutline, null, modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
