package com.undef.superahorro.ui.screens.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalContext
import com.undef.superahorro.R
import com.undef.superahorro.ui.components.*
import com.undef.superahorro.ui.theme.*
import com.undef.superahorro.viewmodel.PeriodoEstadisticas
import com.undef.superahorro.viewmodel.ViewModelEstadisticas
import com.undef.superahorro.viewmodel.ViewModelMoneda
import java.text.NumberFormat
import java.util.Locale

/**
 * Estadísticas de gastos con filtros por Semana, Mes, 3 Meses y Año.
 * Muestra total, cantidad de compras, promedio y distribución por supermercado.
 */
@Composable
fun PantallaEstadisticas(
    navController: NavController,
    viewModelMoneda: ViewModelMoneda,
    alVolverAtras: () -> Unit
) {
    val contextoEst = LocalContext.current
    val viewModel  = remember { ViewModelEstadisticas(contextoEst) }
    val estadoStats by viewModel.estadoEstadisticas.collectAsState()
    val periodo    by viewModel.periodoSeleccionado.collectAsState()
    val stats      = estadoStats.datos
    val formateador = NumberFormat.getNumberInstance(Locale("es", "AR"))

    Scaffold(
        topBar = { BarraSuperior(titulo = stringResource(R.string.stats_title), mostrarVolver = true, alVolverAtras = alVolverAtras) },
        bottomBar = { BarraNavegacionInferior(navController) }
    ) { padding ->
        if (estadoStats.cargando || stats == null) {
            IndicadorCarga(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 80.dp)) {

            // --- Chips de filtro de período ---
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PeriodoEstadisticas.entries.forEach { p ->
                        FilterChip(
                            selected = periodo == p,
                            onClick  = { viewModel.cambiarPeriodo(p) },
                            label    = { Text(stringResource(p.etiquetaRes)) }
                        )
                    }
                }
            }

            // --- Mensaje si no hay compras en el período ---
            if (stats.cantidadCompras == 0) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.stats_no_data_period), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                return@LazyColumn
            }

            // --- Tarjetas de métricas ---
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TarjetaEstadistica(Icons.Outlined.AttachMoney, stringResource(R.string.home_total_spent),      viewModelMoneda.convertir(stats.totalGastado),      Modifier.weight(1f))
                    TarjetaEstadistica(Icons.Outlined.ShoppingCart, stringResource(R.string.stats_total_purchases), "${stats.cantidadCompras}",                         Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TarjetaEstadistica(Icons.Outlined.TrendingDown, stringResource(R.string.stats_avg_per_purchase), viewModelMoneda.convertir(stats.promedioPorCompra), Modifier.weight(1f))
                    TarjetaEstadistica(Icons.Outlined.Store,        stringResource(R.string.stats_favorite_market),  stats.supermercadoFavorito.take(9),                Modifier.weight(1f))
                }
            }

            // --- Gráfico de barras por día ---
            item {
                Spacer(Modifier.height(20.dp))
                Text(stringResource(R.string.stats_daily_chart), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(12.dp))
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val maxVal = stats.gastosPorDia.maxOfOrNull { it.second } ?: 1.0
                        if (stats.gastosPorDia.isEmpty()) {
                            Text(stringResource(R.string.stats_no_data), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            stats.gastosPorDia.take(7).forEach { (dia, monto) ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(dia, style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(52.dp))
                                    Box(modifier = Modifier.weight(1f).height(24.dp).clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                                        Box(modifier = Modifier
                                            .fillMaxWidth((monto / maxVal).toFloat())
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(IndigoMedium))
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text(viewModelMoneda.convertir(monto), style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(72.dp), maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }

            // --- Distribución por supermercado ---
            item {
                Spacer(Modifier.height(20.dp))
                Text(stringResource(R.string.stats_by_supermarket), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(12.dp))
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val total = stats.gastosPorSupermercado.values.sum()
                        stats.gastosPorSupermercado.entries.sortedByDescending { it.value }.forEach { (super_, monto) ->
                            val pct   = if (total > 0) (monto / total * 100).toInt() else 0
                            val color = colorSupermercado(super_)
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(color))
                                    Spacer(Modifier.width(8.dp))
                                    Text(super_, style = MaterialTheme.typography.bodyMedium)
                                }
                                Text(stringResource(R.string.stats_pct_amount, pct, viewModelMoneda.convertir(monto)), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = color)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun Vista() = SuperAhorroTheme { Text("Preview - PantallaEstadisticas") }
