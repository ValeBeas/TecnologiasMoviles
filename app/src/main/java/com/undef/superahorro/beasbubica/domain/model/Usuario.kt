package com.undef.superahorro.domain.model

/**
 * Modelo del usuario logueado.
 * Se persiste en DataStore para mantener la sesión entre cierres de la app.
 */
data class Usuario(
    val id: Int = 0,
    val idSupabase: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val email: String = "",
    val telefono: String = ""
)
