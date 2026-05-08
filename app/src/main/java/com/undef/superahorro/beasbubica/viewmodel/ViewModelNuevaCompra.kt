package com.undef.superahorro.viewmodel

import androidx.lifecycle.ViewModel
import com.undef.superahorro.domain.model.ProductoEnCompra
import com.undef.superahorro.domain.model.calcularTotal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Comparte el estado de la compra que se está cargando entre NuevaCompra y NuevoProducto.
 * Se crea una sola vez en el grafo y ambas pantallas lo usan para no perder los datos al navegar.
 */
class ViewModelNuevaCompra : ViewModel() {

    // Lista reactiva de productos agregados a la compra en curso
    private val _productos = MutableStateFlow<List<ProductoEnCompra>>(emptyList())
    val productos: StateFlow<List<ProductoEnCompra>> = _productos.asStateFlow()

    /** Total actual de la compra. Se recalcula solo cada vez que cambia la lista. */
    val total: Double get() = _productos.value.calcularTotal()

    /** Agrega un producto nuevo a la compra que se está cargando. */
    fun agregarProducto(producto: ProductoEnCompra) {
        _productos.value = _productos.value + producto
    }

    /**
 * Aumenta en 1 la cantidad del producto en la posición indicada.
 *
 * @param indice posición del producto en la lista
 */
    fun aumentarCantidad(indice: Int) {
        _productos.value = _productos.value.mapIndexed { i, p ->
            if (i == indice) p.copy(cantidad = p.cantidad + 1) else p
        }
    }

    /**
 * Reduce en 1 la cantidad del producto. La cantidad mínima es 1, no puede llegar a 0.
 *
 * @param indice posición del producto en la lista
 */
    fun disminuirCantidad(indice: Int) {
        _productos.value = _productos.value.mapIndexed { i, p ->
            if (i == indice && p.cantidad > 1) p.copy(cantidad = p.cantidad - 1) else p
        }
    }

    /**
 * Elimina el producto en la posición indicada de la lista.
 *
 * @param indice posición del producto a eliminar
 */
    fun eliminarProducto(indice: Int) {
        _productos.value = _productos.value.filterIndexed { i, _ -> i != indice }
    }

    /** Vacía la lista de productos. Lo llamo cuando el usuario guarda o cancela la compra. */
    fun limpiar() {
        _productos.value = emptyList()
    }
}
