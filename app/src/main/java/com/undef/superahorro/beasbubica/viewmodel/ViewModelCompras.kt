package com.undef.superahorro.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.superahorro.data.repository.RepositorioComprasSupabase
import com.undef.superahorro.domain.model.Compra
import com.undef.superahorro.domain.model.Producto
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Provee la lista de compras, el detalle y los productos a las pantallas.
 * Lee siempre de Room (caché local sincronizado con Supabase al iniciar).
 */
class ViewModelCompras(private val contexto: Context) : ViewModel() {

    private val repositorio = RepositorioComprasSupabase(contexto)

    private val _estadoCompras = MutableStateFlow(EstadoUi<List<Compra>>(cargando = true))
    val estadoCompras: StateFlow<EstadoUi<List<Compra>>> = _estadoCompras.asStateFlow()

    private val _compraSeleccionada = MutableStateFlow(EstadoUi<Compra>())
    val compraSeleccionada: StateFlow<EstadoUi<Compra>> = _compraSeleccionada.asStateFlow()

    private val _estadoProductos = MutableStateFlow(EstadoUi<List<Producto>>(cargando = true))
    val estadoProductos: StateFlow<EstadoUi<List<Producto>>> = _estadoProductos.asStateFlow()

    init { cargarCompras() }

    // Carga las compras: limpia Room, sincroniza con Supabase, luego lee de Room
    fun cargarCompras() {
        viewModelScope.launch {
            _estadoCompras.value = EstadoUi(cargando = true)
            // Solo leer de Room — ya fue sincronizado al iniciar sesión
            runCatching {
                repositorio.obtenerCompras()
                    .catch { e -> _estadoCompras.value = EstadoUi(error = e.message) }
                    .collect { compras ->
                        _estadoCompras.value = EstadoUi(datos = compras)
                    }
            }.onFailure { e ->
                _estadoCompras.value = EstadoUi(error = e.message ?: "Error al cargar compras")
            }
        }
    }

    // Carga una compra por ID para el detalle
    fun cargarCompraPorId(id: String) {
        viewModelScope.launch {
            _compraSeleccionada.value = EstadoUi(cargando = true)
            runCatching {
                _compraSeleccionada.value = EstadoUi(datos = repositorio.obtenerCompraPorId(id))
            }.onFailure { e ->
                _compraSeleccionada.value = EstadoUi(error = e.message ?: "Error")
            }
        }
    }

    // Carga los productos de una compra
    fun cargarProductosPorCompra(compraId: String) {
        viewModelScope.launch {
            _estadoProductos.value = EstadoUi(cargando = true)
            runCatching {
                repositorio.obtenerProductosPorCompra(compraId)
                    .catch { e -> _estadoProductos.value = EstadoUi(error = e.message) }
                    .collect { productos ->
                        _estadoProductos.value = EstadoUi(datos = productos)
                    }
            }.onFailure { e ->
                _estadoProductos.value = EstadoUi(error = e.message ?: "Error")
            }
        }
    }
}
