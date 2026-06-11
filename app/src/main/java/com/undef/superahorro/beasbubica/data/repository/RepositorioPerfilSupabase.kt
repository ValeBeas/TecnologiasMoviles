package com.undef.superahorro.data.repository

import android.content.Context
import com.undef.superahorro.data.local.datastore.PreferenciasUsuario
import com.undef.superahorro.data.network.ClienteSupabase
import com.undef.superahorro.data.network.dto.DtoCatalogoProducto
import com.undef.superahorro.data.network.dto.DtoPerfil
import com.undef.superahorro.domain.model.Producto
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

// Maneja el perfil del usuario y su catálogo personal de productos en Supabase.
/**
 * Maneja el perfil del usuario y su catálogo personal de productos en Supabase.
 * El perfil se guarda también en DataStore para mostrarlo sin llamadas de red.
 */
class RepositorioPerfilSupabase(private val contexto: Context) {

    private val supabase     = ClienteSupabase.cliente
    private val preferencias = PreferenciasUsuario(contexto)

    private val usuarioId: String
        get() = supabase.auth.currentUserOrNull()?.id ?: ""

    // Obtiene el perfil del usuario desde Supabase
    suspend fun obtenerPerfil(): Result<DtoPerfil> = runCatching {
        supabase.postgrest
            .from("perfiles")
            .select { filter { eq("id", usuarioId) } }
            .decodeSingleOrNull<DtoPerfil>() ?: DtoPerfil(id = usuarioId, nombre = null, apellido = null)
    }

    // Guarda o actualiza el perfil en Supabase y en DataStore
    suspend fun guardarPerfil(nombre: String, apellido: String): Result<Unit> = runCatching {
        supabase.postgrest
            .from("perfiles")
            .upsert(buildJsonObject {
                put("id", usuarioId)
                put("nombre", nombre)
                put("apellido", apellido)
            })
        preferencias.guardarDatosUsuario(nombre, apellido, "")
    }

    // Obtiene los productos del catálogo personal del usuario
    suspend fun obtenerCatalogo(): Result<List<Producto>> = runCatching {
        supabase.postgrest
            .from("catalogo_productos")
            .select { filter { eq("usuario_id", usuarioId) } }
            .decodeList<DtoCatalogoProducto>()
            .map { it.aDominio() }
    }

    // Agrega un producto al catálogo personal
    suspend fun agregarAlCatalogo(nombre: String, precio: Double, codigoBarras: String): Result<Producto> = runCatching {
        val dto = supabase.postgrest
            .from("catalogo_productos")
            .insert(buildJsonObject {
                put("usuario_id", usuarioId)
                put("nombre", nombre)
                put("precio", precio)
                if (codigoBarras.isNotBlank()) put("codigo_barras", codigoBarras)
            }) { select() }
            .decodeSingle<DtoCatalogoProducto>()
        dto.aDominio()
    }

    // Elimina un producto del catálogo personal por su idSupabase
    suspend fun eliminarDelCatalogo(idSupabase: String): Result<Unit> = runCatching {
        supabase.postgrest
            .from("catalogo_productos")
            .delete { filter { eq("id", idSupabase) } }
    }
}
