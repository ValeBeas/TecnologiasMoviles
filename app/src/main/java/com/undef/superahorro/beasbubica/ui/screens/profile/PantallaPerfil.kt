package com.undef.superahorro.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.undef.superahorro.ui.components.BarraNavegacionInferior
import com.undef.superahorro.ui.components.BarraSuperior
import com.undef.superahorro.ui.theme.*

/**
 * Pantalla del perfil
 * Muestra el avatar con las iniciales del usuario,los campos editables y el botón de cerrar sesión
 */
@Composable
fun PantallaPerfil(navController: NavController, alVolverAtras: () -> Unit, alCerrarSesion: () -> Unit) {
    var nombre     by remember { mutableStateOf("Valentina") }
    var apellido   by remember { mutableStateOf("Beas") }
    var email      by remember { mutableStateOf("valentina.beas@undef.edu.ar") }
    var telefono   by remember { mutableStateOf("+54 9 351 000-0000") }
    var mostrarDialogo by remember { mutableStateOf(false) }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            icon    = { Icon(Icons.Outlined.Logout, null) },
            title   = { Text("Cerrar sesión") },
            text    = { Text("¿Estás seguro que querés cerrar sesión?") },
            confirmButton = { TextButton(onClick = { mostrarDialogo = false; alCerrarSesion() }) { Text("Sí, salir", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { mostrarDialogo = false }) { Text("Cancelar") } }
        )
    }

    Scaffold(
        topBar = { BarraSuperior(titulo = "Mi Perfil", mostrarVolver = true, alVolverAtras = alVolverAtras) },
        bottomBar = { BarraNavegacionInferior(navController) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(padding)) {
            // --- Avatar con iniciales ---
            Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary).padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(SurfaceWhite.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                        Text("${nombre.firstOrNull() ?: ""}${apellido.firstOrNull() ?: ""}", style = MaterialTheme.typography.headlineMedium, color = SurfaceWhite, fontWeight = FontWeight.ExtraBold)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("$nombre $apellido", style = MaterialTheme.typography.titleLarge, color = SurfaceWhite, fontWeight = FontWeight.Bold)
                    Text(email, style = MaterialTheme.typography.bodySmall, color = SurfaceWhite.copy(alpha = 0.8f))
                }
            }
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Mis datos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = nombre,   onValueChange = { nombre = it },   label = { Text("Nombre") },   singleLine = true, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.medium)
                    OutlinedTextField(value = apellido, onValueChange = { apellido = it }, label = { Text("Apellido") }, singleLine = true, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.medium)
                }
                OutlinedTextField(value = email,    onValueChange = { email = it },    label = { Text("Correo electrónico") }, leadingIcon = { Icon(Icons.Outlined.Email, null) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium)
                OutlinedTextField(value = telefono, onValueChange = { telefono = it }, label = { Text("Teléfono") },           leadingIcon = { Icon(Icons.Outlined.Phone, null) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium)
                Button(onClick = {}, modifier = Modifier.fillMaxWidth().height(48.dp), shape = MaterialTheme.shapes.extraLarge) { Text("Guardar cambios") }
                HorizontalDivider()
                Text("Cuenta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                OpcionPerfil(Icons.Outlined.Lock,          "Cambiar contraseña") {}
                OpcionPerfil(Icons.Outlined.Notifications, "Notificaciones") {}
                OpcionPerfil(Icons.Outlined.Info,          "Sobre la app") {}
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { mostrarDialogo = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape    = MaterialTheme.shapes.extraLarge,
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border   = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) { Icon(Icons.Outlined.Logout, null); Spacer(Modifier.width(8.dp)); Text("Cerrar sesión") }
            }
        }
    }
}

@Composable
private fun OpcionPerfil(icono: ImageVector, etiqueta: String, alHacerClick: () -> Unit) {
    ListItem(
        headlineContent  = { Text(etiqueta) },
        leadingContent   = { Icon(icono, null, tint = MaterialTheme.colorScheme.primary) },
        trailingContent  = { Icon(Icons.Outlined.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
        modifier         = Modifier.clip(MaterialTheme.shapes.medium).padding(vertical = 2.dp),
        colors           = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    )
    Spacer(Modifier.height(4.dp))
}

@Preview
@Composable
private fun Vista() = SuperAhorroTheme { PantallaPerfil(rememberNavController(), {}, {}) }
