package com.undef.superahorro.data.local.db.dao

import androidx.room.*
import com.undef.superahorro.data.local.db.entity.EntidadCompra
import kotlinx.coroutines.flow.Flow

/**
 * Operaciones sobre la tabla 'compras': obtener todas, obtener por ID, insertar, actualizar y borrar.
 * Devuelve Flow para que la pantalla se actualice sola cuando cambian los datos.
 */
@Dao
interface DaoCompra {
    @Query("SELECT * FROM compras ORDER BY fecha DESC, hora DESC")
    fun obtenerTodas(): Flow<List<EntidadCompra>>

    @Query("SELECT * FROM compras WHERE id = :id")
    suspend fun obtenerPorId(id: Int): EntidadCompra?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(compra: EntidadCompra): Long

    @Update
    suspend fun actualizar(compra: EntidadCompra)

    @Delete
    suspend fun eliminar(compra: EntidadCompra)
}
