package com.undef.superahorro.data.repository

import com.undef.superahorro.data.local.datastore.PreferenciasUsuario
import com.undef.superahorro.domain.model.Usuario
import com.undef.superahorro.domain.repository.RepositorioUsuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Implementación del repositorio de usuario.
 */
class RepositorioUsuarioImpl(
    private val preferencias: PreferenciasUsuario
) : RepositorioUsuario {
    override fun obtenerUsuario(): Flow<Usuario?> = preferencias.flujoUsuario
    override suspend fun guardarUsuario(usuario: Usuario) = preferencias.guardarUsuario(usuario)
    override suspend fun cerrarSesion() = preferencias.cerrarSesion()
    override suspend fun estaLogueado(): Boolean = preferencias.flujoLogueado.first()
}
