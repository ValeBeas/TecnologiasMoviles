package com.undef.superahorro.data.network.dto

import com.google.gson.annotations.SerializedName

/**
 * Clase que recibe los datos de supermercados que manda la API en formato JSON.
 */
data class DtoSupermercado(
    @SerializedName("id")       val id: Int,
    @SerializedName("name")     val nombre: String,
    @SerializedName("logo_url") val urlLogo: String? = null
)
