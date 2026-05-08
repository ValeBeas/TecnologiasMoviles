package com.undef.superahorro.viewmodel

/**
 * Clase genérica para manejar el estado de cualquier pantalla.
 * Tiene tres estados:
 *  Cargando (muestra el spinner)
 *  Datos (muestra el contenido)
 *  Error (muestra el mensaje de error).
 */
data class EstadoUi<T>(
    val cargando: Boolean = false,
    val datos: T? = null,
    val error: String? = null
)
