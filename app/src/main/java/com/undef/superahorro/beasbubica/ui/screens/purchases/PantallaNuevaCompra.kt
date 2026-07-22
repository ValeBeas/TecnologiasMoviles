package com.undef.superahorro.ui.screens.purchases

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.undef.superahorro.R
import com.undef.superahorro.data.network.dto.TicketExtraido
import com.undef.superahorro.data.repository.ApiKeyFaltanteException
import com.undef.superahorro.data.repository.ApiKeyInvalidaException
import com.undef.superahorro.data.repository.RateLimitException
import com.undef.superahorro.data.repository.RepositorioComprasSupabase
import com.undef.superahorro.data.repository.RepositorioTicketGroq
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

    var imagenUri    by remember { mutableStateOf<Uri?>(null) }
    var uriCamara    by remember { mutableStateOf<Uri?>(null) }
    var guardando    by remember { mutableStateOf(false) }
    var analizando   by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }
    var descuentoTexto by remember { mutableStateOf("") }  // texto editable del descuento

    val totalCalculado    = productos.calcularTotal()
    val totalConDescuento = (totalCalculado - descuento).coerceAtLeast(0.0)
    val eligioOtro     = supermercado == "Otro"
    val nombreSuper    = if (eligioOtro) otroMercado else supermercado
    val anioActual     = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
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

    // Launcher para pedir los permisos de cámara en runtime (en Android 7-9 también
    // hace falta WRITE_EXTERNAL_STORAGE para guardar la foto en MediaStore)
    val launcherPermisosCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        if (permisos.values.all { it }) {
            crearUriFoto(contexto)?.let { uri -> uriCamara = uri; launcherCamara.launch(uri) }
        }
    }

    // Verifica los permisos necesarios y abre la cámara (o los pide primero)
    fun abrirCamara() {
        val necesarios = mutableListOf(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            necesarios += Manifest.permission.WRITE_EXTERNAL_STORAGE
        }
        val faltantes = necesarios.filter {
            ContextCompat.checkSelfPermission(contexto, it) != PackageManager.PERMISSION_GRANTED
        }
        if (faltantes.isEmpty()) {
            crearUriFoto(contexto)?.let { uri -> uriCamara = uri; launcherCamara.launch(uri) }
        } else {
            launcherPermisosCamara.launch(faltantes.toTypedArray())
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

            SelectorSupermercado(
                seleccionado  = supermercado,
                otroMercado   = otroMercado,
                alSeleccionar = { viewModel.setSupermercado(it) },
                alCambiarOtro = { viewModel.setOtroMercado(it) }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoFecha(valor = fecha, alCambiar = { viewModel.setFecha(it) }, modifier = Modifier.weight(1f))
                CampoHora(valor = hora, alCambiar = { viewModel.setHora(it) }, modifier = Modifier.weight(1f))
            }

            HorizontalDivider()

            // Sección de productos (componente compartido con la edición)
            SeccionProductos(
                productos       = productos,
                formateador     = formateador,
                etiquetaAgregar = stringResource(R.string.product_add),
                textoVacio      = stringResource(R.string.purchase_no_products_yet),
                alAgregar       = { navController.navigate(Pantalla.NuevoProducto.ruta) },
                alAumentar      = { viewModel.aumentarCantidad(it) },
                alDisminuir     = { viewModel.disminuirCantidad(it) },
                alEliminar      = { viewModel.eliminarProducto(it) }
            )

            SeccionTotales(
                subtotal           = totalCalculado,
                descuento          = descuento,
                descuentoTexto     = descuentoTexto,
                alCambiarDescuento = {
                    descuentoTexto = it
                    viewModel.setDescuento(it.toDoubleOrNull() ?: 0.0)
                },
                totalFinal      = totalConDescuento,
                formateador     = formateador,
                mostrarHintSuma = true
            )

            HorizontalDivider()

            // Foto del ticket + análisis con IA
            Text(stringResource(R.string.purchase_ticket_photo), style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary)
            SeccionFotoTicket(
                imagenUri  = imagenUri,
                analizando = analizando,
                alAnalizar = analizar@{
                    val uri = imagenUri ?: return@analizar
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
                                // Vuelca los datos leídos al formulario (reemplaza los productos)
                                val hayDesfasaje = aplicarTicketAlFormulario(ticket, viewModel, anioActual)
                                // Refleja el descuento leído en el campo editable
                                val descuentoLeido = ticket.descuento ?: 0.0
                                descuentoTexto = if (descuentoLeido > 0) {
                                    if (descuentoLeido % 1.0 == 0.0) descuentoLeido.toInt().toString()
                                    else descuentoLeido.toString()
                                } else ""
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
                alQuitarFoto   = { imagenUri = null },
                alAbrirCamara  = { abrirCamara() },
                alAbrirGaleria = { launcherGaleria.launch("image/*") }
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

                            // Leer bytes de la foto ANTES de cualquier operación async
                            // (el URI puede expirar si se demora en leerlo)
                            val bytesImagen = imagenUri?.let { uri ->
                                runCatching {
                                    contexto.contentResolver.openInputStream(uri)?.readBytes()
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

/**
 * Tarjeta de la foto del ticket: muestra la imagen con los botones de IA y
 * cambiar foto, o los botones de cámara/galería si todavía no hay foto.
 */
@Composable
private fun SeccionFotoTicket(
    imagenUri: Uri?,
    analizando: Boolean,
    alAnalizar: () -> Unit,
    alQuitarFoto: () -> Unit,
    alAbrirCamara: () -> Unit,
    alAbrirGaleria: () -> Unit
) {
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
                    onClick = alAnalizar,
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
                TextButton(onClick = alQuitarFoto) {
                    Text(stringResource(R.string.ticket_change_photo), color = MaterialTheme.colorScheme.error)
                }
            } else {
                Icon(Icons.Outlined.Receipt, null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Cámara — pide permiso en runtime, luego crea URI y abre la cámara
                    OutlinedButton(onClick = alAbrirCamara) {
                        Icon(Icons.Outlined.CameraAlt, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.ticket_camera))
                    }
                    OutlinedButton(onClick = alAbrirGaleria) {
                        Icon(Icons.Outlined.Photo, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.ticket_gallery))
                    }
                }
            }
        }
    }
}

/**
 * Vuelca los datos extraídos del ticket al formulario (ViewModel).
 * Fecha y hora solo se aplican si tienen formato válido; los productos REEMPLAZAN
 * la lista actual. Devuelve true si (suma de productos − descuento) no coincide
 * con el total impreso del ticket (para avisar al usuario).
 */
private fun aplicarTicketAlFormulario(
    ticket: TicketExtraido,
    viewModel: ViewModelNuevaCompra,
    anioActual: Int
): Boolean {
    // Supermercado: si está en la lista lo selecciona; si no, "Otro" con el nombre libre
    ticket.supermercado?.takeIf { it.isNotBlank() }?.let { nombre ->
        val match = SUPERMERCADOS.firstOrNull { it.equals(nombre, ignoreCase = true) && it != "Otro" }
        if (match != null) viewModel.setSupermercado(match)
        else { viewModel.setSupermercado("Otro"); viewModel.setOtroMercado(nombre) }
    }
    // Fecha/hora solo si tienen el formato válido (si no, el campo queda como estaba)
    ticket.fecha?.takeIf { it.isNotBlank() && fechaValida(it, anioActual) }?.let { viewModel.setFecha(it) }
    ticket.hora?.takeIf { it.isNotBlank() && horaValida(it) }?.let { viewModel.setHora(it) }
    // Descuento detectado (0 si no hubo)
    val descuentoLeido = ticket.descuento ?: 0.0
    viewModel.setDescuento(descuentoLeido)
    // Reemplaza la lista: limpia lo anterior antes de cargar el nuevo ticket
    viewModel.limpiarProductos()
    var sumaLeida = 0.0
    ticket.productos?.forEach { p ->
        val nombreP = p.nombre?.trim().orEmpty()
        if (nombreP.isNotBlank()) {
            val cant = p.cantidad ?: 1
            val precioUnit = p.precio ?: 0.0
            viewModel.agregarProducto(ProductoEnCompra(nombreP, cant, precioUnit, p.codigoBarras?.trim().orEmpty()))
            sumaLeida += cant * precioUnit
        }
    }
    // (suma de productos − descuento) debería coincidir con el total del ticket (tolerancia 5% o $1)
    val totalTicket = ticket.total ?: 0.0
    return totalTicket > 0 &&
        kotlin.math.abs((sumaLeida - descuentoLeido) - totalTicket) > maxOf(totalTicket * 0.05, 1.0)
}

/** Crea el URI de MediaStore donde la cámara va a guardar la foto del ticket. */
private fun crearUriFoto(contexto: Context): Uri? =
    contexto.contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "ticket_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
    )

@Preview(showBackground = true)
@Composable
private fun Vista() = SuperAhorroTheme {
    PantallaNuevaCompra(rememberNavController(), ViewModelNuevaCompra(), {})
}
