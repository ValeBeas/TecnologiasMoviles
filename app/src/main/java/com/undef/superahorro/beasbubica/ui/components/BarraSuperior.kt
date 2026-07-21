package com.undef.superahorro.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.undef.superahorro.R


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
                    Icon(Icons.Outlined.ArrowBackIosNew, contentDescription = stringResource(R.string.action_back))
                }
            }
        },
        actions = {
            acciones()
            if (mostrarConfiguracion) {
                IconButton(onClick = alAbrirConfiguracion) {
                    Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.action_settings))
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
