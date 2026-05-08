package com.undef.superahorro.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.undef.superahorro.ui.navigation.Pantalla

/**
 * Barra con las 5 pestañas principales de la app.
 * Recibe el NavController para saber en qué pantalla estoy y marcar la pestaña correcta.
 */
@Composable
fun BarraNavegacionInferior(navController: NavController) {
    // Observa la ruta actual del back stack para marcar el ítem activo
    val entrada by navController.currentBackStackEntryAsState()
    val rutaActual = entrada?.destination?.route

    // Definición de los ítems de la barra inferior
    val items = listOf(
        Triple(Pantalla.Inicio.ruta,        "Inicio",        Icons.Outlined.Home),
        Triple(Pantalla.ListaCompras.ruta,  "Compras",       Icons.Outlined.ShoppingCart),
        Triple(Pantalla.Historial.ruta,     "Historial",     Icons.Outlined.History),
        Triple(Pantalla.Estadisticas.ruta,  "Estadísticas",  Icons.Outlined.BarChart),
        Triple(Pantalla.Perfil.ruta,        "Perfil",        Icons.Outlined.Person),
    )

    NavigationBar {
        items.forEach { (ruta, etiqueta, icono) ->
            NavigationBarItem(
                selected  = rutaActual == ruta,
                onClick   = {
                    if (rutaActual != ruta) {
                        navController.navigate(ruta) {
                            // Evita acumular pantallas en el back stack al cambiar de pestaña
                            popUpTo(Pantalla.Inicio.ruta) { saveState = true }
                            launchSingleTop = true   // No duplica si ya estás en esa pestaña
                            restoreState    = true   // Restaura scroll/estado al volver
                        }
                    }
                },
                icon      = { Icon(icono, contentDescription = etiqueta) },
                label     = { Text(etiqueta, style = MaterialTheme.typography.labelSmall) }
            )
        }
    }
}
