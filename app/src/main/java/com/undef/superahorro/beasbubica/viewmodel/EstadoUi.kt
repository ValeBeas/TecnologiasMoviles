package com.undef.superahorro.viewmodel

/**
 * Clase genérica para manejar el estado de cualquier pantalla.
 * Tiene tres estados: cargando (muestra el spinner), datos (muestra el contenido)
 * y error (muestra el mensaje de error).
 */
data class EstadoUi<T>(
    val cargando: Boolean = false,
    val datos: T? = null,
    val error: String? = null
)
