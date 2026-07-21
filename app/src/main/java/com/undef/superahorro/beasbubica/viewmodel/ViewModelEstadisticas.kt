package com.undef.superahorro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.superahorro.R
import com.undef.superahorro.domain.model.Compra
import android.content.Context
import com.undef.superahorro.data.repository.RepositorioComprasSupabase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar


/**
 * Los cuatro períodos de tiempo disponibles en las estadísticas.
 * Cada uno lleva el id del recurso de texto (no el texto), para que la UI lo
 * resuelva con stringResource(...) y quede internacionalizado.
 */
enum class PeriodoEstadisticas(val etiquetaRes: Int) {
    SEMANA(R.string.history_filter_week),
    MES(R.string.history_filter_month),
    TRES_MESES(R.string.history_filter_3months),
    ANIO(R.string.history_filter_year)
}

/**
 * Agrupa todas las métricas calculadas para el período seleccionado.
 */
data class ResumenEstadisticas(
    val totalGastado:          Double              = 0.0,
    val cantidadCompras:       Int                 = 0,
    val promedioPorCompra:     Double              = 0.0,
    val supermercadoFavorito:  String              = "",
    val gastosPorSupermercado: Map<String, Double> = emptyMap(),
    val gastosPorDia:          List<Pair<String, Double>> = emptyList()
)

/**
 * Calcula las métricas de estadísticas para el período seleccionado.
 * Filtra las compras de Room sin hacer llamadas de red.
 */
class ViewModelEstadisticas(private val contexto: Context) : ViewModel() {

    private val repositorio = RepositorioComprasSupabase(contexto)

    private val _estadoEstadisticas = MutableStateFlow(EstadoUi<ResumenEstadisticas>(cargando = true))
    val estadoEstadisticas: StateFlow<EstadoUi<ResumenEstadisticas>> = _estadoEstadisticas.asStateFlow()

    private val _periodoSeleccionado = MutableStateFlow(PeriodoEstadisticas.MES)
    val periodoSeleccionado: StateFlow<PeriodoEstadisticas> = _periodoSeleccionado.asStateFlow()

    private var todasLasCompras: List<Compra> = emptyList()

    init { cargarEstadisticas() }

    /** Cambia el filtro activo y recalcula los números sin volver a pedir datos. */
    fun cambiarPeriodo(periodo: PeriodoEstadisticas) {
        _periodoSeleccionado.value = periodo
        _estadoEstadisticas.value  = EstadoUi(datos = calcularEstadisticas(todasLasCompras, periodo))
    }

    fun cargarEstadisticas() {
        viewModelScope.launch {
            _estadoEstadisticas.value = EstadoUi(cargando = true)
            runCatching {
                repositorio.obtenerCompras()
                    .catch { e -> _estadoEstadisticas.value = EstadoUi(error = e.message) }
                    .collect { compras ->
                        todasLasCompras = compras
                        _estadoEstadisticas.value = EstadoUi(
                            datos = calcularEstadisticas(compras, _periodoSeleccionado.value)
                        )
                    }
            }.onFailure { e ->
                _estadoEstadisticas.value = EstadoUi(error = e.message)
            }
        }
    }

    // ── Parsing de fecha ─────────────────────────────────────────────────

    /** Convierte una fecha en texto (DD/MM/AAAA) a un objeto Calendar para comparar fechas. */
    private fun parsearFecha(fecha: String): Calendar? {
        val partes = fecha.split("/")
        if (partes.size != 3) return null
        val dia  = partes[0].toIntOrNull() ?: return null
        val mes  = partes[1].toIntOrNull() ?: return null
        val anio = partes[2].toIntOrNull() ?: return null
        return Calendar.getInstance().apply {
            set(anio, mes - 1, dia, 0, 0, 0)   // mes - 1: Calendar es 0-based
            set(Calendar.MILLISECOND, 0)
        }
    }

    // ── Funciones de filtro — una por período ────────────────────────────

    /** Devuelve true si la compra es de los últimos 7 días. */
    private fun esDeLaSemana(fecha: String): Boolean {
        val compraCalendar = parsearFecha(fecha) ?: return false
        val inicio = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -7)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
        }
        val fin = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59);      set(Calendar.MILLISECOND, 999)
        }
        return !compraCalendar.before(inicio) && !compraCalendar.after(fin)
    }

    /** Devuelve true si la compra es del mes actual. */
    private fun esDelMesActual(fecha: String): Boolean {
        val compraCalendar = parsearFecha(fecha) ?: return false
        val hoy = Calendar.getInstance()
        return compraCalendar.get(Calendar.YEAR)  == hoy.get(Calendar.YEAR) &&
               compraCalendar.get(Calendar.MONTH) == hoy.get(Calendar.MONTH)
    }

    /** Devuelve true si la compra es de los últimos 3 meses. */
    private fun esDeTresMeses(fecha: String): Boolean {
        val compraCalendar = parsearFecha(fecha) ?: return false
        val inicio = Calendar.getInstance().apply {
            add(Calendar.MONTH, -3)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
        }
        val fin = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59);      set(Calendar.MILLISECOND, 999)
        }
        return !compraCalendar.before(inicio) && !compraCalendar.after(fin)
    }

    /** Devuelve true si la compra es del año actual. */
    private fun esDelAnioActual(fecha: String): Boolean {
        val compraCalendar = parsearFecha(fecha) ?: return false
        return compraCalendar.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR)
    }

    // ── Cálculo de métricas ──────────────────────────────────────────────

    /**
 * Filtra las compras según el período elegido y calcula todos los números.
 * Elige qué función de filtro usar según el período con una referencia de función.
 */
    private fun calcularEstadisticas(
        compras: List<Compra>,
        periodo: PeriodoEstadisticas
    ): ResumenEstadisticas {
        if (compras.isEmpty()) return ResumenEstadisticas()

        // Selecciona la función de filtro correspondiente al período
        val filtro: (String) -> Boolean = when (periodo) {
            PeriodoEstadisticas.SEMANA      -> ::esDeLaSemana
            PeriodoEstadisticas.MES         -> ::esDelMesActual
            PeriodoEstadisticas.TRES_MESES  -> ::esDeTresMeses
            PeriodoEstadisticas.ANIO        -> ::esDelAnioActual
        }

        val filtradas = compras.filter { filtro(it.fecha) }
        if (filtradas.isEmpty()) return ResumenEstadisticas()

        val totalGastado      = filtradas.sumOf { it.total }
        val gastosPorSuper    = filtradas
            .groupBy  { it.supermercado }
            .mapValues { (_, lista) -> lista.sumOf { it.total } }
        val superFavorito     = gastosPorSuper.maxByOrNull { it.value }?.key ?: ""

        // Agrupa por día para el gráfico de barras (clave = "DD/MM" para mostrar en eje X)
        val gastosPorDia = filtradas
            .groupBy  { it.fecha.take(5) }
            .mapValues { (_, lista) -> lista.sumOf { it.total } }
            .entries
            .sortedBy { it.key }
            .map      { Pair(it.key, it.value) }

        return ResumenEstadisticas(
            totalGastado          = totalGastado,
            cantidadCompras       = filtradas.size,
            promedioPorCompra     = totalGastado / filtradas.size,
            supermercadoFavorito  = superFavorito,
            gastosPorSupermercado = gastosPorSuper,
            gastosPorDia          = gastosPorDia
        )
    }
}
