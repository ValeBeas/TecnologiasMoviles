package com.undef.superahorro.ui.screens.products

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.undef.superahorro.data.repository.RepositorioPerfilSupabase
import com.undef.superahorro.domain.model.Producto
import com.undef.superahorro.domain.model.ProductoEnCompra
import com.undef.superahorro.ui.components.BarraSuperior
import com.undef.superahorro.ui.components.IndicadorCarga
import com.undef.superahorro.ui.theme.SuperAhorroTheme
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Composable
/**
 * Pantalla para agregar un producto a la compra en curso.
 * Tiene dos tabs: ingreso manual y catálogo personal del usuario.
 */
fun PantallaNuevoProducto(
    alAgregarProducto: (ProductoEnCompra) -> Unit,
    alVolverAtras: () -> Unit
) {
    val contexto    = LocalContext.current
    val scope       = rememberCoroutineScope()
    val repositorio = remember { RepositorioPerfilSupabase(contexto) }
    val formateador = NumberFormat.getNumberInstance(Locale("es", "AR"))

    var codigoBarras    by remember { mutableStateOf("") }
    var nombre          by remember { mutableStateOf("") }
    var cantidad        by remember { mutableIntStateOf(1) }
    var costo           by remember { mutableStateOf("") }
    var mostrarCatalogo         by remember { mutableStateOf(false) }
    var catalogoUsuario         by remember { mutableStateOf<List<Producto>>(emptyList()) }
    var cargandoCatalogo        by remember { mutableStateOf(false) }
    var mostrarDialogoCatalogo  by remember { mutableStateOf(false) }

    val costoDouble  = costo.toDoubleOrNull() ?: 0.0
    val subtotal     = cantidad * costoDouble
    val puedeAgregar = nombre.isNotBlank() && costoDouble > 0

    // Cargar catálogo cada vez que se muestra el tab
    LaunchedEffect(mostrarCatalogo) {
        if (mostrarCatalogo) {
            cargandoCatalogo = true
            repositorio.obtenerCatalogo()
                .onSuccess { catalogoUsuario = it }
                .onFailure { catalogoUsuario = emptyList() }
            cargandoCatalogo = false
        }
    }

    // Diálogo para agregar un producto nuevo al catálogo personal
    if (mostrarDialogoCatalogo) {
        DialogoAgregarAlCatalogo(
            onAgregar = { nombre, precio, codigo ->
                scope.launch {
                    repositorio.agregarAlCatalogo(nombre, precio, codigo)
                        .onSuccess { nuevo ->
                            // Agregar inmediatamente a la lista local
                            catalogoUsuario = catalogoUsuario + nuevo
                        }
                    mostrarDialogoCatalogo = false
                }
            },
            onCancelar = { mostrarDialogoCatalogo = false }
        )
    }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Agregar producto",
                mostrarVolver = true,
                alVolverAtras = alVolverAtras,
                acciones = {
                    TextButton(onClick = alVolverAtras) {
                        Text("Cancelar", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = if (mostrarCatalogo) 1 else 0) {
                Tab(selected = !mostrarCatalogo, onClick = { mostrarCatalogo = false }, text = { Text("Ingresar datos") })
                Tab(selected = mostrarCatalogo,  onClick = { mostrarCatalogo = true },  text = { Text("Mis productos") })
            }

            if (mostrarCatalogo) {
                when {
                    cargandoCatalogo -> IndicadorCarga()
                    catalogoUsuario.isEmpty() -> Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Outlined.Inventory2, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Sin productos guardados", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(16.dp))
                        FilledTonalButton(onClick = { mostrarDialogoCatalogo = true }) {
                            Icon(Icons.Outlined.Add, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Agregar al catálogo")
                        }
                    }
                    else -> {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Tocá un producto para cargarlo", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            TextButton(onClick = { mostrarDialogoCatalogo = true }) {
                                Icon(Icons.Outlined.Add, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Nuevo", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                        LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                            items(catalogoUsuario, key = { it.idSupabase }) { prod ->
                                ListItem(
                                    headlineContent   = { Text(prod.nombre, fontWeight = FontWeight.SemiBold) },
                                    supportingContent = {
                                        if (prod.codigo.isNotBlank()) Text("Cód: ${prod.codigo}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    },
                                    trailingContent   = {
                                        Text("$ ${formateador.format(prod.precio)}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    },
                                    modifier = Modifier.clickable {
                                        codigoBarras    = prod.codigo
                                        nombre          = prod.nombre
                                        costo           = prod.precio.toString()
                                        mostrarCatalogo = false
                                    }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(value = codigoBarras, onValueChange = { codigoBarras = it }, label = { Text("Código de barras (opcional)") }, leadingIcon = { Icon(Icons.Outlined.QrCodeScanner, null) }, placeholder = { Text("Ej: 7790040552") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium)
                    OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre del producto *") }, leadingIcon = { Icon(Icons.Outlined.Inventory2, null) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Cantidad", style = MaterialTheme.typography.bodyLarge)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            FilledIconButton(onClick = { if (cantidad > 1) cantidad-- }, colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                                Icon(Icons.Outlined.Remove, null, tint = MaterialTheme.colorScheme.primary)
                            }
                            Text("$cantidad", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.widthIn(min = 32.dp))
                            FilledIconButton(onClick = { cantidad++ }, colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                                Icon(Icons.Outlined.Add, null, tint = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    }

                    OutlinedTextField(value = costo, onValueChange = { costo = it }, label = { Text("Costo unitario *") }, leadingIcon = { Icon(Icons.Outlined.AttachMoney, null) }, prefix = { Text("$ ") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium)

                    if (subtotal > 0) {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Subtotal (${cantidad}× $ ${formateador.format(costoDouble)})", style = MaterialTheme.typography.bodyMedium)
                                Text("$ ${formateador.format(subtotal)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { alAgregarProducto(ProductoEnCompra(nombre.trim(), cantidad, costoDouble, codigoBarras.trim())) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = MaterialTheme.shapes.extraLarge,
                        enabled = puedeAgregar
                    ) {
                        Icon(Icons.Outlined.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Agregar a la compra", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}


@Composable
private fun DialogoAgregarAlCatalogo(
    onAgregar: (String, Double, String) -> Unit,
    onCancelar: () -> Unit
) {
    var nombre  by remember { mutableStateOf("") }
    var precio  by remember { mutableStateOf("") }
    var codigo  by remember { mutableStateOf("") }
    val precioOk = precio.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Agregar al catálogo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nombre, onValueChange = { nombre = it },
                    label = { Text("Nombre *") },
                    leadingIcon = { Icon(Icons.Outlined.Inventory2, null) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
                OutlinedTextField(
                    value = precio, onValueChange = { precio = it },
                    label = { Text("Precio *") },
                    prefix = { Text("$ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
                OutlinedTextField(
                    value = codigo, onValueChange = { codigo = it },
                    label = { Text("Código de barras (opcional)") },
                    leadingIcon = { Icon(Icons.Outlined.QrCodeScanner, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAgregar(nombre.trim(), precioOk, codigo.trim()) },
                enabled = nombre.isNotBlank() && precioOk > 0
            ) { Text("Agregar") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun Vista() = SuperAhorroTheme { PantallaNuevoProducto({}, {}) }
