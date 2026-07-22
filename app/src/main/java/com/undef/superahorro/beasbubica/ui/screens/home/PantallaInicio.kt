package com.undef.superahorro.ui.screens.home

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.undef.superahorro.R
import com.undef.superahorro.ui.components.BarraNavegacionInferior
import com.undef.superahorro.ui.components.TarjetaCompra
import com.undef.superahorro.ui.navigation.Pantalla
import com.undef.superahorro.ui.theme.*
import com.undef.superahorro.viewmodel.ViewModelCompras
import com.undef.superahorro.viewmodel.ViewModelMoneda

@Composable
/**
 * Dashboard principal después del login.
 * Muestra el total gastado en el mes, accesos rápidos y las últimas 3 compras.
 */
fun PantallaInicio(
    navController: NavController,
    viewModelMoneda: ViewModelMoneda,
    alAgregarCompra: () -> Unit,
    alVerDetalleCompra: (String) -> Unit,
    alAbrirConfiguracion: () -> Unit
) {
    val contexto = LocalContext.current
    val viewModel = remember { ViewModelCompras(contexto) }
    val estadoCompras by viewModel.estadoCompras.collectAsState()

    // No hace falta "recargar" acá: el ViewModel ya colecta el Flow de Room en su init,
    // y Room re-emite solo cada vez que cambian los datos (por eso la lista se actualiza sola).
    val compras = estadoCompras.datos ?: emptyList()
    val monedaActiva by viewModelMoneda.monedaActiva.collectAsState()

    val totalMes = compras.sumOf { it.total }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = alAgregarCompra,
                icon = { Icon(Icons.Outlined.Add, null) },
                text = { Text(stringResource(R.string.home_new_purchase)) },
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            )
        },
        bottomBar = { BarraNavegacionInferior(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {
            // Header con gradiente
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(IndigoDeep, IndigoMedium)))
                    .padding(24.dp)
            ) {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                stringResource(R.string.home_greeting_simple),
                                style = MaterialTheme.typography.titleLarge,
                                color = SurfaceWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                stringResource(R.string.home_month_expenses),
                                style = MaterialTheme.typography.bodyMedium,
                                color = SurfaceWhite.copy(alpha = 0.8f)
                            )
                        }
                        IconButton(onClick = alAbrirConfiguracion) {
                            Icon(Icons.Outlined.Settings, null, tint = SurfaceWhite)
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    stringResource(R.string.home_total_spent),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = SurfaceWhite.copy(alpha = 0.8f)
                                )
                                Text(
                                    viewModelMoneda.convertir(totalMes),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = SurfaceWhite,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    stringResource(R.string.stats_total_purchases),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = SurfaceWhite.copy(alpha = 0.8f)
                                )
                                Text(
                                    "${compras.size}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = SurfaceWhite,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }

            // Accesos rápidos
            Spacer(Modifier.height(20.dp))
            Text(
                stringResource(R.string.home_quick_access),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AccesoRapido(Icons.Outlined.ShoppingCart, stringResource(R.string.purchase_list), Modifier.weight(1f)) {
                    navController.navigate(Pantalla.ListaCompras.ruta)
                }
                AccesoRapido(Icons.Outlined.History, stringResource(R.string.nav_history), Modifier.weight(1f)) {
                    navController.navigate(Pantalla.Historial.ruta)
                }
                AccesoRapido(Icons.Outlined.BarChart, stringResource(R.string.nav_statistics), Modifier.weight(1f)) {
                    navController.navigate(Pantalla.Estadisticas.ruta)
                }
            }

            // Últimas 3 compras
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.home_latest_purchases), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TextButton(onClick = { navController.navigate(Pantalla.ListaCompras.ruta) }) {
                    Text(stringResource(R.string.action_see_all))
                }
            }
            compras.take(3).forEach { compra ->
                TarjetaCompra(
                    compra = compra,
                    viewModelMoneda = viewModelMoneda,
                    alHacerClick = { alVerDetalleCompra(compra.idSupabase) }
                )
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}

/**
 * Botón de acceso rápido a una sección de la app.
 * Se usa en la fila de accesos del Home.
 */
@Composable
private fun AccesoRapido(
    icono: ImageVector,
    etiqueta: String,
    modifier: Modifier,
    alHacerClick: () -> Unit
) {
    Card(
        modifier = modifier,
        onClick = alHacerClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icono, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(6.dp))
            Text(etiqueta, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }
    }
}
