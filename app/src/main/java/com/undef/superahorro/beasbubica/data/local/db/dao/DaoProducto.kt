package com.undef.superahorro.data.local.db.dao

import androidx.room.*
import com.undef.superahorro.data.local.db.entity.EntidadProducto
import kotlinx.coroutines.flow.Flow

/**
 * Operaciones sobre la tabla 'productos': obtener por compra, insertar, actualizar y borrar.
 */
@Dao
interface DaoProducto {
    @Query("SELECT * FROM productos WHERE compraId = :compraId")
    fun obtenerPorCompra(compraId: Int): Flow<List<EntidadProducto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(producto: EntidadProducto)

    @Update
    suspend fun actualizar(producto: EntidadProducto)

    @Delete
    suspend fun eliminar(producto: EntidadProducto)
}
