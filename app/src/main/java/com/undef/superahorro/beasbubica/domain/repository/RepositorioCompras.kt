package com.undef.superahorro.domain.repository

import com.undef.superahorro.domain.model.Producto
import com.undef.superahorro.domain.model.Compra
import kotlinx.coroutines.flow.Flow

/**
 * Contrato del repositorio de compras.
 * Las pantallas solo conocen esta interfaz, no la implementación concreta.
 */
interface RepositorioCompras {
    fun obtenerCompras(): Flow<List<Compra>>
    suspend fun obtenerCompraPorId(id: String): Compra?
    suspend fun insertarCompra(compra: Compra): Long
    suspend fun actualizarCompra(compra: Compra)
    suspend fun eliminarCompra(compra: Compra)
    fun obtenerProductosPorCompra(compraId: String): Flow<List<Producto>>
    suspend fun insertarProducto(producto: Producto)
    suspend fun actualizarProducto(producto: Producto)
    suspend fun eliminarProducto(producto: Producto)
}
