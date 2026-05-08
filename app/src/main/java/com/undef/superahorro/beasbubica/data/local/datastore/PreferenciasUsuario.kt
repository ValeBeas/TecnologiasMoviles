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

/**
 * Guarda y lee los datos del usuario logueado usando DataStore.
 * Es más moderno que SharedPreferences y funciona de forma asíncrona con corrutinas.
 */
class PreferenciasUsuario(private val contexto: Context) {

    companion object {
        // Claves tipadas para cada dato del usuario
        val CLAVE_ID       = intPreferencesKey("usuario_id")
        val CLAVE_NOMBRE   = stringPreferencesKey("usuario_nombre")
        val CLAVE_APELLIDO = stringPreferencesKey("usuario_apellido")
        val CLAVE_EMAIL    = stringPreferencesKey("usuario_email")
        val CLAVE_TELEFONO = stringPreferencesKey("usuario_telefono")
        val CLAVE_LOGUEADO = booleanPreferencesKey("esta_logueado")
        val CLAVE_MODO_OSCURO = booleanPreferencesKey("modo_oscuro")
    }

    // Flow que emite el usuario actual o null si no hay sesión
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

    // Flow que emite true si el usuario está logueado
    val flujoLogueado: Flow<Boolean> = contexto.dataStore.data
        .map { it[CLAVE_LOGUEADO] ?: false }

    // Flow para el estado del modo oscuro
    val flujoModoOscuro: Flow<Boolean> = contexto.dataStore.data
        .map { it[CLAVE_MODO_OSCURO] ?: false }

    // Persiste los datos del usuario y marca la sesión como activa
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

    // Guarda la preferencia de modo oscuro
    suspend fun guardarModoOscuro(activado: Boolean) {
        contexto.dataStore.edit { it[CLAVE_MODO_OSCURO] = activado }
    }

    // Borra todos los datos al cerrar sesión
    suspend fun cerrarSesion() { contexto.dataStore.edit { it.clear() } }
}
