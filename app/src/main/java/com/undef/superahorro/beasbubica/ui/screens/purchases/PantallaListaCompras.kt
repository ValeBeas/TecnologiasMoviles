package com.undef.superahorro.ui.screens.purchases

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.undef.superahorro.data.repository.RepositorioComprasImpl
import com.undef.superahorro.ui.components.*
import com.undef.superahorro.ui.theme.SuperAhorroTheme
import com.undef.superahorro.viewmodel.ViewModelCompras

/**
 * Muestra todas las compras en una lista. La más reciente aparece primero.
 */
@Composable
fun PantallaListaCompras(
    navController: NavController,
    alVerDetalle: (Int) -> Unit,
    alAgregarCompra: () -> Unit,
    alVolverAtras: () -> Unit
) {
    val viewModel = remember { ViewModelCompras(RepositorioComprasImpl()) }
    val estado by viewModel.estadoCompras.collectAsState()

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Mis Compras",
                mostrarVolver = true,
                alVolverAtras = alVolverAtras,
                acciones = { IconButton(onClick = {}) { Icon(Icons.Outlined.Search, null, tint = MaterialTheme.colorScheme.onPrimary) } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = alAgregarCompra, containerColor = MaterialTheme.colorScheme.tertiary) {
                Icon(Icons.Outlined.Add, null, tint = MaterialTheme.colorScheme.onTertiary)
            }
        },
        bottomBar = { BarraNavegacionInferior(navController) }
    ) { padding ->
        when {
            estado.cargando -> IndicadorCarga(modifier = Modifier.padding(padding))
            estado.datos.isNullOrEmpty() -> EstadoVacio(
                icono = Icons.Outlined.ShoppingBag,
                titulo = "Sin compras aún",
                subtitulo = "Registrá tu primera compra tocando el botón +",
                etiquetaAccion = "Nueva compra",
                alAccionar = alAgregarCompra
            )
            else -> LazyColumn(contentPadding = PaddingValues(vertical = 8.dp), modifier = Modifier.padding(padding)) {
                items(estado.datos!!) { compra ->
                    TarjetaCompra(compra = compra, alHacerClick = { alVerDetalle(compra.id) })
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Preview
@Composable
private fun Vista() = SuperAhorroTheme { PantallaListaCompras(rememberNavController(), {}, {}, {}) }
