package com.undef.superahorro.data.network.dto

import com.google.gson.annotations.SerializedName
import com.undef.superahorro.domain.model.TipoCambio

/**
 * Mapea la respuesta de bluelytics.com.ar/v2/latest.
 * Contiene los valores del dólar oficial y blue (compra, venta, promedio).
 */
data class DtoDolar(
    val oficial: DtoValorDolar? = null,
    val blue: DtoValorDolar? = null,
    @SerializedName("oficial_euro") val oficialEuro: DtoValorDolar? = null
) {
    fun aDominio(): TipoCambio {
        val dolarBlue = blue ?: oficial
        return TipoCambio(
            valorCompra = dolarBlue?.valueAvg ?: 0.0,
            valorVenta  = dolarBlue?.valueSell ?: 0.0,
            fuente      = "Bluelytics"
        )
    }
}

data class DtoValorDolar(
    @SerializedName("value_avg")  val valueAvg: Double = 0.0,
    @SerializedName("value_buy")  val valueBuy: Double = 0.0,
    @SerializedName("value_sell") val valueSell: Double = 0.0
)
