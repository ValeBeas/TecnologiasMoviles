package com.undef.superahorro.domain.repository

import com.undef.superahorro.domain.model.Usuario
import kotlinx.coroutines.flow.Flow

/**
 * Define qué operaciones existen sobre el usuario.
 * Maneja la sesión y los datos del perfil.
 */
interface RepositorioUsuario {
    fun obtenerUsuario(): Flow<Usuario?>
    suspend fun guardarUsuario(usuario: Usuario)
    suspend fun cerrarSesion()
    suspend fun estaLogueado(): Boolean
}
