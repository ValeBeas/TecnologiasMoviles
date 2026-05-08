package com.undef.superahorro.domain.model

/**
 * Modelo de una compra. Tiene los datos que muestra la app en pantalla.
 * No es la entidad de Room — esa es EntidadCompra.
 */
data class Compra(
    val id: Int = 0,
    val fecha: String = "",          // Formato DD/MM/AAAA
    val hora: String = "",           // Formato HH:MM
    val supermercado: String = "",
    val total: Double = 0.0,
    val cantidadProductos: Int = 0,
    val imagenTicket: String? = null // Ruta local a la foto del ticket
)
