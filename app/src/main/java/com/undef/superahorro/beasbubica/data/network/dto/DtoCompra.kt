package com.undef.superahorro.data.network.dto

import com.google.gson.annotations.SerializedName
import com.undef.superahorro.domain.model.Compra

/**
 * DTO de compra para la API REST genérica (ServicioApi).
 * Estructura base para intercambiar datos de compras con un servidor externo.
 */
data class DtoCompra(
    @SerializedName("id")            val id: Int,
    @SerializedName("date")          val fecha: String,
    @SerializedName("time")          val hora: String,
    @SerializedName("supermarket")   val supermercado: String,
    @SerializedName("total")         val total: Double,
    @SerializedName("product_count") val cantidadProductos: Int
) {
    fun aDominio() = Compra(id = id, idSupabase = "", fecha = fecha, hora = hora, supermercado = supermercado, total = total, cantidadProductos = cantidadProductos)
}
