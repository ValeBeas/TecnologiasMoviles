package com.undef.superahorro.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.undef.superahorro.domain.model.Compra

/**
 * Entidad Room que representa la tabla 'compras'.
 * idSupabase guarda el UUID de Supabase para sincronización.
 */
@Entity(tableName = "compras")
data class EntidadCompra(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val idSupabase: String = "",
    val fecha: String,
    val hora: String,
    val supermercado: String,
    val total: Double,
    val cantidadProductos: Int = 0,
    val imagenTicket: String? = null,
    val sincronizado: Boolean = false
) {
    fun aDominio() = Compra(
        id = id,
        idSupabase = idSupabase,
        fecha = fecha,
        hora = hora,
        supermercado = supermercado,
        total = total,
        cantidadProductos = cantidadProductos,
        imagenTicket = imagenTicket
    )
}

fun Compra.aEntidad() = EntidadCompra(
    id = id,
    idSupabase = idSupabase,
    fecha = fecha,
    hora = hora,
    supermercado = supermercado,
    total = total,
    cantidadProductos = cantidadProductos,
    imagenTicket = imagenTicket
)
