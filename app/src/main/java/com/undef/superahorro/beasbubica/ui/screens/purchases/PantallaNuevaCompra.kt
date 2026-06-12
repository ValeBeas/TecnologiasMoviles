package com.undef.superahorro.ui.screens.purchases

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.undef.superahorro.data.repository.RepositorioComprasSupabase
import com.undef.superahorro.domain.model.Compra
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

    var expandido    by remember { mutableStateOf(false) }
    var imagenUri    by remember { mutableStateOf<Uri?>(null) }
    var uriCamara    by remember { mutableStateOf<Uri?>(null) }
    var guardando    by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }

    val totalCalculado = productos.calcularTotal()
    val supermercados  = listOf("Coto", "Carrefour", "Día", "Jumbo", "Walmart", "La Anónima", "Vea", "Otro")
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
                titulo = "Nueva Compra",
                mostrarVolver = true,
                alVolverAtras = { viewModel.limpiar(); alVolverAtras() },
                acciones = {
                    TextButton(onClick = { viewModel.limpiar(); alVolverAtras() }) {
                        Text("Cancelar", color = MaterialTheme.colorScheme.onPrimary)
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
            Text("Datos de la compra", style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary)

            // Dropdown supermercado — estado en el ViewModel
            ExposedDropdownMenuBox(expanded = expandido, onExpandedChange = { expandido = !expandido }) {
                OutlinedTextField(
                    value = supermercado, onValueChange = {},
                    readOnly = true,
                    label = { Text("Supermercado") },
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
                    label = { Text("Nombre del mercado") },
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
                Text("Productos (${productos.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary)
                FilledTonalButton(onClick = { navController.navigate(Pantalla.NuevoProducto.ruta) }) {
                    Icon(Icons.Outlined.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Agregar producto")
                }
            }

            if (productos.isEmpty()) {
                Card(modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Box(modifier = Modifier.fillMaxWidth().padding(20.dp),
                        contentAlignment = Alignment.Center) {
                        Text("Todavía no agregaste productos",
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

            // Total solo lectura
            OutlinedTextField(
                value = if (totalCalculado > 0) "$ ${formateador.format(totalCalculado)}" else "",
                onValueChange = {}, readOnly = true,
                label = { Text("Total de la compra") },
                leadingIcon = { Icon(Icons.Outlined.AttachMoney, null) },
                placeholder = { Text("Se calcula automáticamente") },
                supportingText = { Text("Suma de los productos agregados") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )

            HorizontalDivider()

            // Foto del ticket
            Text("Foto del ticket", style = MaterialTheme.typography.titleMedium,
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
                            contentDescription = "Foto del ticket",
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = { imagenUri = null }) {
                            Text("Cambiar foto", color = MaterialTheme.colorScheme.error)
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
                                Text("Cámara")
                            }
                            OutlinedButton(onClick = { launcherGaleria.launch("image/*") }) {
                                Icon(Icons.Outlined.Photo, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Galería")
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

                            // 1. Guardar compra en Supabase y Room
                            val (idLocal, idSupabase) = repo.insertarCompraCompleta(
                                fecha             = fecha,
                                hora              = hora,
                                supermercado      = nombreSuper,
                                total             = totalCalculado,
                                cantidadProductos = productos.size
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
                            mensajeError = "Error al guardar: ${e.message}"
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
                Text("Guardar Compra", style = MaterialTheme.typography.labelLarge)
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
            Text("$ ${formateador.format(producto.costo)} c/u",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (producto.codigoBarras.isNotBlank())
                Text("Cód: ${producto.codigoBarras}",
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
            Text("$ ${formateador.format(producto.subtotal)}",
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
