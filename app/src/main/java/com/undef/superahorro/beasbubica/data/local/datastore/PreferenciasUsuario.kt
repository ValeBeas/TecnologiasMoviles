package com.undef.superahorro.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.undef.superahorro.domain.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// Extensión que crea el DataStore una sola vez por contexto de aplicación
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferencias_usuario")


class PreferenciasUsuario(private val contexto: Context) {

    companion object {
        val CLAVE_ID       = intPreferencesKey("usuario_id")
        val CLAVE_NOMBRE   = stringPreferencesKey("usuario_nombre")
        val CLAVE_APELLIDO = stringPreferencesKey("usuario_apellido")
        val CLAVE_EMAIL    = stringPreferencesKey("usuario_email")
        val CLAVE_TELEFONO = stringPreferencesKey("usuario_telefono")
        val CLAVE_LOGUEADO = booleanPreferencesKey("esta_logueado")
        val CLAVE_MODO_OSCURO = booleanPreferencesKey("modo_oscuro")
    }

    val flujoUsuario: Flow<Usuario?> = contexto.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            if (prefs[CLAVE_LOGUEADO] != true) null
            else Usuario(
                id       = prefs[CLAVE_ID] ?: 0,
                nombre   = prefs[CLAVE_NOMBRE] ?: "",
                apellido = prefs[CLAVE_APELLIDO] ?: "",
                email    = prefs[CLAVE_EMAIL] ?: "",
                telefono = prefs[CLAVE_TELEFONO] ?: ""
            )
        }

    val flujoLogueado: Flow<Boolean> = contexto.dataStore.data
        .map { it[CLAVE_LOGUEADO] ?: false }

    val flujoModoOscuro: Flow<Boolean> = contexto.dataStore.data
        .map { it[CLAVE_MODO_OSCURO] ?: false }

    suspend fun guardarUsuario(usuario: Usuario) {
        contexto.dataStore.edit { prefs ->
            prefs[CLAVE_ID]       = usuario.id
            prefs[CLAVE_NOMBRE]   = usuario.nombre
            prefs[CLAVE_APELLIDO] = usuario.apellido
            prefs[CLAVE_EMAIL]    = usuario.email
            prefs[CLAVE_TELEFONO] = usuario.telefono
            prefs[CLAVE_LOGUEADO] = true
        }
    }

    suspend fun guardarModoOscuro(activado: Boolean) {
        contexto.dataStore.edit { it[CLAVE_MODO_OSCURO] = activado }
    }

    suspend fun cerrarSesion() { contexto.dataStore.edit { it.clear() } }
}
