package com.undef.superahorro.data.network.dto

import com.google.gson.annotations.SerializedName
import com.undef.superahorro.domain.model.Compra

/**
 * Clase que recibe los datos de compras que manda la API en formato JSON.
 * Tiene una función aDominio() para convertirlo al modelo que usa la app.
 */
data class DtoCompra(
    @SerializedName("id")            val id: Int,
    @SerializedName("date")          val fecha: String,
    @SerializedName("time")          val hora: String,
    @SerializedName("supermarket")   val supermercado: String,
    @SerializedName("total")         val total: Double,
    @SerializedName("product_count") val cantidadProductos: Int
) {
    fun aDominio() = Compra(id, fecha, hora, supermercado, total, cantidadProductos)
}
