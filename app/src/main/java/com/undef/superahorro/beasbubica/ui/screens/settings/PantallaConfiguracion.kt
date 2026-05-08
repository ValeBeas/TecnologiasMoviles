package com.undef.superahorro.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.undef.superahorro.ui.components.BarraSuperior
import com.undef.superahorro.ui.theme.SuperAhorroTheme

/**
 * Pantalla de configuración. El toggle de modo oscuro cambia el tema de toda la app.
 * La moneda y las notificaciones son visuales por ahora, se conectan en la Entrega 2.
 */
@Composable
fun PantallaConfiguracion(
    modoOscuro: Boolean,
    alCambiarModo: (Boolean) -> Unit,
    alVolverAtras: () -> Unit
) {
    var notificaciones    by remember { mutableStateOf(true) }
    var monedaSeleccionada by remember { mutableStateOf("ARS") }

    Scaffold(topBar = { BarraSuperior(titulo = "Configuración", mostrarVolver = true, alVolverAtras = alVolverAtras) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {

            // --- Sección: Apariencia ---
            SeccionConfiguracion(titulo = "Apariencia")
            ToggleConfiguracion(
                icono   = Icons.Outlined.DarkMode,
                titulo  = "Modo oscuro",
                activado = modoOscuro,
                alCambiar = alCambiarModo   // Notifica a MainActivity
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // --- Sección: Preferencias ---
            SeccionConfiguracion(titulo = "Preferencias")
            ListItem(
                headlineContent  = { Text("Moneda") },
                supportingContent = { Text(monedaSeleccionada) },
                leadingContent   = { Icon(Icons.Outlined.CurrencyExchange, null, tint = MaterialTheme.colorScheme.primary) },
                trailingContent  = {
                    Row {
                        listOf("ARS", "USD").forEach { moneda ->
                            FilterChip(
                                selected = monedaSeleccionada == moneda,
                                onClick  = { monedaSeleccionada = moneda },
                                label    = { Text(moneda) },
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                },
                modifier = Modifier.clip(MaterialTheme.shapes.medium),
                colors   = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(Modifier.height(8.dp))
            ToggleConfiguracion(Icons.Outlined.Notifications, "Notificaciones", notificaciones) { notificaciones = it }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // --- Sección: Acerca de ---
            SeccionConfiguracion(titulo = "Acerca de")
            ListItem(
                headlineContent  = { Text("Super Ahorro") },
                supportingContent = { Text("Versión 1.0.0 · Valentina Beas & Mirko Bubica Hundt") },
                leadingContent   = { Icon(Icons.Outlined.Info, null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.clip(MaterialTheme.shapes.medium),
                colors   = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    }
}

@Composable
private fun SeccionConfiguracion(titulo: String) {
    Text(titulo, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
private fun ToggleConfiguracion(icono: androidx.compose.ui.graphics.vector.ImageVector, titulo: String, activado: Boolean, alCambiar: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(titulo) },
        leadingContent  = { Icon(icono, null, tint = MaterialTheme.colorScheme.primary) },
        trailingContent = { Switch(checked = activado, onCheckedChange = alCambiar) },
        modifier        = Modifier.clip(MaterialTheme.shapes.medium).padding(bottom = 4.dp),
        colors          = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    )
}

@Preview
@Composable
private fun Vista() = SuperAhorroTheme { PantallaConfiguracion(false, {}, {}) }
