package com.undef.superahorro.ui.screens.purchases

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import com.undef.superahorro.R
import com.undef.superahorro.ui.components.*
import com.undef.superahorro.ui.theme.SuperAhorroTheme
import com.undef.superahorro.viewmodel.ViewModelMoneda
import com.undef.superahorro.viewmodel.ViewModelCompras


@Composable
/**
 * Lista de todas las compras del usuario ordenadas por fecha.
 * Lee de Room (caché local) para respuesta instantánea.
 */
fun PantallaListaCompras(
    navController: NavController,
    viewModelMoneda: ViewModelMoneda,
    alVerDetalle: (String) -> Unit,
    alAgregarCompra: () -> Unit,
    alVolverAtras: () -> Unit
) {
    val contexto = LocalContext.current
    val viewModel = remember { ViewModelCompras(contexto) }
    val estado by viewModel.estadoCompras.collectAsState()

    // Estado de la búsqueda (filtro en memoria sobre lo que ya viene de Room)
    var mostrarBusqueda by remember { mutableStateOf(false) }
    var textoBusqueda by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(R.string.purchase_list),
                mostrarVolver = true,
                alVolverAtras = alVolverAtras,
                acciones = {
                    IconButton(onClick = {
                        mostrarBusqueda = !mostrarBusqueda
                        if (!mostrarBusqueda) textoBusqueda = ""
                    }) {
                        Icon(Icons.Outlined.Search, contentDescription = stringResource(R.string.action_search), tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = alAgregarCompra, containerColor = MaterialTheme.colorScheme.tertiary) {
                Icon(Icons.Outlined.Add, null, tint = MaterialTheme.colorScheme.onTertiary)
            }
        },
        bottomBar = { BarraNavegacionInferior(navController) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            val compras = estado.datos ?: emptyList()
            // Filtra en memoria por supermercado o fecha (sobre lo que ya emitió Room)
            val comprasFiltradas = if (textoBusqueda.isBlank()) compras
                else compras.filter {
                    it.supermercado.contains(textoBusqueda, ignoreCase = true) ||
                    it.fecha.contains(textoBusqueda)
                }

            // Campo de búsqueda — se muestra al tocar la lupa
            if (mostrarBusqueda) {
                OutlinedTextField(
                    value = textoBusqueda,
                    onValueChange = { textoBusqueda = it },
                    placeholder = { Text(stringResource(R.string.search_purchases_hint)) },
                    leadingIcon = { Icon(Icons.Outlined.Search, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = MaterialTheme.shapes.medium
                )
            }

            when {
                estado.cargando -> IndicadorCarga()
                compras.isEmpty() -> EstadoVacio(
                    icono = Icons.Outlined.ShoppingBag,
                    titulo = stringResource(R.string.purchase_empty_title),
                    subtitulo = stringResource(R.string.purchase_empty_subtitle),
                    etiquetaAccion = stringResource(R.string.home_new_purchase),
                    alAccionar = alAgregarCompra
                )
                comprasFiltradas.isEmpty() -> Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.search_no_results), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(comprasFiltradas) { compra ->
                        TarjetaCompra(compra = compra, viewModelMoneda = viewModelMoneda, alHacerClick = { alVerDetalle(compra.idSupabase) })
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Preview
@Composable
private fun Vista() = SuperAhorroTheme { Text("Preview - PantallaListaCompras") }
