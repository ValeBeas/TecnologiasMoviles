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
import com.undef.superahorro.ui.theme.SuperAhorroTheme
import com.undef.superahorro.viewmodel.ViewModelAuth


@Composable
/**
 * Pantalla de inicio de sesión contra Supabase Auth.
 * Muestra error en español si las credenciales son incorrectas.
 */
fun PantallaLogin(
    viewModelAuth: ViewModelAuth,
    alIniciarSesion: () -> Unit,
    alIrARegistro: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var verContrasena by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }

    val estadoAuth by viewModelAuth.estadoUsuario.collectAsState()
    val cargando = estadoAuth.cargando

    // stringResource solo se puede llamar dentro de un @Composable, no dentro de onClick,
    // por eso el texto del error se lee acá y se usa más abajo en el botón.
    val textoCamposIncompletos = stringResource(R.string.error_empty_fields)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        Icon(
            Icons.Outlined.ShoppingCart,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(R.string.app_brand),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            stringResource(R.string.login_welcome_back),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(40.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; mensajeError = "" },
            label = { Text(stringResource(R.string.login_email)) },
            leadingIcon = { Icon(Icons.Outlined.Email, null) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it; mensajeError = "" },
            label = { Text(stringResource(R.string.login_password)) },
            leadingIcon = { Icon(Icons.Outlined.Lock, null) },
            trailingIcon = {
                IconButton(onClick = { verContrasena = !verContrasena }) {
                    Icon(
                        if (verContrasena) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = null
                    )
                }
            },
            visualTransformation = if (verContrasena) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )

        // Mensaje de error
        if (mensajeError.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                mensajeError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                if (email.isBlank() || contrasena.isBlank()) {
                    mensajeError = textoCamposIncompletos
                    return@Button
                }
                viewModelAuth.iniciarSesion(
                    email = email.trim(),
                    contrasena = contrasena,
                    onExito = alIniciarSesion,
                    onError = { mensajeError = it }
                )
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = MaterialTheme.shapes.extraLarge,
            enabled = !cargando
        ) {
            if (cargando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(stringResource(R.string.login_title), style = MaterialTheme.typography.labelLarge)
            }
        }
        Spacer(Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.login_no_account))
            TextButton(onClick = alIrARegistro) { Text(stringResource(R.string.login_register)) }
        }
    }
}
