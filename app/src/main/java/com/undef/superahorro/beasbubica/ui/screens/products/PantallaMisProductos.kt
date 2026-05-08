package com.undef.superahorro.ui.screens.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.undef.superahorro.ui.components.BarraNavegacionInferior
import com.undef.superahorro.ui.components.EstadoVacio
import com.undef.superahorro.ui.theme.SuperAhorroTheme
import java.text.NumberFormat
import java.util.Locale

/**
 * Pantalla con el catálogo personal de productos frecuentes.
 */
@Composable
fun PantallaMisProductos(navController: NavController, alVolverAtras: () -> Unit) {
    val formateador = NumberFormat.getNumberInstance(Locale("es", "AR"))

    Scaffold(
        topBar = {
            com.undef.superahorro.ui.components.BarraSuperior(
                titulo = "Mis Productos",
                mostrarVolver = true,
                alVolverAtras = alVolverAtras
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {},
                containerColor = MaterialTheme.colorScheme.tertiary
            ) {
                Icon(Icons.Outlined.Add, null, tint = MaterialTheme.colorScheme.onTertiary)
            }
        },
        bottomBar = { BarraNavegacionInferior(navController) }
    ) { padding ->
        if (productosFrecuentes.isEmpty()) {
            EstadoVacio(
                icono = Icons.Outlined.Inventory2,
                titulo = "Sin productos guardados",
                subtitulo = "Guardá tus productos frecuentes para cargarlos mas rapido",
                etiquetaAccion = "Agregar producto"
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    Text(
                        "Tocá un producto al crear una nueva compra para autocompletarlo.",
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(productosFrecuentes) { prod ->
                    ListItem(
                        headlineContent = {
                            Text(prod.nombre, fontWeight = FontWeight.SemiBold)
                        },
                        trailingContent = {
                            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                                Text(
                                    "$ ${formateador.format(prod.precio)}",
                                    style      = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color      = MaterialTheme.colorScheme.primary
                                )
                                IconButton(onClick = {}) {
                                    Icon(
                                        Icons.Outlined.DeleteOutline, null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Preview
@Composable
private fun Vista() = SuperAhorroTheme { PantallaMisProductos(rememberNavController(), {}) }
