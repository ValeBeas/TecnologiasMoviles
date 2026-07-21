package com.undef.superahorro.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.undef.superahorro.R
import com.undef.superahorro.data.repository.RepositorioAuthSupabase
import com.undef.superahorro.data.repository.RepositorioComprasSupabase
import com.undef.superahorro.domain.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Maneja el login, registro y cierre de sesión contra Supabase.
 * Al iniciar sesión llama a sincronizarCompleto() para llenar Room.
 */
class ViewModelAuth(private val contexto: Context) : ViewModel() {

    private val repositorio = RepositorioAuthSupabase(contexto)
    private val repositorioCompras = RepositorioComprasSupabase(contexto)

    private val _estadoUsuario = MutableStateFlow(EstadoUi<Usuario>())
    val estadoUsuario: StateFlow<EstadoUi<Usuario>> = _estadoUsuario.asStateFlow()

    private val _estaLogueado = MutableStateFlow(false)
    val estaLogueado: StateFlow<Boolean> = _estaLogueado.asStateFlow()

    // Verifica la sesión guardada al iniciar la app
    fun verificarSesion(onExito: () -> Unit, onSinSesion: () -> Unit) {
        viewModelScope.launch {
            _estadoUsuario.value = EstadoUi(cargando = true)
            repositorio.restaurarSesion()
                .onSuccess { usuario ->
                    _estadoUsuario.value = EstadoUi(datos = usuario)
                    _estaLogueado.value = true
                    // Sincronizar Room completo al restaurar sesión (apertura de app)
                    repositorioCompras.sincronizarCompleto()
                    onExito()
                }
                .onFailure {
                    _estadoUsuario.value = EstadoUi()
                    _estaLogueado.value = false
                    onSinSesion()
                }
        }
    }

    // Login real contra Supabase
    fun iniciarSesion(
        email: String,
        contrasena: String,
        onExito: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _estadoUsuario.value = EstadoUi(cargando = true)
            repositorio.iniciarSesion(email, contrasena)
                .onSuccess { usuario ->
                    _estadoUsuario.value = EstadoUi(datos = usuario)
                    _estaLogueado.value = true
                    // Sincronizar Room completo al hacer login
                    repositorioCompras.sincronizarCompleto()
                    onExito()
                }
                .onFailure { e ->
                    _estadoUsuario.value = EstadoUi(error = e.message)
                    onError(mensajeDeError(e.message))
                }
        }
    }

    // Registro nuevo usuario
    fun registrarse(
        email: String,
        contrasena: String,
        nombre: String,
        apellido: String,
        onExito: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _estadoUsuario.value = EstadoUi(cargando = true)
            repositorio.registrarse(email, contrasena, nombre, apellido)
                .onSuccess { usuario ->
                    _estadoUsuario.value = EstadoUi(datos = usuario)
                    _estaLogueado.value = true
                    // Sincronizar Room (usuario nuevo — Room vacío)
                    repositorioCompras.sincronizarCompleto()
                    onExito()
                }
                .onFailure { e ->
                    _estadoUsuario.value = EstadoUi(error = e.message)
                    onError(mensajeDeError(e.message))
                }
        }
    }

    // Envía el email de recuperación de contraseña
    fun recuperarContrasena(email: String, onExito: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            repositorio.recuperarContrasena(email)
                .onSuccess { onExito() }
                .onFailure { e -> onError(mensajeDeError(e.message)) }
        }
    }

    // Cierra sesión y limpia todo
    fun cerrarSesion(onCompleto: () -> Unit) {
        viewModelScope.launch {
            repositorio.cerrarSesion()
            _estaLogueado.value = false
            _estadoUsuario.value = EstadoUi()
            onCompleto()
        }
    }

    // Convierte los errores técnicos de Supabase en mensajes legibles.
    // Usa contexto.getString(...) porque el ViewModel no es un @Composable.
    private fun mensajeDeError(mensaje: String?): String = when {
        mensaje == null -> contexto.getString(R.string.error_unknown)
        "Invalid login credentials" in mensaje -> contexto.getString(R.string.auth_error_invalid_credentials)
        "Email not confirmed" in mensaje -> contexto.getString(R.string.auth_error_email_not_confirmed)
        "User already registered" in mensaje -> contexto.getString(R.string.auth_error_user_exists)
        "Password should be" in mensaje -> contexto.getString(R.string.auth_error_weak_password)
        "Unable to validate" in mensaje -> contexto.getString(R.string.error_network)
        else -> contexto.getString(R.string.error_with_detail, mensaje)
    }
}
