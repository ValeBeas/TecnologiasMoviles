package com.undef.superahorro.viewmodel

/**
 * Estado genérico de cualquier pantalla que carga datos.
 * Tiene tres situaciones posibles: cargando, con datos, o con error.
 */
data class EstadoUi<T>(
    val cargando: Boolean = false,
    val datos: T? = null,
    val error: String? = null
)
