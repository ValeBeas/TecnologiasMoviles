package com.undef.superahorro.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.undef.superahorro.domain.model.Compra

/**
 * Tabla 'compras' en la base de datos local.
 */
@Entity(tableName = "compras")
data class EntidadCompra(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fecha: String,
    val hora: String,
    val supermercado: String,
    val total: Double,
    val cantidadProductos: Int = 0,
    val imagenTicket: String? = null
) {
    fun aDominio() = Compra(id, fecha, hora, supermercado, total, cantidadProductos, imagenTicket)
}

fun Compra.aEntidad() = EntidadCompra(id, fecha, hora, supermercado, total, cantidadProductos, imagenTicket)
