package com.undef.superahorro.domain.model

/**
 * Tipo de cambio del dólar obtenido de la API.
 * valorPromedio es el que se usa para las conversiones ARS → USD.
 */
data class TipoCambio(
    val valorCompra: Double = 0.0,
    val valorVenta: Double = 0.0,
    val fuente: String = ""
) {
    // Usamos el promedio entre compra y venta para los cálculos
    val valorPromedio: Double get() = (valorCompra + valorVenta) / 2
}
