package com.undef.superahorro.ui.screens.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.undef.superahorro.R
import com.undef.superahorro.data.repository.RepositorioPerfilSupabase
import com.undef.superahorro.domain.model.Producto
import com.undef.superahorro.ui.components.*
import com.undef.superahorro.ui.theme.SuperAhorroTheme
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Composable
/**
 * Catálogo personal del usuario cargado desde Supabase.
 * Permite agregar productos con el botón y eliminarlos individualmente.
 */
fun PantallaMisProductos(navController: NavController, alVolverAtras: () -> Unit) {
    val contexto    = LocalContext.current
    val scope       = rememberCoroutineScope()
    val repositorio = remember { RepositorioPerfilSupabase(contexto) }
    val formateador = NumberFormat.getNumberInstance(Locale("es", "AR"))

    var productos       by remember { mutableStateOf<List<Producto>>(emptyList()) }
    var cargando        by remember { mutableStateOf(true) }
    var mostrarDialogo  by remember { mutableStateOf(false) }
    var mensajeError    by remember { mutableStateOf("") }

    // Función de carga reutilizable — se llama al entrar y después de agregar
    suspend fun cargarProductos() {
        cargando = true
        mensajeError = ""
        repositorio.obtenerCatalogo()
            .onSuccess { lista ->
                productos = lista
            }
            .onFailure { e ->
                mensajeError = e.message ?: contexto.getString(R.string.error_loading)
                productos = emptyList()
            }
        cargando = false
    }

    // Recargar cada vez que se monta la pantalla
    LaunchedEffect(Unit) {
        cargarProductos()
    }

    // Diálogo para agregar un producto nuevo al catálogo
    if (mostrarDialogo) {
        DialogoAgregarAlCatalogo(
            onAgregar = { nombre, precio, codigo ->
                scope.launch {
                    repositorio.agregarAlCatalogo(nombre, precio, codigo)
                        .onSuccess { nuevo ->
                            // Actualizar lista local inmediatamente
                            productos = productos + nuevo
                        }
                        .onFailure { e ->
                            mensajeError = contexto.getString(R.string.profile_save_error, e.message ?: "")
                        }
                    mostrarDialogo = false
                }
            },
            onCancelar = { mostrarDialogo = false }
        )
    }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(R.string.products_my_title),
                mostrarVolver = true,
                alVolverAtras = alVolverAtras
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogo = true },
                containerColor = MaterialTheme.colorScheme.tertiary
            ) {
                Icon(Icons.Outlined.Add, null, tint = MaterialTheme.colorScheme.onTertiary)
            }
        },
        bottomBar = { BarraNavegacionInferior(navController) }
    ) { padding ->
        if (mensajeError.isNotBlank()) {
            androidx.compose.material3.Snackbar(
                modifier = androidx.compose.ui.Modifier.padding(padding)
            ) { Text(mensajeError) }
        }
        when {
            cargando -> IndicadorCarga(modifier = Modifier.padding(padding))
            productos.isEmpty() -> Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                EstadoVacio(
                    icono = Icons.Outlined.Inventory2,
                    titulo = stringResource(R.string.products_empty_title),
                    subtitulo = stringResource(R.string.products_empty_subtitle)
                )
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    Text(
                        stringResource(R.string.products_frequent_hint),
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(productos, key = { it.idSupabase }) { prod ->
                    ListItem(
                        headlineContent = { Text(prod.nombre, fontWeight = FontWeight.SemiBold) },
                        supportingContent = {
                            Column {
                                Text(stringResource(R.string.amount_format, formateador.format(prod.precio)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                if (prod.codigo.isNotBlank()) Text(stringResource(R.string.product_code_prefix, prod.codigo), style = MaterialTheme.typography.labelSmall)
                            }
                        },
                        trailingContent = {
                            IconButton(onClick = {
                                scope.launch {
                                    repositorio.eliminarDelCatalogo(prod.idSupabase)
                                        .onSuccess { productos = productos.filter { it.idSupabase != prod.idSupabase } }
                                }
                            }) {
                                Icon(Icons.Outlined.DeleteOutline, null, tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Preview
@Composable
private fun Vista() = SuperAhorroTheme { PantallaMisProductos(rememberNavController(), {}) }
