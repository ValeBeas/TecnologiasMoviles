package com.undef.superahorro.ui.screens.purchases

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.undef.superahorro.R
import com.undef.superahorro.data.repository.ApiKeyFaltanteException
import com.undef.superahorro.data.repository.ApiKeyInvalidaException
import com.undef.superahorro.data.repository.RateLimitException
import com.undef.superahorro.data.repository.RepositorioComprasSupabase
import com.undef.superahorro.data.repository.RepositorioTicketGroq
import com.undef.superahorro.domain.model.Compra
import com.undef.superahorro.domain.model.ProductoEnCompra
import com.undef.superahorro.domain.model.calcularTotal
import com.undef.superahorro.ui.components.BarraSuperior
import com.undef.superahorro.ui.components.CampoFecha
import com.undef.superahorro.ui.components.CampoHora
import com.undef.superahorro.ui.components.fechaValida
import com.undef.superahorro.ui.components.horaValida
import com.undef.superahorro.ui.navigation.Pantalla
import com.undef.superahorro.ui.theme.SuperAhorroTheme
import com.undef.superahorro.viewmodel.ViewModelNuevaCompra
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Formulario para registrar una compra nueva.
 * El estado persiste al navegar a AgregarProducto gracias al ViewModel compartido.
 */
fun PantallaNuevaCompra(
    navController: NavController,
    viewModel: ViewModelNuevaCompra,
    alVolverAtras: () -> Unit
) {
    val contexto    = LocalContext.current
    val scope       = rememberCoroutineScope()
    val formateador = NumberFormat.getNumberInstance(Locale("es", "AR"))

    val supermercado by viewModel.supermercado.collectAsState()
    val otroMercado  by viewModel.otroMercado.collectAsState()
    val fecha        by viewModel.fecha.collectAsState()
    val hora         by viewModel.hora.collectAsState()
    val productos    by viewModel.productos.collectAsState()
    val descuento    by viewModel.descuento.collectAsState()

    var expandido    by remember { mutableStateOf(false) }
    var imagenUri    by remember { mutableStateOf<Uri?>(null) }
    var uriCamara    by remember { mutableStateOf<Uri?>(null) }
    var guardando    by remember { mutableStateOf(false) }
    var analizando   by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }
    var descuentoTexto by remember { mutableStateOf("") }  // texto editable del descuento

    val totalCalculado   = productos.calcularTotal()
    val totalConDescuento = (totalCalculado - descuento).coerceAtLeast(0.0)
    val supermercados  = SUPERMERCADOS
    val eligioOtro     = supermercado == "Otro"
    val nombreSuper    = if (eligioOtro) otroMercado else supermercado
    val anioActual = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    val formularioValido = supermercado.isNotBlank() &&
        (!eligioOtro || otroMercado.isNotBlank()) &&
        productos.isNotEmpty() &&
        fechaValida(fecha, anioActual) &&
        horaValida(hora)

    // Launcher de la cámara — guarda la foto en el URI creado
    val launcherCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito -> if (exito) imagenUri = uriCamara }

    // Launcher de galería
    val launcherGaleria = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> imagenUri = uri }

    // Launcher para pedir permiso de cámara en runtime
    val launcherPermisoCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            // Permiso concedido — crear URI y abrir la cámara
            val valores = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "ticket_${System.currentTimeMillis()}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            }
            val uri = contexto.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, valores
            )
            if (uri != null) { uriCamara = uri; launcherCamara.launch(uri) }
        }
    }

    // Función que verifica el permiso y actúa en consecuencia
    fun abrirCamara() {
        val permiso = ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA)
        if (permiso == PackageManager.PERMISSION_GRANTED) {
            // Ya tiene permiso — crear URI y abrir directamente
            val valores = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "ticket_${System.currentTimeMillis()}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            }
            val uri = contexto.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, valores
            )
            if (uri != null) { uriCamara = uri; launcherCamara.launch(uri) }
        } else {
            // Pedir el permiso primero
            launcherPermisoCamara.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(R.string.purchase_new),
                mostrarVolver = true,
                alVolverAtras = { viewModel.limpiar(); alVolverAtras() },
                acciones = {
                    TextButton(onClick = { viewModel.limpiar(); alVolverAtras() }) {
                        Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(stringResource(R.string.purchase_data_section), style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary)

            // Dropdown supermercado — estado en el ViewModel
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
                                viewModel.setSupermercado(s)
                                expandido = false
                                if (s != "Otro") viewModel.setOtroMercado("")
                            }
                        )
                    }
                }
            }

            if (eligioOtro) {
                OutlinedTextField(
                    value = otroMercado,
                    onValueChange = { viewModel.setOtroMercado(it) },
                    label = { Text(stringResource(R.string.purchase_other_market)) },
                    leadingIcon = { Icon(Icons.Outlined.Edit, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoFecha(
                    valor    = fecha,
                    alCambiar = { viewModel.setFecha(it) },
                    modifier = Modifier.weight(1f)
                )
                CampoHora(
                    valor    = hora,
                    alCambiar = { viewModel.setHora(it) },
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider()

            // Sección productos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.purchase_products_count, productos.size),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary)
                FilledTonalButton(onClick = { navController.navigate(Pantalla.NuevoProducto.ruta) }) {
                    Icon(Icons.Outlined.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.product_add))
                }
            }

            if (productos.isEmpty()) {
                Card(modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Box(modifier = Modifier.fillMaxWidth().padding(20.dp),
                        contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.purchase_no_products_yet),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                Card(modifier = Modifier.fillMaxWidth()) {
                    productos.forEachIndexed { indice, prod ->
                        FilaProducto(
                            producto    = prod,
                            formateador = formateador,
                            alAumentar  = { viewModel.aumentarCantidad(indice) },
                            alDisminuir = { viewModel.disminuirCantidad(indice) },
                            alEliminar  = { viewModel.eliminarProducto(indice) }
                        )
                        if (indice < productos.lastIndex) HorizontalDivider()
                    }
                }
            }

            // Descuento (opcional): se puede cargar a mano o lo completa la IA
            OutlinedTextField(
                value = descuentoTexto,
                onValueChange = {
                    descuentoTexto = it
                    viewModel.setDescuento(it.toDoubleOrNull() ?: 0.0)
                },
                label = { Text(stringResource(R.string.purchase_discount_optional)) },
                leadingIcon = { Icon(Icons.Outlined.LocalOffer, null) },
                prefix = { Text(stringResource(R.string.currency_prefix)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )

            // Subtotal (se muestra si hay descuento, para ver la diferencia)
            if (descuento > 0) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.purchase_subtotal), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stringResource(R.string.amount_format, formateador.format(totalCalculado)))
                }
            }

            // Total solo lectura (ya con el descuento restado)
            OutlinedTextField(
                value = if (totalConDescuento > 0) stringResource(R.string.amount_format, formateador.format(totalConDescuento)) else "",
                onValueChange = {}, readOnly = true,
                label = { Text(stringResource(R.string.purchase_total)) },
                leadingIcon = { Icon(Icons.Outlined.AttachMoney, null) },
                placeholder = { Text(stringResource(R.string.purchase_total_auto)) },
                supportingText = { Text(stringResource(R.string.purchase_total_sum_hint)) },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )

            HorizontalDivider()

            // Foto del ticket
            Text(stringResource(R.string.purchase_ticket_photo), style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (imagenUri != null) {
                        AsyncImage(
                            model = imagenUri,
                            contentDescription = stringResource(R.string.purchase_ticket_photo),
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        // Botón OCR/IA: manda la foto a Groq y precarga el formulario
                        FilledTonalButton(
                            onClick = {
                                val uri = imagenUri ?: return@FilledTonalButton
                                scope.launch {
                                    analizando = true
                                    val bytes = runCatching {
                                        contexto.contentResolver.openInputStream(uri)?.readBytes()
                                    }.getOrNull()
                                    if (bytes == null || bytes.isEmpty()) {
                                        analizando = false
                                        Toast.makeText(contexto, contexto.getString(R.string.ticket_scan_read_error), Toast.LENGTH_LONG).show()
                                        return@launch
                                    }
                                    RepositorioTicketGroq().analizarTicket(bytes)
                                        .onSuccess { ticket ->
                                            // Supermercado: si está en la lista lo selecciona; si no, "Otro"
                                            ticket.supermercado?.takeIf { it.isNotBlank() }?.let { nombre ->
                                                val match = supermercados.firstOrNull { it.equals(nombre, ignoreCase = true) && it != "Otro" }
                                                if (match != null) viewModel.setSupermercado(match)
                                                else { viewModel.setSupermercado("Otro"); viewModel.setOtroMercado(nombre) }
                                            }
                                            // Mejora 1: solo se aplican fecha/hora si tienen el formato válido
                                            ticket.fecha?.takeIf { it.isNotBlank() && fechaValida(it, anioActual) }?.let { viewModel.setFecha(it) }
                                            ticket.hora?.takeIf { it.isNotBlank() && horaValida(it) }?.let { viewModel.setHora(it) }
                                            // Descuento detectado (0 si no hubo) — se refleja en el campo editable
                                            val descuentoLeido = ticket.descuento ?: 0.0
                                            viewModel.setDescuento(descuentoLeido)
                                            descuentoTexto = if (descuentoLeido > 0) {
                                                if (descuentoLeido % 1.0 == 0.0) descuentoLeido.toInt().toString()
                                                else descuentoLeido.toString()
                                            } else ""
                                            // Reemplaza la lista: limpia lo anterior antes de cargar el nuevo ticket
                                            viewModel.limpiarProductos()
                                            // Agrega los productos leídos (ignora los que no tienen nombre) y suma lo leído
                                            var sumaLeida = 0.0
                                            ticket.productos?.forEach { p ->
                                                val nombreP = p.nombre?.trim().orEmpty()
                                                if (nombreP.isNotBlank()) {
                                                    val cant = p.cantidad ?: 1
                                                    val precioUnit = p.precio ?: 0.0
                                                    viewModel.agregarProducto(
                                                        ProductoEnCompra(nombreP, cant, precioUnit, p.codigoBarras?.trim().orEmpty())
                                                    )
                                                    sumaLeida += cant * precioUnit
                                                }
                                            }
                                            // Mejora 3: (suma de productos − descuento) debería coincidir con el total del ticket
                                            val totalTicket = ticket.total ?: 0.0
                                            val hayDesfasaje = totalTicket > 0 &&
                                                kotlin.math.abs((sumaLeida - descuentoLeido) - totalTicket) > maxOf(totalTicket * 0.05, 1.0)
                                            val idMensaje = if (hayDesfasaje) R.string.ticket_scan_total_mismatch
                                                            else R.string.ticket_scan_success
                                            Toast.makeText(contexto, contexto.getString(idMensaje), Toast.LENGTH_LONG).show()
                                        }
                                        .onFailure { e ->
                                            // Mensaje según el tipo de error
                                            val idError = when (e) {
                                                is ApiKeyFaltanteException -> R.string.ticket_scan_no_key
                                                is ApiKeyInvalidaException -> R.string.ticket_scan_invalid_key
                                                is RateLimitException      -> R.string.ticket_scan_rate_limit
                                                else -> R.string.ticket_scan_error
                                            }
                                            Toast.makeText(contexto, contexto.getString(idError), Toast.LENGTH_LONG).show()
                                        }
                                    analizando = false
                                }
                            },
                            enabled = !analizando,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (analizando) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.ticket_scanning))
                            } else {
                                Icon(Icons.Outlined.AutoAwesome, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.ticket_scan_ai))
                            }
                        }
                        TextButton(onClick = { imagenUri = null }) {
                            Text(stringResource(R.string.ticket_change_photo), color = MaterialTheme.colorScheme.error)
                        }
                    } else {
                        Icon(Icons.Outlined.Receipt, null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Cámara — pide permiso en runtime, luego crea URI y abre la cámara
                            OutlinedButton(onClick = { abrirCamara() }) {
                                Icon(Icons.Outlined.CameraAlt, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.ticket_camera))
                            }
                            OutlinedButton(onClick = { launcherGaleria.launch("image/*") }) {
                                Icon(Icons.Outlined.Photo, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.ticket_gallery))
                            }
                        }
                    }
                }
            }

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

                            // Leer bytes de la foto ANTES de cualquier operación async
                            // (el URI puede expirar si se demora en leerlo)
                            val bytesImagen = imagenUri?.let { uri ->
                                runCatching {
                                    val bytes = contexto.contentResolver.openInputStream(uri)?.readBytes()
                                    bytes
                                }.onFailure { e ->
                                }.getOrNull()
                            }

                            // 1. Guardar compra en Supabase y Room (total ya con descuento restado)
                            val (idLocal, idSupabase) = repo.insertarCompraCompleta(
                                fecha             = fecha,
                                hora              = hora,
                                supermercado      = nombreSuper,
                                total             = totalConDescuento,
                                cantidadProductos = productos.size,
                                descuento         = descuento
                            )

                            // 2. Subir foto con los bytes ya leídos
                            if (bytesImagen != null && bytesImagen.isNotEmpty() && idSupabase.isNotBlank()) {
                                val urlFoto = repo.subirFotoTicketBytes(bytesImagen, idSupabase)
                                if (urlFoto != null) {
                                    repo.actualizarFotoTicket(idLocal, idSupabase, urlFoto)
                                }
                            }

                            // 3. Guardar productos
                            if (idSupabase.isNotBlank()) {
                                repo.insertarProductos(idLocal, idSupabase, productos)
                            }

                            viewModel.limpiar()
                            alVolverAtras()
                        }.onFailure { e ->
                            mensajeError = contexto.getString(R.string.profile_save_error, e.message ?: "")
                        }
                        guardando = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.extraLarge,
                enabled = formularioValido && !guardando
            ) {
                if (guardando) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Icon(Icons.Outlined.Save, null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.purchase_save), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun FilaProducto(
    producto: com.undef.superahorro.domain.model.ProductoEnCompra,
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
                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
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

@Preview(showBackground = true)
@Composable
private fun Vista() = SuperAhorroTheme {
    PantallaNuevaCompra(rememberNavController(), ViewModelNuevaCompra(), {})
}
