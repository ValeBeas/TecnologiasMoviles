package com.undef.superahorro.data.network.dto

import com.undef.superahorro.domain.model.Producto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
/**
 * Mapea una fila de la tabla 'productos' de Supabase.
 * Se asocia a su compra mediante compraId (UUID de Supabase).
 */
data class DtoProductoSupabase(
    val id: String = "",
    @SerialName("compra_id") val compraId: String = "",
    val nombre: String = "",
    val cantidad: Int = 1,
    val precio: Double = 0.0,
    @SerialName("codigo_barras") val codigoBarras: String? = null
) {
    fun aDominio() = Producto(
        id = id.hashCode(),
        compraId = compraId.hashCode(),
        idSupabase = id,
        idCompraSupabase = compraId,
        nombre = nombre,
        cantidad = cantidad,
        precio = precio,
        codigo = codigoBarras ?: ""
    )
}
