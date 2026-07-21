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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.undef.superahorro.R
import com.undef.superahorro.data.repository.RepositorioPerfilSupabase
import com.undef.superahorro.ui.components.BarraNavegacionInferior
import com.undef.superahorro.ui.components.BarraSuperior
import com.undef.superahorro.ui.theme.SurfaceWhite
import com.undef.superahorro.viewmodel.ViewModelAuth
import kotlinx.coroutines.launch

@Composable
/**
 * Perfil del usuario con nombre y apellido editables.
 * Guarda los cambios en Supabase (tabla perfiles) y en DataStore.
 */
fun PantallaPerfil(
    navController: NavController,
    viewModelAuth: ViewModelAuth,
    alVolverAtras: () -> Unit,
    alCerrarSesion: () -> Unit
) {
    val contexto = LocalContext.current
    val scope    = rememberCoroutineScope()
    val repositorio = remember { RepositorioPerfilSupabase(contexto) }

    val estadoUsuario by viewModelAuth.estadoUsuario.collectAsState()
    val usuario = estadoUsuario.datos

    var nombre         by remember { mutableStateOf(usuario?.nombre ?: "") }
    var apellido       by remember { mutableStateOf(usuario?.apellido ?: "") }
    val email          = usuario?.email ?: ""
    var mostrarDialogo by remember { mutableStateOf(false) }
    var guardando      by remember { mutableStateOf(false) }
    var mensajeGuardado by remember { mutableStateOf("") }

    // Cargar perfil actualizado desde Supabase al entrar
    LaunchedEffect(Unit) {
        repositorio.obtenerPerfil()
            .onSuccess { perfil ->
                if (!perfil.nombre.isNullOrBlank()) nombre   = perfil.nombre!!
                if (!perfil.apellido.isNullOrBlank()) apellido = perfil.apellido!!
            }
    }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            icon    = { Icon(Icons.Outlined.Logout, null) },
            title   = { Text(stringResource(R.string.profile_logout)) },
            text    = { Text(stringResource(R.string.profile_logout_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogo = false
                    viewModelAuth.cerrarSesion { alCerrarSesion() }
                }) { Text(stringResource(R.string.profile_logout_yes), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }

    Scaffold(
        topBar = { BarraSuperior(stringResource(R.string.profile_title), mostrarVolver = true, alVolverAtras = alVolverAtras) },
        bottomBar = { BarraNavegacionInferior(navController) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(padding)
        ) {
            // Avatar con iniciales
            Box(
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary).padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val iniciales = "${nombre.firstOrNull() ?: ""}${apellido.firstOrNull() ?: ""}".uppercase()
                        .ifBlank { email.firstOrNull()?.uppercase() ?: "U" }
                    Box(
                        modifier = Modifier.size(80.dp).clip(CircleShape).background(SurfaceWhite.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(iniciales, style = MaterialTheme.typography.headlineMedium, color = SurfaceWhite, fontWeight = FontWeight.ExtraBold)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("$nombre $apellido".trim().ifBlank { email }, style = MaterialTheme.typography.titleLarge, color = SurfaceWhite, fontWeight = FontWeight.Bold)
                    Text(email, style = MaterialTheme.typography.bodySmall, color = SurfaceWhite.copy(alpha = 0.8f))
                }
            }

            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(R.string.profile_my_data), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = nombre, onValueChange = { nombre = it; mensajeGuardado = "" }, label = { Text(stringResource(R.string.register_name)) }, singleLine = true, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.medium)
                    OutlinedTextField(value = apellido, onValueChange = { apellido = it; mensajeGuardado = "" }, label = { Text(stringResource(R.string.register_last_name)) }, singleLine = true, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.medium)
                }

                OutlinedTextField(value = email, onValueChange = {}, label = { Text(stringResource(R.string.login_email)) }, leadingIcon = { Icon(Icons.Outlined.Email, null) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, enabled = false)

                // Mensaje de confirmación
                if (mensajeGuardado.isNotBlank()) {
                    Text(mensajeGuardado, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                }

                // Botón guardar — actualiza Supabase y DataStore
                Button(
                    onClick = {
                        scope.launch {
                            guardando = true
                            repositorio.guardarPerfil(nombre.trim(), apellido.trim())
                                .onSuccess { mensajeGuardado = contexto.getString(R.string.profile_saved) }
                                .onFailure { mensajeGuardado = contexto.getString(R.string.profile_save_error, it.message ?: "") }
                            guardando = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape   = MaterialTheme.shapes.extraLarge,
                    enabled = !guardando
                ) {
                    if (guardando) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Icon(Icons.Outlined.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.profile_save))
                }

                HorizontalDivider()
                Text(stringResource(R.string.profile_account), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                OpcionPerfil(Icons.Outlined.Info, stringResource(R.string.profile_about)) {}
                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { mostrarDialogo = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape    = MaterialTheme.shapes.extraLarge,
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border   = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Outlined.Logout, null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.profile_logout))
                }
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
