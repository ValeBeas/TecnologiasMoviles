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

// Guarda la sesión del usuario y las preferencias con DataStore.
/**
 * Guarda la sesión y las preferencias del usuario con DataStore.
 * Tokens de sesión, moneda activa y modo oscuro persisten entre cierres de la app.
 */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferencias_usuario")

class PreferenciasUsuario(private val contexto: Context) {

    companion object {
        val CLAVE_USER_ID       = stringPreferencesKey("user_id")
        val CLAVE_USER_EMAIL    = stringPreferencesKey("user_email")
        val CLAVE_NOMBRE        = stringPreferencesKey("usuario_nombre")
        val CLAVE_APELLIDO      = stringPreferencesKey("usuario_apellido")
        val CLAVE_TELEFONO      = stringPreferencesKey("usuario_telefono")
        val CLAVE_LOGUEADO      = booleanPreferencesKey("esta_logueado")
        val CLAVE_MODO_OSCURO   = booleanPreferencesKey("modo_oscuro")
        val CLAVE_MONEDA        = stringPreferencesKey("moneda")
        val CLAVE_ACCESS_TOKEN  = stringPreferencesKey("access_token")
        val CLAVE_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    }

    val flujoUsuario: Flow<Usuario?> = contexto.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            if (prefs[CLAVE_LOGUEADO] != true) null
            else Usuario(
                id = 0,
                idSupabase = prefs[CLAVE_USER_ID] ?: "",
                nombre = prefs[CLAVE_NOMBRE] ?: "",
                apellido = prefs[CLAVE_APELLIDO] ?: "",
                email = prefs[CLAVE_USER_EMAIL] ?: "",
                telefono = prefs[CLAVE_TELEFONO] ?: ""
            )
        }

    val flujoLogueado: Flow<Boolean> = contexto.dataStore.data
        .map { it[CLAVE_LOGUEADO] ?: false }

    val flujoModoOscuro: Flow<Boolean> = contexto.dataStore.data
        .map { it[CLAVE_MODO_OSCURO] ?: false }

    val flujoMoneda: Flow<String> = contexto.dataStore.data
        .map { it[CLAVE_MONEDA] ?: "ARS" }

    val flujoAccessToken: Flow<String?> = contexto.dataStore.data
        .map { it[CLAVE_ACCESS_TOKEN] }

    suspend fun guardarSesion(userId: String, email: String, accessToken: String, refreshToken: String) {
        contexto.dataStore.edit { prefs ->
            prefs[CLAVE_USER_ID]       = userId
            prefs[CLAVE_USER_EMAIL]    = email
            prefs[CLAVE_ACCESS_TOKEN]  = accessToken
            prefs[CLAVE_REFRESH_TOKEN] = refreshToken
            prefs[CLAVE_LOGUEADO]      = true
        }
    }

    suspend fun guardarDatosUsuario(nombre: String, apellido: String, telefono: String) {
        contexto.dataStore.edit { prefs ->
            prefs[CLAVE_NOMBRE]   = nombre
            prefs[CLAVE_APELLIDO] = apellido
            prefs[CLAVE_TELEFONO] = telefono
        }
    }

    // Compatibilidad con RepositorioUsuarioImpl
    suspend fun guardarUsuario(usuario: Usuario) {
        guardarDatosUsuario(usuario.nombre, usuario.apellido, usuario.telefono)
    }

    suspend fun guardarModoOscuro(activado: Boolean) {
        contexto.dataStore.edit { it[CLAVE_MODO_OSCURO] = activado }
    }

    suspend fun guardarMoneda(moneda: String) {
        contexto.dataStore.edit { it[CLAVE_MONEDA] = moneda }
    }

    suspend fun cerrarSesion() {
        contexto.dataStore.edit { it.clear() }
    }
}
