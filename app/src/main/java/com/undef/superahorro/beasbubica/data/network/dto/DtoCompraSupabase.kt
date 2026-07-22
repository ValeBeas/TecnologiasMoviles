package com.undef.superahorro.data.network.dto

import com.undef.superahorro.domain.model.Compra
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
/**
 * Mapea una fila de la tabla 'compras' de Supabase.
 * Se usa tanto para leer compras como para insertarlas y recibirlas con su UUID.
 */
data class DtoCompraSupabase(
    val id: String = "",
    @SerialName("usuario_id") val usuarioId: String = "",
    val fecha: String = "",
    val hora: String = "",
    val supermercado: String = "",
    val total: Double = 0.0,
    val descuento: Double = 0.0,
    @SerialName("imagen_ticket") val imagenTicket: String? = null,
    @SerialName("created_at") val creadoEn: String? = null
) {
    // Convierte el DTO al modelo de dominio
    fun aDominio(cantidadProductos: Int = 0) = Compra(
        id = id.hashCode(),
        idSupabase = id,
        fecha = fecha,
        hora = hora,
        supermercado = supermercado,
        total = total,
        descuento = descuento,
        cantidadProductos = cantidadProductos,
        imagenTicket = imagenTicket
    )
}
