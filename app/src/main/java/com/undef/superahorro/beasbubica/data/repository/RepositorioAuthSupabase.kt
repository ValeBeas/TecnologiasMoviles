package com.undef.superahorro.data.repository

import android.content.Context
import com.undef.superahorro.data.local.datastore.PreferenciasUsuario
import com.undef.superahorro.data.local.db.BaseDeDatos
import com.undef.superahorro.data.local.datastore.dataStore
import com.undef.superahorro.data.network.ClienteSupabase
import com.undef.superahorro.domain.model.Usuario
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.first

/**
 * Maneja el login, registro y sesión contra Supabase Auth.
 * Persiste los tokens en DataStore para mantener la sesión entre cierres.
 */
class RepositorioAuthSupabase(private val contexto: Context) {

    private val supabase = ClienteSupabase.cliente
    private val preferencias = PreferenciasUsuario(contexto)

    // Inicia sesión con email y contraseña contra Supabase
    suspend fun iniciarSesion(email: String, contrasena: String): Result<Usuario> {
        return runCatching {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = contrasena
            }

            val sesion = supabase.auth.currentSessionOrNull()
                ?: throw Exception("No se pudo obtener la sesión")

            val usuario = supabase.auth.currentUserOrNull()
                ?: throw Exception("No se pudo obtener el usuario")

            // Persistir la sesión en DataStore
            preferencias.guardarSesion(
                userId = usuario.id,
                email = email,
                accessToken = sesion.accessToken,
                refreshToken = sesion.refreshToken
            )

            Usuario(
                idSupabase = usuario.id,
                email = email,
                nombre = email.substringBefore("@")
            )
        }
    }

    // Registra un usuario nuevo en Supabase Auth
    suspend fun registrarse(
        email: String,
        contrasena: String,
        nombre: String,
        apellido: String
    ): Result<Usuario> {
        return runCatching {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = contrasena
            }

            // Hacer login automático después del registro
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = contrasena
            }

            val sesion = supabase.auth.currentSessionOrNull()
                ?: throw Exception("No se pudo obtener la sesión")

            val usuario = supabase.auth.currentUserOrNull()
                ?: throw Exception("No se pudo obtener el usuario")

            preferencias.guardarSesion(
                userId = usuario.id,
                email = email,
                accessToken = sesion.accessToken,
                refreshToken = sesion.refreshToken
            )

            preferencias.guardarDatosUsuario(nombre, apellido, "")

            Usuario(
                idSupabase = usuario.id,
                email = email,
                nombre = nombre,
                apellido = apellido
            )
        }
    }

    // Restaura la sesión guardada en DataStore al abrir la app
    suspend fun restaurarSesion(): Result<Usuario> {
        return runCatching {
            val accessToken = preferencias.flujoAccessToken.first()
                ?: throw Exception("Sin sesión guardada")

            // Supabase valida y refresca el token automáticamente
            supabase.auth.retrieveUser(accessToken)
            supabase.auth.refreshCurrentSession()

            val usuario = supabase.auth.currentUserOrNull()
                ?: throw Exception("Sesión expirada")

            // Actualizar tokens en DataStore
            val sesion = supabase.auth.currentSessionOrNull()
            if (sesion != null) {
                preferencias.guardarSesion(
                    userId = usuario.id,
                    email = usuario.email ?: "",
                    accessToken = sesion.accessToken,
                    refreshToken = sesion.refreshToken
                )
            }

            val datosUsuario = preferencias.flujoUsuario.first()

            Usuario(
                idSupabase = usuario.id,
                email = usuario.email ?: "",
                nombre = datosUsuario?.nombre ?: usuario.email?.substringBefore("@") ?: "",
                apellido = datosUsuario?.apellido ?: ""
            )
        }
    }

    // Envía un email de recuperación de contraseña a través de Supabase Auth
    suspend fun recuperarContrasena(email: String): Result<Unit> = runCatching {
        supabase.auth.resetPasswordForEmail(email)
    }

    // Cierra sesión en Supabase y limpia DataStore
    suspend fun cerrarSesion() {
        runCatching { supabase.auth.signOut() }
        // Limpiar caché local al cerrar sesión
        runCatching { BaseDeDatos.obtenerInstancia(contexto).daoCompra().eliminarTodas() }
        preferencias.cerrarSesion()
    }

    fun estaLogueado(): Boolean =
        supabase.auth.currentUserOrNull() != null
}
