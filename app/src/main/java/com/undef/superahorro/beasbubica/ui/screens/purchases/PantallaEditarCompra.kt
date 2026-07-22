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
    var guardando     by remember { mutableStateOf(false) }
    var mensajeError  by remember { mutableStateOf("") }
    var productosInicializados by remember { mutableStateOf(false) }
    var mostrarDialogoProducto by remember { mutableStateOf(false) }
    var descuentoActual by remember { mutableStateOf(0.0) }
    var descuentoTexto by remember { mutableStateOf("") }

    val supermercados = SUPERMERCADOS
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
            descuentoTexto = if (it.descuento > 0) {
                if (it.descuento % 1.0 == 0.0) it.descuento.toInt().toString() else it.descuento.toString()
            } else ""
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

            // Dropdown de supermercado + campo libre (componente compartido)
            SelectorSupermercado(
                seleccionado  = supermercado,
                otroMercado   = otroMercado,
                alSeleccionar = { supermercado = it },
                alCambiarOtro = { otroMercado = it }
            )

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

            // Sección de productos (componente compartido con el alta)
            SeccionProductos(
                productos       = productosVM,
                formateador     = formateador,
                etiquetaAgregar = stringResource(R.string.action_add),
                textoVacio      = stringResource(R.string.purchase_no_products),
                alAgregar       = { mostrarDialogoProducto = true },
                alAumentar      = { vmProductos.aumentarCantidad(it) },
                alDisminuir     = { vmProductos.disminuirCantidad(it) },
                alEliminar      = { vmProductos.eliminarProducto(it) }
            )

            // Descuento + subtotal + total (componente compartido)
            SeccionTotales(
                subtotal           = total,
                descuento          = descuentoActual,
                descuentoTexto     = descuentoTexto,
                alCambiarDescuento = {
                    descuentoTexto = it
                    descuentoActual = it.toDoubleOrNull() ?: 0.0
                },
                totalFinal  = totalConDescuento,
                formateador = formateador
            )

            if (mensajeError.isNotBlank()) {
                Text(mensajeError, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    scope.launch {
                        // Guardia: si la compra todavía no cargó, no hay nada que guardar
                        val compraCargada = compra ?: return@launch
                        guardando = true; mensajeError = ""
                        runCatching {
                            val repo = RepositorioComprasSupabase(contexto)
                            val compraActualizada = compraCargada.copy(
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
