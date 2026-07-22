package com.undef.superahorro.data.repository

import android.content.Context
import android.net.Uri
import com.undef.superahorro.data.local.db.BaseDeDatos
import com.undef.superahorro.data.local.db.entity.EntidadProducto
import com.undef.superahorro.data.local.db.entity.aEntidad
import com.undef.superahorro.data.network.ClienteSupabase
import com.undef.superahorro.data.network.dto.DtoCompraSupabase
import com.undef.superahorro.data.network.dto.DtoProductoSupabase
import com.undef.superahorro.domain.model.Compra
import com.undef.superahorro.domain.model.Producto
import com.undef.superahorro.domain.model.ProductoEnCompra
import com.undef.superahorro.domain.repository.RepositorioCompras
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Repositorio principal de compras y productos.
 * Lógica uniforme: las pantallas leen de Room, los cambios van a Supabase y Room.
 * Al abrir la app sincronizarCompleto() recarga Room desde Supabase.
 */
class RepositorioComprasSupabase(private val contexto: Context) : RepositorioCompras {

    private val bd         = BaseDeDatos.obtenerInstancia(contexto)
    private val daoCompra  = bd.daoCompra()
    private val daoProducto = bd.daoProducto()
    private val supabase   = ClienteSupabase.cliente

    private val usuarioId: String
        get() = supabase.auth.currentUserOrNull()?.id ?: ""

    // SINCRONIZACIÓN

    /**
     * Sincronización completa al abrir la app.
     * Limpia Room y recarga TODO desde Supabase — compras y sus productos.
     * Garantiza que Room siempre refleja el estado actual de Supabase.
     */
    suspend fun sincronizarCompleto() {
        runCatching {
            // Limpiar Room (fuente de verdad es Supabase)
            daoCompra.eliminarTodas()

            // Traer todas las compras del usuario
            val comprasSupabase = supabase.postgrest
                .from("compras")
                .select { filter { eq("usuario_id", usuarioId) } }
                .decodeList<DtoCompraSupabase>()

            comprasSupabase.forEach { dtoCompra ->
                // Traer los productos de cada compra
                val productosSupabase = supabase.postgrest
                    .from("productos")
                    .select { filter { eq("compra_id", dtoCompra.id) } }
                    .decodeList<DtoProductoSupabase>()

                // Guardar compra en Room con idSupabase correcto
                val idLocal = daoCompra.insertar(
                    dtoCompra.aDominio(productosSupabase.size).aEntidad()
                ).toInt()

                // Guardar productos en Room asociados a la compra
                productosSupabase.forEach { dto ->
                    daoProducto.insertar(EntidadProducto(
                        compraId         = idLocal,
                        idSupabase       = dto.id,
                        idCompraSupabase = dto.compraId,
                        codigo           = dto.codigoBarras ?: "",
                        nombre           = dto.nombre,
                        cantidad         = dto.cantidad,
                        precio           = dto.precio
                    ))
                }
            }
        }
    }

    // LEER

    /** Las pantallas leen de Room (Flow reactivo — sin llamadas a red). */
    override fun obtenerCompras(): Flow<List<Compra>> =
        daoCompra.obtenerTodas().map { lista -> lista.map { it.aDominio() } }

    /** Lee una compra por idSupabase desde Room. */
    override suspend fun obtenerCompraPorId(id: String): Compra? =
        daoCompra.obtenerPorIdSupabase(id)?.aDominio()

    /**
     * Lee los productos de una compra desde Room.
     * Si Room no los tiene todavía, los trae de Supabase, los cachea y los devuelve.
     */
    override fun obtenerProductosPorCompra(compraId: String): Flow<List<Producto>> = flow {
        val compraLocal = daoCompra.obtenerPorIdSupabase(compraId)

        if (compraLocal != null) {
            val productosEnRoom = daoProducto.obtenerPorCompraSuspend(compraLocal.id)

            if (productosEnRoom.isEmpty()) {
                // No están en caché — traer de Supabase y cachear en Room
                runCatching {
                    supabase.postgrest
                        .from("productos")
                        .select { filter { eq("compra_id", compraId) } }
                        .decodeList<DtoProductoSupabase>()
                }.getOrDefault(emptyList()).forEach { dto ->
                    daoProducto.insertar(EntidadProducto(
                        compraId         = compraLocal.id,
                        idSupabase       = dto.id,
                        idCompraSupabase = dto.compraId,
                        codigo           = dto.codigoBarras ?: "",
                        nombre           = dto.nombre,
                        cantidad         = dto.cantidad,
                        precio           = dto.precio
                    ))
                }
            }

            // Siempre emitir desde Room (reactivo)
            emitAll(daoProducto.obtenerPorCompra(compraLocal.id).map { lista ->
                lista.map { it.aDominio() }
            })
        } else {
            emit(emptyList())
        }
    }

