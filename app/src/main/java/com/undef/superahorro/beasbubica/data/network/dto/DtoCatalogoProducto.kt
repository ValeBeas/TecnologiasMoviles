package com.undef.superahorro.data.network.dto

import com.undef.superahorro.domain.model.Producto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
/**
 * Mapea una fila de la tabla 'catalogo_productos' de Supabase.
 * Representa un producto frecuente del catálogo personal del usuario.
 */
data class DtoCatalogoProducto(
    val id: String = "",
    @SerialName("usuario_id") val usuarioId: String = "",
    val nombre: String = "",
    val precio: Double = 0.0,
    @SerialName("codigo_barras") val codigoBarras: String? = null,
    @SerialName("created_at") val creadoEn: String? = null
) {
    fun aDominio() = Producto(
        idSupabase = id,
        nombre     = nombre,
        precio     = precio,
        codigo     = codigoBarras ?: ""
    )
}
