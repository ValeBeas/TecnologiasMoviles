package com.undef.superahorro.data.local.db.dao

import androidx.room.*
import com.undef.superahorro.data.local.db.entity.EntidadProducto
import kotlinx.coroutines.flow.Flow

@Dao
/**
 * Operaciones sobre la tabla 'productos' en Room.
 * Los productos se obtienen filtrando por el id local de su compra.
 */
interface DaoProducto {
    @Query("SELECT * FROM productos WHERE compraId = :compraId")
    fun obtenerPorCompra(compraId: Int): Flow<List<EntidadProducto>>

    @Query("SELECT * FROM productos WHERE compraId = :compraId")
    suspend fun obtenerPorCompraSuspend(compraId: Int): List<EntidadProducto>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(producto: EntidadProducto): Long

    @Update
    suspend fun actualizar(producto: EntidadProducto)

    @Delete
    suspend fun eliminar(producto: EntidadProducto)

    @Query("DELETE FROM productos WHERE compraId = :compraId")
    suspend fun eliminarPorCompra(compraId: Int)
}
