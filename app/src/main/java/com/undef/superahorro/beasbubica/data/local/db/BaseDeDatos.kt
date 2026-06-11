package com.undef.superahorro.data.local.db

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.undef.superahorro.data.local.db.dao.DaoCompra
import com.undef.superahorro.data.local.db.dao.DaoProducto
import com.undef.superahorro.data.local.db.entity.EntidadCompra
import com.undef.superahorro.data.local.db.entity.EntidadProducto

// Base de datos local de la app usando Room.
// versión 2: se agregaron idSupabase, idCompraSupabase y sincronizado
/**
 * Base de datos local Room con las tablas de compras y productos.
 * Versión 2 — incluye campos idSupabase para sincronizar con Supabase.
 */
@Database(
    entities = [EntidadCompra::class, EntidadProducto::class],
    version = 2,
    exportSchema = false
)
abstract class BaseDeDatos : RoomDatabase() {
    abstract fun daoCompra(): DaoCompra
    abstract fun daoProducto(): DaoProducto

    companion object {
        const val NOMBRE_BD = "superahorro_db"

        @Volatile
        private var INSTANCIA: BaseDeDatos? = null

        // Migración de versión 1 a 2: nuevas columnas para Supabase
        val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE compras ADD COLUMN idSupabase TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE compras ADD COLUMN sincronizado INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE productos ADD COLUMN idSupabase TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE productos ADD COLUMN idCompraSupabase TEXT NOT NULL DEFAULT ''")
            }
        }

        fun obtenerInstancia(contexto: Context): BaseDeDatos {
            return INSTANCIA ?: synchronized(this) {
                Room.databaseBuilder(
                    contexto.applicationContext,
                    BaseDeDatos::class.java,
                    NOMBRE_BD
                )
                    .addMigrations(MIGRACION_1_2)
                    .build()
                    .also { INSTANCIA = it }
            }
        }
    }
}
