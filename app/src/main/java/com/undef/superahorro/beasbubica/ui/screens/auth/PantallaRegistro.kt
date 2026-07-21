package com.undef.superahorro.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.undef.superahorro.R
import com.undef.superahorro.ui.components.BarraSuperior
import com.undef.superahorro.ui.theme.SuperAhorroTheme
import com.undef.superahorro.viewmodel.ViewModelAuth


@Composable
/**
 * Pantalla de registro de nuevo usuario.
 * El botón se habilita solo cuando las contraseñas coinciden y los campos están completos.
 */
fun PantallaRegistro(
    viewModelAuth: ViewModelAuth,
    alVolverAlLogin: () -> Unit,
    alRegistrarse: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }
    var verContrasena by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }

    val estadoAuth by viewModelAuth.estadoUsuario.collectAsState()
    val cargando = estadoAuth.cargando

    val contrasenaValida = contrasena.length >= 6
    val contrasenaCoincide = contrasena == confirmar
    val formularioValido = nombre.isNotBlank() && email.isNotBlank() &&
        contrasenaValida && contrasenaCoincide

    Scaffold(topBar = {
        BarraSuperior(stringResource(R.string.register_title), mostrarVolver = true, alVolverAtras = alVolverAlLogin)
    }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.register_subtitle),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it; mensajeError = "" },
                    label = { Text(stringResource(R.string.register_name) + " *") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                )
                OutlinedTextField(
                    value = apellido,
                    onValueChange = { apellido = it },
                    label = { Text(stringResource(R.string.register_last_name)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                )
            }
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; mensajeError = "" },
                label = { Text(stringResource(R.string.login_email) + " *") },
                leadingIcon = { Icon(Icons.Outlined.Email, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )
            OutlinedTextField(
                value = contrasena,
                onValueChange = { contrasena = it; mensajeError = "" },
                label = { Text(stringResource(R.string.login_password) + " * " + stringResource(R.string.register_password_hint)) },
                leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { verContrasena = !verContrasena }) {
                        Icon(
                            if (verContrasena) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            null
                        )
                    }
                },
                visualTransformation = if (verContrasena) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = contrasena.isNotEmpty() && !contrasenaValida,
                supportingText = {
                    if (contrasena.isNotEmpty() && !contrasenaValida)
                        Text(stringResource(R.string.register_password_min))
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )
            OutlinedTextField(
                value = confirmar,
                onValueChange = { confirmar = it },
                label = { Text(stringResource(R.string.register_confirm_password) + " *") },
                leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = confirmar.isNotEmpty() && !contrasenaCoincide,
                supportingText = {
                    if (confirmar.isNotEmpty() && !contrasenaCoincide)
                        Text(stringResource(R.string.register_password_mismatch))
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )

            if (mensajeError.isNotBlank()) {
                Text(
                    mensajeError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    viewModelAuth.registrarse(
                        email = email.trim(),
                        contrasena = contrasena,
                        nombre = nombre.trim(),
                        apellido = apellido.trim(),
                        onExito = alRegistrarse,
                        onError = { mensajeError = it }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.extraLarge,
                enabled = formularioValido && !cargando
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(R.string.register_title), style = MaterialTheme.typography.labelLarge)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.register_already_account))
                TextButton(onClick = alVolverAlLogin) { Text(stringResource(R.string.login_title)) }
            }
        }
    }
}
