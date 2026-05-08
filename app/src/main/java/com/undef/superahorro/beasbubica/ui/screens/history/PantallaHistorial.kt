package com.undef.superahorro.ui.screens.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.undef.superahorro.data.repository.RepositorioComprasImpl
import com.undef.superahorro.domain.model.Compra
import com.undef.superahorro.ui.components.*
import com.undef.superahorro.ui.theme.SuperAhorroTheme
import com.undef.superahorro.viewmodel.ViewModelCompras
import java.text.NumberFormat
import java.util.Locale

/**
 * Muestra el historial completo de compras agrupado por mes y año.
 * No tiene filtros. Las compras más recientes aparecen primero.
 */
@Composable
fun PantallaHistorial(
    navController: NavController,
    alVerDetalle: (Int) -> Unit,
    alVolverAtras: () -> Unit
) {
    val viewModel = remember { ViewModelCompras(RepositorioComprasImpl()) }
    val estado    by viewModel.estadoCompras.collectAsState()
    val compras   = estado.datos ?: emptyList()
    val formateador = NumberFormat.getNumberInstance(Locale("es", "AR"))

    // Agrupa las compras por "MM/AAAA" — ya vienen ordenadas por fecha descendente del repositorio
    val agrupadasPorMes: Map<String, List<Compra>> = compras.groupBy { compra ->
        val partes = compra.fecha.split("/")
        if (partes.size >= 3) "${partes[1]}/${partes[2]}" else "Sin fecha"
    }

    // Nombres de meses en español para mostrar en los encabezados
    val nombresMeses = mapOf(
        "01" to "Enero", "02" to "Febrero", "03" to "Marzo",    "04" to "Abril",
        "05" to "Mayo",  "06" to "Junio",   "07" to "Julio",    "08" to "Agosto",
        "09" to "Septiembre", "10" to "Octubre", "11" to "Noviembre", "12" to "Diciembre"
    )

    Scaffold(
        topBar = { BarraSuperior(titulo = "Historial", mostrarVolver = true, alVolverAtras = alVolverAtras) },
        bottomBar = { BarraNavegacionInferior(navController) }
    ) { padding ->
        if (estado.cargando) { IndicadorCarga(modifier = Modifier.padding(padding)); return@Scaffold }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 80.dp)) {
            // Ordena los grupos de mes de más reciente a más antiguo
            agrupadasPorMes.entries.sortedByDescending { it.key }.forEach { (claveMes, comprasDelMes) ->
                val partes    = claveMes.split("/")
                val nombreMes = if (partes.size >= 2) "${nombresMeses[partes[0]] ?: partes[0]} ${partes[1]}" else claveMes
                val totalMes  = comprasDelMes.sumOf { it.total }

                // --- Encabezado de mes con total ---
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(nombreMes, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("$ ${formateador.format(totalMes)}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }

                // --- Tarjetas de compras del mes, ordenadas por fecha y hora descendente ---
                items(comprasDelMes.sortedByDescending { "${it.fecha} ${it.hora}" }) { compra ->
                    TarjetaCompra(compra = compra, alHacerClick = { alVerDetalle(compra.id) })
                }
            }
        }
    }
}

@Preview
@Composable
private fun Vista() = SuperAhorroTheme { PantallaHistorial(rememberNavController(), {}, {}) }
