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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.undef.superahorro.ui.theme.SuperAhorroTheme

/**
 * Pantalla de inicio de sesión.
 * Cualquier email y contraseña funcionan.
 */
@Composable
fun PantallaLogin(alIniciarSesion: () -> Unit, alIrARegistro: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var verContrasena by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        Icon(Icons.Outlined.ShoppingCart, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(72.dp))
        Spacer(Modifier.height(12.dp))
        Text("SUPER AHORRO", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        Text("Bienvenido de nuevo", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(40.dp))

        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            leadingIcon = { Icon(Icons.Outlined.Email, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = contrasena, onValueChange = { contrasena = it },
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Outlined.Lock, null) },
            trailingIcon = {
                IconButton(onClick = { verContrasena = !verContrasena }) {
                    Icon(if (verContrasena) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, null)
                }
            },
            visualTransformation = if (verContrasena) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium
        )
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = {}, modifier = Modifier.align(Alignment.End)) {
            Text("¿Olvidaste tu contraseña?")
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { cargando = true; alIniciarSesion() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = MaterialTheme.shapes.extraLarge,
            enabled = !cargando
        ) {
            if (cargando) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
            else Text("Iniciar sesión", style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("¿No tenés cuenta?")
            TextButton(onClick = alIrARegistro) { Text("Registrate") }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Vista() = SuperAhorroTheme { PantallaLogin({}, {}) }
