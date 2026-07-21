package com.undef.superahorro.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.undef.superahorro.R
import com.undef.superahorro.ui.navigation.Pantalla


@Composable
/**
 * Barra con las 6 pestañas principales de la app.
 * Detecta la ruta activa y la marca automáticamente.
 */
fun BarraNavegacionInferior(navController: NavController) {
    // Observa la ruta actual del back stack para marcar el ítem activo
    val entrada by navController.currentBackStackEntryAsState()
    val rutaActual = entrada?.destination?.route

    // Definición de los ítems de la barra inferior
    val items = listOf(
        Triple(Pantalla.Inicio.ruta,        stringResource(R.string.nav_home),       Icons.Outlined.Home),
        Triple(Pantalla.ListaCompras.ruta,  stringResource(R.string.nav_purchases),   Icons.Outlined.ShoppingCart),
        Triple(Pantalla.Historial.ruta,     stringResource(R.string.nav_history),     Icons.Outlined.History),
        Triple(Pantalla.Estadisticas.ruta,  stringResource(R.string.nav_statistics),  Icons.Outlined.BarChart),
        Triple(Pantalla.MisProductos.ruta,  stringResource(R.string.nav_products),    Icons.Outlined.Inventory2),
        Triple(Pantalla.Perfil.ruta,        stringResource(R.string.nav_profile),     Icons.Outlined.Person),
    )

    NavigationBar(tonalElevation = 0.dp) {
        items.forEach { (ruta, etiqueta, icono) ->
            NavigationBarItem(
                selected  = rutaActual == ruta,
                onClick   = {
                    if (rutaActual != ruta) {
                        navController.navigate(ruta) {
                            popUpTo(Pantalla.Inicio.ruta) { saveState = true }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    }
                },
                icon      = { Icon(icono, contentDescription = etiqueta) },
                label     = { Text(etiqueta, fontSize = 9.sp, maxLines = 1, softWrap = false) }
            )
        }
    }
}
