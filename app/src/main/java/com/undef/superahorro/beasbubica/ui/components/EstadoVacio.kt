package com.undef.superahorro.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Pantalla que muestro cuando una lista no tiene nada.
 * Tiene un ícono, un título, un subtítulo y opcionalmente un botón de acción.
 */
@Composable
/**
 * Pantalla vacía con ícono, título y subtítulo.
 * Se muestra cuando una lista no tiene elementos todavía.
 */
fun EstadoVacio(icono: ImageVector, titulo: String, subtitulo: String, etiquetaAccion: String? = null, alAccionar: () -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icono, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
        Spacer(Modifier.height(16.dp))
        Text(titulo, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(subtitulo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        if (etiquetaAccion != null) {
            Spacer(Modifier.height(24.dp))
            Button(onClick = alAccionar) { Text(etiquetaAccion) }
        }
    }
}
