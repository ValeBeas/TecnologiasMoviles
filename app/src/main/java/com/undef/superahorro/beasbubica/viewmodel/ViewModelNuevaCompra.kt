package com.undef.superahorro.viewmodel

import androidx.lifecycle.ViewModel
import com.undef.superahorro.domain.model.ProductoEnCompra
import com.undef.superahorro.domain.model.calcularTotal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

/**
 * Estado del formulario de nueva compra.
 * Persiste supermercado, fecha, hora y productos al navegar a AgregarProducto.
 */
class ViewModelNuevaCompra : ViewModel() {

    // Estado del formulario — persiste al navegar entre pantallas
    private val _supermercado = MutableStateFlow("")
    val supermercado: StateFlow<String> = _supermercado.asStateFlow()

    private val _otroMercado = MutableStateFlow("")
    val otroMercado: StateFlow<String> = _otroMercado.asStateFlow()

    private val _fecha = MutableStateFlow(
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    )
    val fecha: StateFlow<String> = _fecha.asStateFlow()

    private val _hora = MutableStateFlow(
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    )
    val hora: StateFlow<String> = _hora.asStateFlow()

    // Lista de productos de la compra en curso
    private val _productos = MutableStateFlow<List<ProductoEnCompra>>(emptyList())
    val productos: StateFlow<List<ProductoEnCompra>> = _productos.asStateFlow()

    val total: Double get() = _productos.value.calcularTotal()

    fun setSupermercado(valor: String) { _supermercado.value = valor }
    fun setOtroMercado(valor: String)  { _otroMercado.value = valor }
    fun setFecha(valor: String)        { _fecha.value = valor }
    fun setHora(valor: String)         { _hora.value = valor }

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

    // Limpia todo al guardar o cancelar
    fun limpiar() {
        _supermercado.value = ""
        _otroMercado.value  = ""
        _fecha.value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        _hora.value  = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        _productos.value = emptyList()
    }
}
