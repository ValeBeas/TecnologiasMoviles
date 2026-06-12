package com.undef.superahorro.data.local.db.entity

import androidx.room.*
import com.undef.superahorro.domain.model.Producto

/**
 * Entidad Room para la tabla 'productos'.
 * Se borra en cascada si se elimina la compra asociada. idSupabase para sincronización.
 */
@Entity(
    tableName = "productos",
    foreignKeys = [ForeignKey(
        entity = EntidadCompra::class,
        parentColumns = ["id"],
        childColumns = ["compraId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("compraId")]
)
data class EntidadProducto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val compraId: Int,
    val idSupabase: String = "",
    val idCompraSupabase: String = "",
    val codigo: String = "",
    val nombre: String,
    val cantidad: Int,
    val precio: Double
) {
    fun aDominio() = Producto(
        id = id,
        compraId = compraId,
        idSupabase = idSupabase,
        idCompraSupabase = idCompraSupabase,
        codigo = codigo,
        nombre = nombre,
        cantidad = cantidad,
        precio = precio
    )
}

fun Producto.aEntidad() = EntidadProducto(
    id = id,
    compraId = compraId,
    idSupabase = idSupabase,
    idCompraSupabase = idCompraSupabase,
    codigo = codigo,
    nombre = nombre,
    cantidad = cantidad,
    precio = precio
)
