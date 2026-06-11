package com.undef.superahorro.data.network.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO de supermercado para la API REST genérica (ServicioApi).
 * Estructura base para recibir información de supermercados desde un servidor.
 */
data class DtoSupermercado(
    @SerializedName("id")       val id: Int,
    @SerializedName("name")     val nombre: String,
    @SerializedName("logo_url") val urlLogo: String? = null
)