    // ESCRIBIR

    /**
     * Agrega una compra nueva.
     * 1. Inserta en Supabase → obtiene el UUID real
     * 2. Inserta en Room con ese UUID
     * 3. Devuelve (idLocal, idSupabase) para usarlos en foto y productos
     */
    suspend fun insertarCompraCompleta(
        fecha: String,
        hora: String,
        supermercado: String,
        total: Double,
        cantidadProductos: Int,
        descuento: Double = 0.0
    ): Pair<Int, String> {
        val dto = supabase.postgrest
            .from("compras")
            .insert(buildJsonObject {
                put("usuario_id",  usuarioId)
                put("fecha",       fecha)
                put("hora",        hora)
                put("supermercado", supermercado)
                put("total",       total)
                put("descuento",   descuento)
            }) { select() }
            .decodeSingle<DtoCompraSupabase>()

        val idLocal = daoCompra.insertar(dto.aDominio(cantidadProductos).aEntidad()).toInt()
        return Pair(idLocal, dto.id)
    }

    /**
     * Agrega los productos de una compra.
     * Inserta en Supabase y en Room simultáneamente.
     */
    suspend fun insertarProductos(
        compraIdLocal: Int,
        compraIdSupabase: String,
        productos: List<ProductoEnCompra>
    ) {
        productos.forEach { prod ->
            // Supabase primero
            val dto = runCatching {
                supabase.postgrest
                    .from("productos")
                    .insert(buildJsonObject {
                        put("compra_id", compraIdSupabase)
                        put("nombre",    prod.nombre)
                        put("cantidad",  prod.cantidad)
                        put("precio",    prod.costo)
                        if (prod.codigoBarras.isNotBlank()) put("codigo_barras", prod.codigoBarras)
                    }) { select() }
                    .decodeSingle<DtoProductoSupabase>()
            }.getOrNull()

            // Room con idSupabase del producto
            daoProducto.insertar(EntidadProducto(
                compraId         = compraIdLocal,
                idSupabase       = dto?.id ?: "",
                idCompraSupabase = compraIdSupabase,
                codigo           = prod.codigoBarras,
                nombre           = prod.nombre,
                cantidad         = prod.cantidad,
                precio           = prod.costo
            ))
        }
    }

    /**
     * Sube la foto del ticket a Supabase Storage.
     * Actualiza la URL en Supabase y en Room.
     */
    suspend fun subirFotoTicket(uri: Uri, compraId: String): String? {
        return runCatching {
            // Leer los bytes de la imagen
            val inputStream = contexto.contentResolver.openInputStream(uri)
                ?: throw Exception("No se pudo abrir la imagen")
            val bytes = inputStream.readBytes()
            inputStream.close()

            if (bytes.isEmpty()) throw Exception("La imagen está vacía")

            val nombreArchivo = "tickets/$usuarioId/$compraId.jpg"


            supabase.storage
                .from("tickets")
                .upload(nombreArchivo, bytes) { upsert = true }

            val url = supabase.storage.from("tickets").publicUrl(nombreArchivo)
            url
        }.onFailure { e ->
        }.getOrNull()
    }


    // Versión que recibe bytes directamente (evita problemas de URI expirado)
    suspend fun subirFotoTicketBytes(bytes: ByteArray, compraId: String): String? {
        return runCatching {
            if (bytes.isEmpty()) throw Exception("Imagen vacía")
            val nombreArchivo = "tickets/$usuarioId/$compraId.jpg"
            supabase.storage.from("tickets").upload(nombreArchivo, bytes) { upsert = true }
            val url = supabase.storage.from("tickets").publicUrl(nombreArchivo)
            url
        }.onFailure { e ->
        }.getOrNull()
    }

