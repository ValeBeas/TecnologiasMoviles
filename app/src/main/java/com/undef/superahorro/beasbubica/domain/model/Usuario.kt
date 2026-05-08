package com.undef.superahorro.domain.model

/**
 * Datos del usuario logueado. Se guardan en DataStore para mantener la sesión
 * aunque el usuario cierre y vuelva a abrir la app.
 */
data class Usuario(
    val id: Int = 0,
    val nombre: String = "",
    val apellido: String = "",
    val email: String = "",
    val telefono: String = ""
)
