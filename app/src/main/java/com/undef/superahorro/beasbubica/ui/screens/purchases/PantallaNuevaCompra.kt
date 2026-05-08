package com.undef.superahorro.ui.screens.purchases

import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.undef.superahorro.domain.model.calcularTotal
import com.undef.superahorro.ui.components.BarraSuperior
import com.undef.superahorro.ui.navigation.Pantalla
import com.undef.superahorro.ui.theme.SuperAhorroTheme
import com.undef.superahorro.viewmodel.ViewModelNuevaCompra
import java.text.NumberFormat
import java.util.Locale

/**
 * Pantalla para cargar una compra nueva.
 * El usuario elige el supermercado, la fecha, agrega los productos y el total se calcula solo.
 * La foto del ticket se puede tomar con la cámara o elegir de la galería.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaNuevaCompra(
    navController: NavController,
    viewModel: ViewModelNuevaCompra,
    alVolverAtras: () -> Unit
) {
    val contexto    = LocalContext.current
    val formateador = NumberFormat.getNumberInstance(Locale("es", "AR"))

    // ── Estado del formulario ─────────────────────────────────────────────
    var supermercado by remember { mutableStateOf("") }
    var otroMercado  by remember { mutableStateOf("") }
    var fecha        by remember { mutableStateOf("") }
    var hora         by remember { mutableStateOf("") }
    var expandido    by remember { mutableStateOf(false) }
    var imagenUri    by remember { mutableStateOf<Uri?>(null) }

    // URI temporal para la foto de la cámara — creada antes de disparar la cámara
    var uriCamara    by remember { mutableStateOf<Uri?>(null) }

    // Productos gestionados por el ViewModel compartido
    val productos by viewModel.productos.collectAsState()
    val totalCalculado = productos.calcularTotal()

    val supermercados = listOf("Coto", "Carrefour", "Día", "Jumbo", "Walmart", "La Anónima", "Vea", "Otro")
    val eligioOtro    = supermercado == "Otro"

    val formularioValido = supermercado.isNotBlank() &&
        (!eligioOtro || otroMercado.isNotBlank()) &&
        fecha.isNotBlank() &&
        productos.isNotEmpty()

    // ── Cámara: TakePicture con URI propio — evita crash de TakePicturePreview ──
    val launcherCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exito ->
        if (exito) imagenUri = uriCamara   // foto guardada — usamos el URI que creamos
    }

    // ── Galería: GetContent estándar ──────────────────────────────────────
    val launcherGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> imagenUri = uri }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Nueva Compra",
                mostrarVolver = true,
                alVolverAtras = {
                    viewModel.limpiar()   // limpia productos al cancelar
                    alVolverAtras()
                },
                acciones = {
                    TextButton(onClick = {
                        viewModel.limpiar()
                        alVolverAtras()
                    }) {
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
            // ── Datos de la compra ────────────────────────────────────────
            Text(
                "Datos de la compra",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Dropdown de supermercado
            ExposedDropdownMenuBox(
                expanded = expandido,
                onExpandedChange = { expandido = !expandido }
            ) {
                OutlinedTextField(
                    value = supermercado, onValueChange = {},
                    readOnly    = true,
                    label       = { Text("Supermercado") },
                    leadingIcon = { Icon(Icons.Outlined.Store, null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandido) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape    = MaterialTheme.shapes.medium
                )
                ExposedDropdownMenu(
                    expanded = expandido,
                    onDismissRequest = { expandido = false }
                ) {
                    supermercados.forEach { s ->
                        DropdownMenuItem(
                            text    = { Text(s) },
                            onClick = {
                                supermercado = s
                                expandido    = false
                                if (s != "Otro") otroMercado = ""
                            }
                        )
                    }
                }
            }

            // Campo libre para "Otro"
            if (eligioOtro) {
                OutlinedTextField(
                    value = otroMercado, onValueChange = { otroMercado = it },
                    label       = { Text("Nombre del mercado") },
                    leadingIcon = { Icon(Icons.Outlined.Edit, null) },
                    placeholder = { Text("Ej: Supermercado García") },
                    singleLine  = true,
                    modifier    = Modifier.fillMaxWidth(),
                    shape       = MaterialTheme.shapes.medium
                )
            }

            // Fecha y hora en la misma fila
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = fecha, onValueChange = { fecha = it },
                    label       = { Text("Fecha") },
                    leadingIcon = { Icon(Icons.Outlined.CalendarMonth, null) },
                    placeholder = { Text("DD/MM/AAAA") },
                    singleLine  = true,
                    modifier    = Modifier.weight(1f),
                    shape       = MaterialTheme.shapes.medium
                )
                OutlinedTextField(
                    value = hora, onValueChange = { hora = it },
                    label       = { Text("Hora") },
                    leadingIcon = { Icon(Icons.Outlined.Schedule, null) },
                    placeholder = { Text("HH:MM") },
                    singleLine  = true,
                    modifier    = Modifier.weight(1f),
                    shape       = MaterialTheme.shapes.medium
                )
            }

            HorizontalDivider()

            // ── Sección de productos ──────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Productos (${productos.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                // Navega a la pantalla completa de agregar producto
                FilledTonalButton(
                    onClick = { navController.navigate(Pantalla.NuevoProducto.ruta) }
                ) {
                    Icon(Icons.Outlined.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Agregar producto")
                }
            }

            // Lista de productos con botones +/-
            if (productos.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors   = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Todavía no agregaste productos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Card(modifier = Modifier.fillMaxWidth()) {
                    productos.forEachIndexed { indice, prod ->
                        FilaProducto(
                            nombre       = prod.nombre,
                            cantidad     = prod.cantidad,
                            costo        = prod.costo,
                            codigoBarras = prod.codigoBarras,
                            formateador  = formateador,
                            alAumentar = { viewModel.aumentarCantidad(indice) },
                            alDisminuir = { viewModel.disminuirCantidad(indice) },
                            alEliminar  = { viewModel.eliminarProducto(indice) }
                        )
                        if (indice < productos.lastIndex) HorizontalDivider()
                    }
                }
            }

            // ── Total autocalculado — solo lectura ────────────────────────
            OutlinedTextField(
                value         = if (totalCalculado > 0) "$ ${formateador.format(totalCalculado)}" else "",
                onValueChange = {},
                readOnly      = true,
                label         = { Text("Total de la compra") },
                leadingIcon   = { Icon(Icons.Outlined.AttachMoney, null) },
                placeholder   = { Text("Se calcula automáticamente") },
                supportingText = { Text("Suma de los productos agregados") },
                modifier = Modifier.fillMaxWidth(),
                shape    = MaterialTheme.shapes.medium,
                colors   = OutlinedTextFieldDefaults.colors(
                    focusedTextColor   = MaterialTheme.colorScheme.primary,
                    unfocusedTextColor = MaterialTheme.colorScheme.primary
                )
            )

            HorizontalDivider()

            // ── Foto del ticket ───────────────────────────────────────────
            Text(
                "Foto del ticket",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (imagenUri != null) {
                        AsyncImage(
                            model               = imagenUri,
                            contentDescription  = "Foto del ticket",
                            modifier            = Modifier.fillMaxWidth().height(200.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = { imagenUri = null }) {
                            Text("Cambiar foto", color = MaterialTheme.colorScheme.error)
                        }
                    } else {
                        Icon(
                            Icons.Outlined.Receipt, null,
                            modifier = Modifier.size(48.dp),
                            tint     = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Cámara — crea un URI en MediaStore y lanza TakePicture
                            OutlinedButton(onClick = {
                                val valores = ContentValues().apply {
                                    put(MediaStore.Images.Media.DISPLAY_NAME, "ticket_${System.currentTimeMillis()}.jpg")
                                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                                }
                                val uri = contexto.contentResolver.insert(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, valores
                                )
                                uriCamara = uri
                                if (uri != null) launcherCamara.launch(uri)
                            }) {
                                Icon(Icons.Outlined.CameraAlt, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Cámara")
                            }
                            // Galería — abre el selector del sistema
                            OutlinedButton(onClick = {
                                launcherGaleria.launch("image/*")
                            }) {
                                Icon(Icons.Outlined.Photo, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Galería")
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.limpiar()   // limpia el estado al guardar
                    alVolverAtras()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = MaterialTheme.shapes.extraLarge,
                enabled  = formularioValido
            ) {
                Icon(Icons.Outlined.Save, null)
                Spacer(Modifier.width(8.dp))
                Text("Guardar Compra", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

/**
 * Fila de un producto dentro de la lista de la compra nueva.
 * Tiene los botones de + y − para cambiar la cantidad. La cantidad mínima es 1.
 */
