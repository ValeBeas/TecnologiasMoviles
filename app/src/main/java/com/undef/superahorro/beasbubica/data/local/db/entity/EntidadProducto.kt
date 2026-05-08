package com.undef.superahorro.data.local.db.entity

import androidx.room.*
import com.undef.superahorro.domain.model.Producto

/**
 * Tabla 'productos' en la base de datos local.
 */
@Entity(
    tableName = "productos",
    foreignKeys = [ForeignKey(
        entity = EntidadCompra::class,
        parentColumns = ["id"],
        childColumns = ["compraId"],
        onDelete = ForeignKey.CASCADE  // Si se borra la compra, se borran sus productos
    )]
)
data class EntidadProducto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val compraId: Int,
    val codigo: String,
    val nombre: String,
    val descripcion: String,
    val cantidad: Int,
    val precio: Double
) {
    fun aDominio() = Producto(id, compraId, codigo, nombre, descripcion, cantidad, precio)
}

fun Producto.aEntidad() = EntidadProducto(id, compraId, codigo, nombre, descripcion, cantidad, precio)
