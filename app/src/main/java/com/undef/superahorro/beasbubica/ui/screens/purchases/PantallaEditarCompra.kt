package com.undef.superahorro.ui.screens.purchases

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.undef.superahorro.R
import com.undef.superahorro.data.repository.RepositorioComprasSupabase
import com.undef.superahorro.domain.model.ProductoEnCompra
import com.undef.superahorro.domain.model.calcularTotal
import com.undef.superahorro.ui.components.BarraSuperior
import com.undef.superahorro.ui.components.CampoFecha
import com.undef.superahorro.ui.components.CampoHora
import com.undef.superahorro.ui.components.fechaValida
import com.undef.superahorro.ui.components.horaValida
import com.undef.superahorro.ui.components.IndicadorCarga
import com.undef.superahorro.ui.theme.SuperAhorroTheme
import com.undef.superahorro.viewmodel.ViewModelCompras
import com.undef.superahorro.viewmodel.ViewModelNuevaCompra
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Pantalla para editar una compra existente.
 * Precarga los datos actuales y guarda los cambios en Supabase y Room.
 */
fun PantallaEditarCompra(
    compraId: String,
    alVolverAtras: () -> Unit
) {
    val contexto    = LocalContext.current
    val scope       = rememberCoroutineScope()
    val viewModel   = remember { ViewModelCompras(contexto) }
    val vmProductos = remember { ViewModelNuevaCompra() }
    val formateador = NumberFormat.getNumberInstance(Locale("es", "AR"))

    val estadoCompra    by viewModel.compraSeleccionada.collectAsState()
    val estadoProductos by viewModel.estadoProductos.collectAsState()
    val compra          = estadoCompra.datos
    val productosVM     by vmProductos.productos.collectAsState()

    var supermercado  by remember { mutableStateOf("") }
    var otroMercado   by remember { mutableStateOf("") }
    var fecha         by remember { mutableStateOf("") }
    var hora          by remember { mutableStateOf("") }
    var expandido     by remember { mutableStateOf(false) }
    var guardando     by remember { mutableStateOf(false) }
    var mensajeError  by remember { mutableStateOf("") }
    var productosInicializados by remember { mutableStateOf(false) }
    var mostrarDialogoProducto by remember { mutableStateOf(false) }
    var descuentoActual by remember { mutableStateOf(0.0) }

    val supermercados = listOf("Coto", "Carrefour", "Día", "Jumbo", "Walmart", "La Anónima", "Vea", "Otro")
    val eligioOtro    = supermercado == "Otro"
    val nombreSuper   = if (eligioOtro) otroMercado else supermercado
    val total         = productosVM.calcularTotal()
    val totalConDescuento = (total - descuentoActual).coerceAtLeast(0.0)

    // Cargar datos de la compra al entrar
    LaunchedEffect(compraId) {
        viewModel.cargarCompraPorId(compraId)
        viewModel.cargarProductosPorCompra(compraId)
    }

    // Precargar formulario cuando llegan los datos
    LaunchedEffect(compra) {
        compra?.let {
            fecha = it.fecha
            hora  = it.hora
            descuentoActual = it.descuento
            val estaEnLista = supermercados.contains(it.supermercado)
            supermercado = if (estaEnLista) it.supermercado else "Otro"
            otroMercado  = if (!estaEnLista) it.supermercado else ""
        }
    }

    // Precargar los productos del ViewModel de edición (una sola vez)
    LaunchedEffect(estadoProductos.datos) {
        if (!productosInicializados && estadoProductos.datos != null) {
            estadoProductos.datos!!.forEach { prod ->
                vmProductos.agregarProducto(
                    ProductoEnCompra(
                        nombre       = prod.nombre,
                        cantidad     = prod.cantidad,
                        costo        = prod.precio,
                        codigoBarras = prod.codigo
                    )
                )
            }
            productosInicializados = true
        }
    }

    // Diálogo para agregar un producto nuevo a la compra en edición
    if (mostrarDialogoProducto) {
        DialogoAgregarProducto(
            onAgregar = { nuevo ->
                vmProductos.agregarProducto(nuevo)
                mostrarDialogoProducto = false
            },
            onCancelar = { mostrarDialogoProducto = false }
        )
    }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(R.string.purchase_edit),
                mostrarVolver = true,
                alVolverAtras = { vmProductos.limpiar(); alVolverAtras() }
            )
        }
    ) { padding ->
        if (estadoCompra.cargando) {
            IndicadorCarga(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(stringResource(R.string.purchase_data_section),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary)

            // Dropdown supermercado
            ExposedDropdownMenuBox(expanded = expandido, onExpandedChange = { expandido = !expandido }) {
                OutlinedTextField(
                    value = supermercado, onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.purchase_supermarket)) },
                    leadingIcon = { Icon(Icons.Outlined.Store, null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandido) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = MaterialTheme.shapes.medium
                )
                ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
                    supermercados.forEach { s ->
                        DropdownMenuItem(
                            text = { Text(s) },
                            onClick = {
                                supermercado = s; expandido = false
                                if (s != "Otro") otroMercado = ""
                            }
                        )
                    }
                }
            }

            if (eligioOtro) {
                OutlinedTextField(
                    value = otroMercado, onValueChange = { otroMercado = it },
                    label = { Text(stringResource(R.string.purchase_other_market)) },
                    leadingIcon = { Icon(Icons.Outlined.Edit, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoFecha(
                    valor     = fecha,
                    alCambiar = { fecha = it },
                    modifier  = Modifier.weight(1f)
                )
                CampoHora(
                    valor     = hora,
                    alCambiar = { hora = it },
                    modifier  = Modifier.weight(1f)
                )
            }

            HorizontalDivider()

            // Sección de productos editables
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.purchase_products_count, productosVM.size),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary)
                FilledTonalButton(onClick = { mostrarDialogoProducto = true }) {
                    Icon(Icons.Outlined.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.action_add))
                }
            }

            if (productosVM.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(20.dp),
                        contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.purchase_no_products), style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                Card(modifier = Modifier.fillMaxWidth()) {
                    productosVM.forEachIndexed { indice, prod ->
                        FilaProductoEdicion(
                            producto    = prod,
                            formateador = formateador,
                            alAumentar  = { vmProductos.aumentarCantidad(indice) },
                            alDisminuir = { vmProductos.disminuirCantidad(indice) },
                            alEliminar  = { vmProductos.eliminarProducto(indice) }
                        )
                        if (indice < productosVM.lastIndex) HorizontalDivider()
                    }
                }
            }

            // Subtotal + descuento (si la compra tenía descuento)
            if (descuentoActual > 0) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.purchase_subtotal), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stringResource(R.string.amount_format, formateador.format(total)))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.purchase_discount), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stringResource(R.string.discount_amount_format, formateador.format(descuentoActual)), color = MaterialTheme.colorScheme.error)
                }
            }

            // Total calculado (ya con el descuento restado)
            OutlinedTextField(
                value = if (totalConDescuento > 0) stringResource(R.string.amount_format, formateador.format(totalConDescuento)) else "",
                onValueChange = {}, readOnly = true,
                label = { Text(stringResource(R.string.purchase_total)) },
                leadingIcon = { Icon(Icons.Outlined.AttachMoney, null) },
                placeholder = { Text(stringResource(R.string.purchase_total_auto)) },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )

            if (mensajeError.isNotBlank()) {
                Text(mensajeError, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    scope.launch {
                        guardando = true; mensajeError = ""
                        runCatching {
                            val repo = RepositorioComprasSupabase(contexto)
                            val compraActualizada = compra!!.copy(
                                fecha        = fecha,
                                hora         = hora,
                                supermercado = nombreSuper,
                                total        = totalConDescuento,
                                descuento    = descuentoActual,
                                cantidadProductos = productosVM.size
                            )
                            // Guarda la cabecera Y reemplaza los productos (persiste los cambios)
                            repo.editarCompraConProductos(compraActualizada, productosVM)
                            vmProductos.limpiar()
                            alVolverAtras()
                        }.onFailure { mensajeError = contexto.getString(R.string.error_with_detail, it.message ?: "") }
                        guardando = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = MaterialTheme.shapes.extraLarge,
                enabled  = supermercado.isNotBlank() &&
                    (!eligioOtro || otroMercado.isNotBlank()) &&
                    fechaValida(fecha, java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)) &&
                    horaValida(hora) && !guardando
            ) {
                if (guardando) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Icon(Icons.Outlined.Save, null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.profile_save))
            }
        }
    }
}

