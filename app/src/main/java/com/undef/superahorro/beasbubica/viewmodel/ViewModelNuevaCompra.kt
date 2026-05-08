package com.undef.superahorro.viewmodel

import androidx.lifecycle.ViewModel
import com.undef.superahorro.domain.model.ProductoEnCompra
import com.undef.superahorro.domain.model.calcularTotal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Comparte el estado de la compra que se está cargando entre NuevaCompra y NuevoProducto
 * Se crea una sola vez en el grafo y ambas pantallas lo usan para no perder los datos al navegar
 */
class ViewModelNuevaCompra : ViewModel() {

    // Lista reactiva de productos agregados a la compra en curso
    private val _productos = MutableStateFlow<List<ProductoEnCompra>>(emptyList())
    val productos: StateFlow<List<ProductoEnCompra>> = _productos.asStateFlow()

    val total: Double get() = _productos.value.calcularTotal()

    fun agregarProducto(producto: ProductoEnCompra) {
        _productos.value = _productos.value + producto
    }

    fun aumentarCantidad(indice: Int) {
        _productos.value = _productos.value.mapIndexed { i, p ->
            if (i == indice) p.copy(cantidad = p.cantidad + 1) else p
        }
    }

    fun disminuirCantidad(indice: Int) {
        _productos.value = _productos.value.mapIndexed { i, p ->
            if (i == indice && p.cantidad > 1) p.copy(cantidad = p.cantidad - 1) else p
        }
    }

    fun eliminarProducto(indice: Int) {
        _productos.value = _productos.value.filterIndexed { i, _ -> i != indice }
    }

    fun limpiar() {
        _productos.value = emptyList()
    }
}
