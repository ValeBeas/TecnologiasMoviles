package com.undef.superahorro.data.local.db.dao

import androidx.room.*
import com.undef.superahorro.data.local.db.entity.EntidadCompra
import kotlinx.coroutines.flow.Flow

// DAO con operaciones CRUD sobre la tabla 'compras'.
@Dao
/**
 * Operaciones sobre la tabla 'compras' en Room.
 * Incluye búsqueda por id local y por idSupabase para la sincronización.
 */
interface DaoCompra {
    @Query("SELECT * FROM compras ORDER BY id DESC")
    fun obtenerTodas(): Flow<List<EntidadCompra>>

    @Query("SELECT * FROM compras ORDER BY id DESC")
    suspend fun obtenerTodasSuspend(): List<EntidadCompra>

    @Query("SELECT * FROM compras WHERE id = :id")
    suspend fun obtenerPorId(id: Int): EntidadCompra?

    @Query("SELECT * FROM compras WHERE idSupabase = :idSupabase")
    suspend fun obtenerPorIdSupabase(idSupabase: String): EntidadCompra?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(compra: EntidadCompra): Long

    @Update
    suspend fun actualizar(compra: EntidadCompra)

    @Delete
    suspend fun eliminar(compra: EntidadCompra)

    @Query("DELETE FROM compras")
    suspend fun eliminarTodas()

    // Actualiza el idSupabase después de sincronizar con la nube
    @Query("UPDATE compras SET idSupabase = :idSupabase, sincronizado = 1 WHERE id = :id")
    suspend fun actualizarIdSupabase(id: Int, idSupabase: String)
}