@Composable
private fun FilaProductoEdicion(
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
        }
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            FilledIconButton(
                onClick = alDisminuir, enabled = producto.cantidad > 1,
                modifier = Modifier.size(32.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                onClick = alAumentar, modifier = Modifier.size(32.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Outlined.Add, null, modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onPrimary)
            }
            Text(stringResource(R.string.amount_format, formateador.format(producto.subtotal)),
                style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp).widthIn(min = 72.dp))
            IconButton(onClick = alEliminar, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Outlined.DeleteOutline, null, modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

/**
 * Diálogo para cargar un producto nuevo a la compra que se está editando.
 * Devuelve un ProductoEnCompra al confirmar; no navega a otra pantalla.
 */
@Composable
private fun DialogoAgregarProducto(
    onAgregar: (ProductoEnCompra) -> Unit,
    onCancelar: () -> Unit
) {
    var nombre   by remember { mutableStateOf("") }
    var cantidad by remember { mutableIntStateOf(1) }
    var costo    by remember { mutableStateOf("") }
    var codigo   by remember { mutableStateOf("") }
    val costoOk  = costo.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(stringResource(R.string.product_add)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nombre, onValueChange = { nombre = it },
                    label = { Text(stringResource(R.string.product_name) + " *") },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.product_quantity), style = MaterialTheme.typography.bodyLarge)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FilledIconButton(
                            onClick = { if (cantidad > 1) cantidad-- },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) { Icon(Icons.Outlined.Remove, null, tint = MaterialTheme.colorScheme.primary) }
                        Text("$cantidad", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.widthIn(min = 32.dp), textAlign = TextAlign.Center)
                        FilledIconButton(
                            onClick = { cantidad++ },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) { Icon(Icons.Outlined.Add, null, tint = MaterialTheme.colorScheme.onPrimary) }
                    }
                }
                OutlinedTextField(
                    value = costo, onValueChange = { costo = it },
                    label = { Text(stringResource(R.string.product_unit_cost) + " *") },
                    prefix = { Text(stringResource(R.string.currency_prefix)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
                OutlinedTextField(
                    value = codigo, onValueChange = { codigo = it },
                    label = { Text(stringResource(R.string.product_code_optional)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAgregar(ProductoEnCompra(nombre.trim(), cantidad, costoOk, codigo.trim())) },
                enabled = nombre.isNotBlank() && costoOk > 0
            ) { Text(stringResource(R.string.action_add)) }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text(stringResource(R.string.action_cancel)) } }
    )
}

@Preview(showBackground = true)
@Composable
private fun Vista() = SuperAhorroTheme { Text("Preview - PantallaEditarCompra") }
