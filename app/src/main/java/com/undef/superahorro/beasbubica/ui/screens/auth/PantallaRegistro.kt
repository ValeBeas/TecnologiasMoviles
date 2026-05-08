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
import com.undef.superahorro.ui.components.BarraSuperior
import com.undef.superahorro.ui.theme.SuperAhorroTheme

/**
 * Pantalla para crear una cuenta nueva.
 */
@Composable
fun PantallaRegistro(alVolverAlLogin: () -> Unit, alRegistrarse: () -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }
    var verContrasena by remember { mutableStateOf(false) }

    Scaffold(topBar = { BarraSuperior("Crear cuenta", mostrarVolver = true, alVolverAtras = alVolverAlLogin) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Text("Completá tus datos", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.medium)
                OutlinedTextField(value = apellido, onValueChange = { apellido = it }, label = { Text("Apellido") }, singleLine = true, modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.medium)
            }
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo electrónico") }, leadingIcon = { Icon(Icons.Outlined.Email, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium)
            OutlinedTextField(value = contrasena, onValueChange = { contrasena = it }, label = { Text("Contraseña") }, leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                trailingIcon = { IconButton(onClick = { verContrasena = !verContrasena }) { Icon(if (verContrasena) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, null) } },
                visualTransformation = if (verContrasena) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium)
            OutlinedTextField(value = confirmar, onValueChange = { confirmar = it }, label = { Text("Confirmar contraseña") }, leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = confirmar.isNotEmpty() && contrasena != confirmar,
                supportingText = { if (confirmar.isNotEmpty() && contrasena != confirmar) Text("Las contraseñas no coinciden") },
                singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium)
            Spacer(Modifier.height(8.dp))
            Button(onClick = alRegistrarse, modifier = Modifier.fillMaxWidth().height(52.dp), shape = MaterialTheme.shapes.extraLarge,
                enabled = nombre.isNotBlank() && email.isNotBlank() && contrasena.isNotBlank() && contrasena == confirmar) {
                Text("Crear cuenta", style = MaterialTheme.typography.labelLarge)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("¿Ya tenés cuenta?")
                TextButton(onClick = alVolverAlLogin) { Text("Iniciar sesión") }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Vista() = SuperAhorroTheme { PantallaRegistro({}, {}) }
