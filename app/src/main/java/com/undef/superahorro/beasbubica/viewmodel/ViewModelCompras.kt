package com.undef.superahorro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.superahorro.domain.model.Producto
import com.undef.superahorro.domain.model.Compra
import com.undef.superahorro.domain.repository.RepositorioCompras
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Maneja los datos de las pantallas de compras
 */
class ViewModelCompras(private val repositorio: RepositorioCompras) : ViewModel() {

    // Estado de la lista completa de compras
    private val _estadoCompras = MutableStateFlow(EstadoUi<List<Compra>>(cargando = true))
    val estadoCompras: StateFlow<EstadoUi<List<Compra>>> = _estadoCompras.asStateFlow()

    // Estado de la compra seleccionada para ver su detalle
    private val _compraSeleccionada = MutableStateFlow(EstadoUi<Compra>())
    val compraSeleccionada: StateFlow<EstadoUi<Compra>> = _compraSeleccionada.asStateFlow()

    // Estado de los productos de la compra seleccionada
    private val _estadoProductos = MutableStateFlow(EstadoUi<List<Producto>>(cargando = true))
    val estadoProductos: StateFlow<EstadoUi<List<Producto>>> = _estadoProductos.asStateFlow()

    init { cargarCompras() }

    fun cargarCompras() {
        viewModelScope.launch {
            _estadoCompras.value = EstadoUi(cargando = true)
            runCatching {
                repositorio.obtenerCompras()
                    .catch { e -> _estadoCompras.value = EstadoUi(error = e.message) }
                    .collect { compras ->
                        _estadoCompras.value = EstadoUi(datos = compras)
                    }
            }.onFailure { e ->
                _estadoCompras.value = EstadoUi(error = e.message ?: "Error desconocido")
            }
        }
    }

    fun cargarCompraPorId(id: Int) {
        viewModelScope.launch {
            _compraSeleccionada.value = EstadoUi(cargando = true)
            runCatching {
                _compraSeleccionada.value = EstadoUi(datos = repositorio.obtenerCompraPorId(id))
            }.onFailure { e ->
                _compraSeleccionada.value = EstadoUi(error = e.message ?: "Error desconocido")
            }
        }
    }

    fun cargarProductosPorCompra(compraId: Int) {
        viewModelScope.launch {
            _estadoProductos.value = EstadoUi(cargando = true)
            runCatching {
                repositorio.obtenerProductosPorCompra(compraId)
                    .catch { e -> _estadoProductos.value = EstadoUi(error = e.message) }
                    .collect { productos ->
                        _estadoProductos.value = EstadoUi(datos = productos)
                    }
            }.onFailure { e ->
                _estadoProductos.value = EstadoUi(error = e.message ?: "Error desconocido")
            }
        }
    }
}
