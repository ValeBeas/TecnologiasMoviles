package com.undef.superahorro.ui.screens.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import com.undef.superahorro.R
import com.undef.superahorro.domain.model.Compra
import com.undef.superahorro.ui.components.*
import com.undef.superahorro.ui.theme.SuperAhorroTheme
import com.undef.superahorro.viewmodel.ViewModelMoneda
import com.undef.superahorro.viewmodel.ViewModelCompras

@Composable
/**
 * Historial de compras agrupado por mes y año.
 * Muestra el total gastado por mes en la moneda activa.
 */
fun PantallaHistorial(
    navController: NavController,
    viewModelMoneda: ViewModelMoneda,
    alVerDetalle: (String) -> Unit,
    alVolverAtras: () -> Unit
) {
    val contexto = LocalContext.current
    val viewModel = remember { ViewModelCompras(contexto) }
    val estado    by viewModel.estadoCompras.collectAsState()
    val compras   = estado.datos ?: emptyList()

    // Recursos leídos en contexto @Composable (no se pueden llamar dentro de los lambdas de abajo)
    val nombresMeses = stringArrayResource(R.array.month_names)
    val textoSinFecha = stringResource(R.string.history_no_date)

    // Agrupa las compras por "MM/AAAA" — ya vienen ordenadas por fecha descendente del repositorio
    val agrupadasPorMes: Map<String, List<Compra>> = compras.groupBy { compra ->
        val partes = compra.fecha.split("/")
        if (partes.size >= 3) "${partes[1]}/${partes[2]}" else textoSinFecha
    }

    Scaffold(
        topBar = { BarraSuperior(titulo = stringResource(R.string.history_title), mostrarVolver = true, alVolverAtras = alVolverAtras) },
        bottomBar = { BarraNavegacionInferior(navController) }
    ) { padding ->
        if (estado.cargando) { IndicadorCarga(modifier = Modifier.padding(padding)); return@Scaffold }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 80.dp)) {
            // Ordena los grupos de mes de más reciente a más antiguo
            agrupadasPorMes.entries.sortedByDescending { it.key }.forEach { (claveMes, comprasDelMes) ->
                val partes     = claveMes.split("/")
                val indiceMes  = partes.getOrNull(0)?.toIntOrNull()?.minus(1)
                val nombreMes  = if (partes.size >= 2 && indiceMes != null && indiceMes in 0..11)
                    "${nombresMeses[indiceMes]} ${partes[1]}" else claveMes
                val totalMes  = comprasDelMes.sumOf { it.total }

                // --- Encabezado de mes con total ---
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(nombreMes, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(viewModelMoneda.convertir(totalMes), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }

                // --- Tarjetas de compras del mes, ordenadas por fecha y hora descendente ---
                items(comprasDelMes.sortedByDescending { "${it.fecha} ${it.hora}" }) { compra ->
                    TarjetaCompra(compra = compra, viewModelMoneda = viewModelMoneda, alHacerClick = { alVerDetalle(compra.idSupabase) })
                }
            }
        }
    }
}

@Preview
@Composable
private fun Vista() = SuperAhorroTheme { Text("Preview - PantallaHistorial") }