@Composable
private fun FilaProducto(
    nombre: String,
    cantidad: Int,
    costo: Double,
    codigoBarras: String,
    formateador: NumberFormat,
    alAumentar: () -> Unit,
    alDisminuir: () -> Unit,
    alEliminar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Nombre + costo unitario
        Column(modifier = Modifier.weight(1f)) {
            Text(
                nombre,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "$ ${formateador.format(costo)} c/u",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (codigoBarras.isNotBlank()) {
                Text(
                    "Cód: $codigoBarras",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Controles − / cantidad / +
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Botón −
            FilledIconButton(
                onClick  = alDisminuir,
                enabled  = cantidad > 1,
                modifier = Modifier.size(32.dp),
                colors   = IconButtonDefaults.filledIconButtonColors(
                    containerColor        = MaterialTheme.colorScheme.primaryContainer,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(
                    Icons.Outlined.Remove, null,
                    modifier = Modifier.size(16.dp),
                    tint     = if (cantidad > 1) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Número de cantidad
            Text(
                "$cantidad",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier   = Modifier.widthIn(min = 24.dp),
                textAlign  = androidx.compose.ui.text.style.TextAlign.Center
            )

            // Botón +
            FilledIconButton(
                onClick  = alAumentar,
                modifier = Modifier.size(32.dp),
                colors   = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    Icons.Outlined.Add, null,
                    modifier = Modifier.size(16.dp),
                    tint     = MaterialTheme.colorScheme.onPrimary
                )
            }

            // Subtotal
            Text(
                "$ ${formateador.format(cantidad * costo)}",
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary,
                modifier   = Modifier.padding(start = 8.dp).widthIn(min = 72.dp)
            )

            // Botón eliminar
            IconButton(onClick = alEliminar, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Outlined.DeleteOutline, null,
                    modifier = Modifier.size(18.dp),
                    tint     = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Vista() = SuperAhorroTheme {
    PantallaNuevaCompra(rememberNavController(), ViewModelNuevaCompra(), {})
}
