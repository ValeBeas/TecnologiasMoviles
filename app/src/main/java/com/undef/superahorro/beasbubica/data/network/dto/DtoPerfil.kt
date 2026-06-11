package com.undef.superahorro.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// DTO que mapea la tabla 'perfiles' de Supabase.
@Serializable
/**
 * Mapea una fila de la tabla 'perfiles' de Supabase.
 * Guarda el nombre y apellido del usuario además de los datos de Auth.
 */
data class DtoPerfil(
    val id: String = "",
    val nombre: String? = null,
    val apellido: String? = null,
    @SerialName("updated_at") val actualizadoEn: String? = null
)
