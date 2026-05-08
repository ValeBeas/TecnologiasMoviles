package com.undef.superahorro.domain.repository

import com.undef.superahorro.domain.model.Producto
import com.undef.superahorro.domain.model.Compra
import kotlinx.coroutines.flow.Flow

/**
 * Define qué operaciones existen sobre las compras.
 * La UI y los ViewModels solo conocen esta interfaz, nunca los datos reales de abajo.
 */
interface RepositorioCompras {
    fun obtenerCompras(): Flow<List<Compra>>
    suspend fun obtenerCompraPorId(id: Int): Compra?
    suspend fun insertarCompra(compra: Compra): Long
    suspend fun actualizarCompra(compra: Compra)
    suspend fun eliminarCompra(compra: Compra)
    fun obtenerProductosPorCompra(compraId: Int): Flow<List<Producto>>
    suspend fun insertarProducto(producto: Producto)
    suspend fun actualizarProducto(producto: Producto)
    suspend fun eliminarProducto(producto: Producto)
}
