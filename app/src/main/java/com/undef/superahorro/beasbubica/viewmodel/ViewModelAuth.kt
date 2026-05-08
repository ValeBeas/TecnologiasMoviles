package com.undef.superahorro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.superahorro.domain.model.Usuario
import com.undef.superahorro.domain.repository.RepositorioUsuario
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Maneja el login, la sesión activa y el logout.
 * En la Entrega 1 el login es de mentira: cualquier email y contraseña funcionan.
 * En la Entrega 2 se conecta con un servidor real.
 */
class ViewModelAuth(private val repositorioUsuario: RepositorioUsuario) : ViewModel() {

    private val _estadoUsuario = MutableStateFlow(EstadoUi<Usuario>())
    val estadoUsuario: StateFlow<EstadoUi<Usuario>> = _estadoUsuario.asStateFlow()

    private val _estaLogueado = MutableStateFlow(false)
    val estaLogueado: StateFlow<Boolean> = _estaLogueado.asStateFlow()

    init { verificarSesion() }

    /** Al crear el ViewModel revisa si ya hay una sesión guardada en DataStore. */
    private fun verificarSesion() {
        viewModelScope.launch {
            runCatching {
                _estaLogueado.value = repositorioUsuario.estaLogueado()
                repositorioUsuario.obtenerUsuario().collect { usuario ->
                    _estadoUsuario.value = EstadoUi(datos = usuario)
                }
            }
        }
    }

    /** Hace el login. Por ahora es de mentira, en Entrega 2 valida contra un servidor. */
    fun iniciarSesion(email: String, password: String, onExito: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _estadoUsuario.value = EstadoUi(cargando = true)
            runCatching {
                val usuarioMock = Usuario(1, "Valentina", "Beas", email)
                repositorioUsuario.guardarUsuario(usuarioMock)
                _estadoUsuario.value = EstadoUi(datos = usuarioMock)
                _estaLogueado.value = true
                onExito()
            }.onFailure { e ->
                _estadoUsuario.value = EstadoUi(error = e.message)
                onError(e.message ?: "Error al iniciar sesión")
            }
        }
    }

    /** Cierra la sesión y borra todos los datos guardados en DataStore. */
    fun cerrarSesion(onCompleto: () -> Unit) {
        viewModelScope.launch {
            runCatching {
                repositorioUsuario.cerrarSesion()
                _estaLogueado.value = false
                _estadoUsuario.value = EstadoUi()
                onCompleto()
            }
        }
    }
}
