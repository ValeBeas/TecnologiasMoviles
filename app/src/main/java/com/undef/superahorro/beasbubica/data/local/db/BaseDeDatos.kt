package com.undef.superahorro.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.undef.superahorro.data.local.db.dao.DaoCompra
import com.undef.superahorro.data.local.db.dao.DaoProducto
import com.undef.superahorro.data.local.db.entity.EntidadCompra
import com.undef.superahorro.data.local.db.entity.EntidadProducto

/**
 * Base de datos local de la app con Room.
 */
@Database(
    entities = [EntidadCompra::class, EntidadProducto::class],
    version = 1,
    exportSchema = false
)
abstract class BaseDeDatos : RoomDatabase() {
    abstract fun daoCompra(): DaoCompra
    abstract fun daoProducto(): DaoProducto

    companion object {
        const val NOMBRE_BD = "superahorro_db"
    }
}
