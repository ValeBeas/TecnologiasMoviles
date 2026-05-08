package com.undef.superahorro.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.undef.superahorro.data.repository.RepositorioComprasImpl
import com.undef.superahorro.ui.components.BarraNavegacionInferior
import com.undef.superahorro.ui.components.TarjetaCompra
import com.undef.superahorro.ui.navigation.Pantalla
import com.undef.superahorro.ui.theme.*
import com.undef.superahorro.viewmodel.ViewModelCompras
import java.text.NumberFormat
import java.util.Locale

/**
 * Pantalla principal que ve el usuario después de loguearse.
 */
@Composable
fun PantallaInicio(
    navController: NavController,
    alAgregarCompra: () -> Unit,
    alVerDetalleCompra: (Int) -> Unit,
    alAbrirConfiguracion: () -> Unit
) {
    val viewModel = remember { ViewModelCompras(RepositorioComprasImpl()) }
    val estadoCompras by viewModel.estadoCompras.collectAsState()
    val compras = estadoCompras.datos ?: emptyList()
    val formateador = NumberFormat.getNumberInstance(Locale("es", "AR"))
    val totalMes = compras.sumOf { it.total }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = alAgregarCompra,
                icon = { Icon(Icons.Outlined.Add, null) },
                text = { Text("Nueva compra") },
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor   = MaterialTheme.colorScheme.onTertiary
            )
        },
        bottomBar = { BarraNavegacionInferior(navController) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(padding)) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(IndigoDeep, IndigoMedium)))
                    .padding(24.dp)
            ) {
                Column {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("¡Hola, Valentina! 👋", style = MaterialTheme.typography.titleLarge, color = SurfaceWhite, fontWeight = FontWeight.Bold)
                            Text("Mayo 2026", style = MaterialTheme.typography.bodyMedium, color = SurfaceWhite.copy(alpha = 0.8f))
                        }
                        Row {
                            IconButton(onClick = alAbrirConfiguracion) { Icon(Icons.Outlined.Settings, null, tint = SurfaceWhite) }
                            IconButton(onClick = { navController.navigate(Pantalla.Perfil.ruta) }) {
                                Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(50)).background(SurfaceWhite.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                                    Text("VB", style = MaterialTheme.typography.labelMedium, color = SurfaceWhite, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SurfaceWhite.copy(alpha = 0.15f)), shape = RoundedCornerShape(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Gasto del mes", style = MaterialTheme.typography.labelMedium, color = SurfaceWhite.copy(alpha = 0.8f))
                                Text("$ ${formateador.format(totalMes)}", style = MaterialTheme.typography.headlineMedium, color = SurfaceWhite, fontWeight = FontWeight.ExtraBold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Compras", style = MaterialTheme.typography.labelMedium, color = SurfaceWhite.copy(alpha = 0.8f))
                                Text("${compras.size}", style = MaterialTheme.typography.headlineMedium, color = SurfaceWhite, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Accesos rápidos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AccesoRapido(Icons.Outlined.ShoppingCart, "Mis Compras", Modifier.weight(1f)) { navController.navigate(Pantalla.ListaCompras.ruta) }
                AccesoRapido(Icons.Outlined.History, "Historial", Modifier.weight(1f)) { navController.navigate(Pantalla.Historial.ruta) }
                AccesoRapido(Icons.Outlined.BarChart, "Estadísticas", Modifier.weight(1f)) { navController.navigate(Pantalla.Estadisticas.ruta) }
            }

            Spacer(Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Últimas compras", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TextButton(onClick = { navController.navigate(Pantalla.ListaCompras.ruta) }) { Text("Ver todas") }
            }
            compras.take(3).forEach { compra ->
                TarjetaCompra(compra = compra, alHacerClick = { alVerDetalleCompra(compra.id) })
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun AccesoRapido(icono: ImageVector, etiqueta: String, modifier: Modifier, alHacerClick: () -> Unit) {
    Card(modifier = modifier, onClick = alHacerClick, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icono, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(6.dp))
            Text(etiqueta, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Vista() = SuperAhorroTheme { PantallaInicio(rememberNavController(), {}, {}, {}) }