    suspend fun actualizarFotoTicket(compraIdLocal: Int, compraIdSupabase: String, urlFoto: String) {
        // Supabase
        runCatching {
            supabase.postgrest.from("compras")
                .update({ set("imagen_ticket", urlFoto) }) {
                    filter { eq("id", compraIdSupabase) }
                }
        }
        // Room
        daoCompra.obtenerPorId(compraIdLocal)?.let { entidad ->
            daoCompra.actualizar(entidad.copy(imagenTicket = urlFoto))
        }
    }

    /**
     * Edita una compra existente.
     * Actualiza en Room y en Supabase.
     */
    suspend fun editarCompra(compra: Compra): Result<Unit> = runCatching {
        // Room primero (UI se actualiza inmediatamente)
        daoCompra.actualizar(compra.aEntidad())
        // Supabase
        if (compra.idSupabase.isNotBlank()) {
            supabase.postgrest.from("compras")
                .update(buildJsonObject {
                    put("fecha",        compra.fecha)
                    put("hora",         compra.hora)
                    put("supermercado", compra.supermercado)
                    put("total",        compra.total)
                }) { filter { eq("id", compra.idSupabase) } }
        }
    }

    /**
     * Edita una compra y REEMPLAZA sus productos.
     * 1. Actualiza la cabecera en Room y Supabase (igual que editarCompra).
     * 2. Borra los productos viejos (Supabase + Room) y re-inserta la lista actual.
     * De esta forma los cambios de productos (cantidad, alta y baja) quedan persistidos.
     */
    suspend fun editarCompraConProductos(
        compra: Compra,
        productos: List<ProductoEnCompra>
    ): Result<Unit> = runCatching {
        // 1. Cabecera: Room primero (UI reacciona al toque), luego Supabase
        daoCompra.actualizar(compra.aEntidad())
        if (compra.idSupabase.isNotBlank()) {
            supabase.postgrest.from("compras")
                .update(buildJsonObject {
                    put("fecha",        compra.fecha)
                    put("hora",         compra.hora)
                    put("supermercado", compra.supermercado)
                    put("total",        compra.total)
                    put("descuento",    compra.descuento)
                }) { filter { eq("id", compra.idSupabase) } }
        }

        // 2. Reemplazar productos: borrar los viejos en Supabase...
        if (compra.idSupabase.isNotBlank()) {
            runCatching {
                supabase.postgrest.from("productos")
                    .delete { filter { eq("compra_id", compra.idSupabase) } }
            }
        }
        // ...y en Room (por el id local de la compra)
        daoProducto.eliminarPorCompra(compra.id)

        // Re-insertar la lista actual (Supabase + Room, reutiliza el método existente)
        insertarProductos(compra.id, compra.idSupabase, productos)
    }

    /**
     * Borra una compra.
     * Borra en Room y en Supabase (cascade elimina sus productos en Supabase).
     * También borra los productos de Room manualmente.
     */
    override suspend fun eliminarCompra(compra: Compra) {
        // Room (cascade borra productos por FK)
        daoCompra.eliminar(compra.aEntidad())
        // Supabase
        if (compra.idSupabase.isNotBlank()) {
            runCatching {
                supabase.postgrest.from("compras")
                    .delete { filter { eq("id", compra.idSupabase) } }
            }
        }
    }

    // LEGACY / INTERFACE

    override suspend fun insertarCompra(compra: Compra): Long =
        daoCompra.insertar(compra.aEntidad())

    override suspend fun actualizarCompra(compra: Compra) =
        daoCompra.actualizar(compra.aEntidad())

    override suspend fun insertarProducto(producto: Producto) =
        daoProducto.insertar(producto.aEntidad()).let { }

    override suspend fun actualizarProducto(producto: Producto) =
        daoProducto.actualizar(producto.aEntidad())

    override suspend fun eliminarProducto(producto: Producto) =
        daoProducto.eliminar(producto.aEntidad())
}
