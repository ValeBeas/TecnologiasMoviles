package com.undef.superahorro.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

/**
 * Barra superior que uso en todas las pantallas.
 * Le puedo pasar el título, si quiero la flecha de volver y botones extra a la derecha.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Barra superior reutilizable en todas las pantallas.
 * Soporta flecha de volver y botones de acción opcionales a la derecha.
 */
fun BarraSuperior(
    titulo: String,
    mostrarVolver: Boolean = false,
    alVolverAtras: () -> Unit = {},
    mostrarConfiguracion: Boolean = false,
    alAbrirConfiguracion: () -> Unit = {},
    acciones: @Composable () -> Unit = {}
) {
    TopAppBar(
        title = { Text(titulo, style = MaterialTheme.typography.titleLarge) },
        navigationIcon = {
            if (mostrarVolver) {
                IconButton(onClick = alVolverAtras) {
                    Icon(Icons.Outlined.ArrowBackIosNew, contentDescription = "Volver")
                }
            }
        },
        actions = {
            acciones()
            if (mostrarConfiguracion) {
                IconButton(onClick = alAbrirConfiguracion) {
                    Icon(Icons.Outlined.Settings, contentDescription = "Configuración")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor          = MaterialTheme.colorScheme.primary,
            titleContentColor       = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor  = MaterialTheme.colorScheme.onPrimary
        )
    )
}
