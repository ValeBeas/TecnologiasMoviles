package com.undef.superahorro.ui.screens.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.undef.superahorro.R

/**
 * Diálogo para dar de alta un producto en el catálogo personal del usuario.
 * Lo comparten PantallaMisProductos y PantallaNuevoProducto (antes estaba duplicado).
 * Devuelve (nombre, precio, código) por el callback onAgregar.
 */
@Composable
fun DialogoAgregarAlCatalogo(
    onAgregar: (String, Double, String) -> Unit,
    onCancelar: () -> Unit
) {
    var nombre  by remember { mutableStateOf("") }
    var precio  by remember { mutableStateOf("") }
    var codigo  by remember { mutableStateOf("") }
    val precioOk = precio.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(stringResource(R.string.catalog_add_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nombre, onValueChange = { nombre = it },
                    label = { Text(stringResource(R.string.register_name) + " *") },
                    leadingIcon = { Icon(Icons.Outlined.Inventory2, null) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
                OutlinedTextField(
                    value = precio, onValueChange = { precio = it },
                    label = { Text(stringResource(R.string.product_price_simple) + " *") },
                    prefix = { Text(stringResource(R.string.currency_prefix)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
                OutlinedTextField(
                    value = codigo, onValueChange = { codigo = it },
                    label = { Text(stringResource(R.string.product_code_optional)) },
                    leadingIcon = { Icon(Icons.Outlined.QrCodeScanner, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAgregar(nombre.trim(), precioOk, codigo.trim()) },
                enabled = nombre.isNotBlank() && precioOk > 0
            ) { Text(stringResource(R.string.action_add)) }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}
