package com.undef.superahorro.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.superahorro.data.local.datastore.PreferenciasUsuario
import com.undef.superahorro.data.local.datastore.dataStore
import com.undef.superahorro.data.network.ClienteDolar
import com.undef.superahorro.domain.model.TipoCambio
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

/**
 * Maneja la conversión ARS/USD usando la cotización de la API del dólar.
 * Se crea una vez en MainActivity y se comparte con todas las pantallas.
 */
class ViewModelMoneda(private val contexto: Context) : ViewModel() {

    private val preferencias = PreferenciasUsuario(contexto)
    private val formateadorARS = NumberFormat.getNumberInstance(Locale("es", "AR"))
    private val formateadorUSD = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

    // Moneda activa — se lee de DataStore al iniciar
    private val _monedaActiva = MutableStateFlow("ARS")
    val monedaActiva: StateFlow<String> = _monedaActiva.asStateFlow()

    // Tipo de cambio obtenido de la API
    private val _tipoCambio = MutableStateFlow<TipoCambio?>(null)
    val tipoCambio: StateFlow<TipoCambio?> = _tipoCambio.asStateFlow()

    private val _cargandoCotizacion = MutableStateFlow(false)
    val cargandoCotizacion: StateFlow<Boolean> = _cargandoCotizacion.asStateFlow()

    init {
        // Leer la moneda guardada en DataStore
        viewModelScope.launch {
            preferencias.flujoMoneda.collect { moneda ->
                _monedaActiva.value = moneda
            }
        }
        // Cargar el tipo de cambio al iniciar
        cargarTipoCambio()
    }

    // Llama a la API del dólar una sola vez
    fun cargarTipoCambio() {
        viewModelScope.launch {
            _cargandoCotizacion.value = true
            runCatching {
                val dto = ClienteDolar.servicio.obtenerCotizacion()
                _tipoCambio.value = dto.aDominio()
            }.onFailure {
                // Si falla la API, se mantiene en ARS sin conversión
                _tipoCambio.value = null
            }
            _cargandoCotizacion.value = false
        }
    }

    // Cambia la moneda activa y la persiste en DataStore
    fun cambiarMoneda(moneda: String) {
        viewModelScope.launch {
            _monedaActiva.value = moneda
            preferencias.guardarMoneda(moneda)
        }
    }

    /**
     * Convierte un monto ARS al formato de la moneda activa.
     * Devuelve el string formateado listo para mostrar en pantalla.
     *
     * @param montoARS precio en pesos argentinos
     * @return "$ 1.250,00" en ARS o "U$D 1.02" en USD
     */
    fun convertir(montoARS: Double): String {
        return if (_monedaActiva.value == "USD" && _tipoCambio.value != null) {
            val valorUSD = montoARS / _tipoCambio.value!!.valorPromedio
            "U\$D ${formateadorUSD.format(valorUSD)}"
        } else {
            "$ ${formateadorARS.format(montoARS)}"
        }
    }

    // Devuelve solo el número convertido (para cálculos)
    fun convertirNumero(montoARS: Double): Double {
        return if (_monedaActiva.value == "USD" && _tipoCambio.value != null) {
            montoARS / _tipoCambio.value!!.valorPromedio
        } else {
            montoARS
        }
    }
}
