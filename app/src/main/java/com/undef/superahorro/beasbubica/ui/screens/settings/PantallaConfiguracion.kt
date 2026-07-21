package com.undef.superahorro.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.undef.superahorro.R
import com.undef.superahorro.ui.components.BarraSuperior
import com.undef.superahorro.ui.theme.SuperAhorroTheme
import com.undef.superahorro.viewmodel.ViewModelMoneda


@Composable
/**
 * Ajustes de la app: modo oscuro y moneda (ARS/USD).
 * El modo oscuro y la moneda se persisten en DataStore entre sesiones.
 */
fun PantallaConfiguracion(
    modoOscuro: Boolean,
    alCambiarModo: (Boolean) -> Unit,
    viewModelMoneda: ViewModelMoneda,
    alVolverAtras: () -> Unit
) {
    val monedaActiva by viewModelMoneda.monedaActiva.collectAsState()
    val tipoCambio by viewModelMoneda.tipoCambio.collectAsState()
    val cargandoCotizacion by viewModelMoneda.cargandoCotizacion.collectAsState()


    Scaffold(topBar = {
        BarraSuperior(stringResource(R.string.settings_title), mostrarVolver = true, alVolverAtras = alVolverAtras)
    }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            SeccionConfig(stringResource(R.string.settings_appearance))
            ToggleConfig(Icons.Outlined.DarkMode, stringResource(R.string.settings_dark_mode), modoOscuro, alCambiarModo)

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            SeccionConfig(stringResource(R.string.settings_currency))

            // Selector ARS / USD conectado al ViewModel
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_currency_display)) },
                supportingContent = {
                    if (monedaActiva == "USD" && tipoCambio != null) {
                        Text(stringResource(R.string.settings_exchange_rate, String.format("%.0f", tipoCambio!!.valorPromedio)))
                    } else if (cargandoCotizacion) {
                        Text(stringResource(R.string.settings_loading_rate))
                    }
                },
                leadingContent = {
                    Icon(Icons.Outlined.CurrencyExchange, null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingContent = {
                    Row {
                        listOf("ARS", "USD").forEach { moneda ->
                            FilterChip(
                                selected = monedaActiva == moneda,
                                onClick  = { viewModelMoneda.cambiarMoneda(moneda) },
                                label    = { Text(moneda) },
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                },
                modifier = Modifier.clip(MaterialTheme.shapes.medium),
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            )

            // Botón para actualizar la cotización manualmente
            if (monedaActiva == "USD") {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { viewModelMoneda.cargarTipoCambio() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !cargandoCotizacion
                ) {
                    if (cargandoCotizacion) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Icon(Icons.Outlined.Refresh, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.settings_refresh_rate))
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            SeccionConfig(stringResource(R.string.settings_about))
            ListItem(
                headlineContent = { Text(stringResource(R.string.app_name)) },
                supportingContent = { Text(stringResource(R.string.settings_version_authors)) },
                leadingContent = { Icon(Icons.Outlined.Info, null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.clip(MaterialTheme.shapes.medium),
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    }
}

@Composable
private fun SeccionConfig(titulo: String) {
    Text(
        titulo,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun ToggleConfig(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    activado: Boolean,
    alCambiar: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(titulo) },
        leadingContent = { Icon(icono, null, tint = MaterialTheme.colorScheme.primary) },
        trailingContent = { Switch(checked = activado, onCheckedChange = alCambiar) },
        modifier = Modifier.clip(MaterialTheme.shapes.medium).padding(bottom = 4.dp),
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    )
}
