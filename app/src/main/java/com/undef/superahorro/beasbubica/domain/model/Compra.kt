package com.undef.superahorro.domain.model

/**
 * Modelo de una compra. Lo usan las pantallas y los ViewModels.
 * idSupabase vincula el registro local con Supabase.
 */
data class Compra(
    val id: Int = 0,
    val idSupabase: String = "",
    val fecha: String = "",
    val hora: String = "",
    val supermercado: String = "",
    val total: Double = 0.0,          // total final (ya con el descuento restado)
    val descuento: Double = 0.0,      // monto de descuento aplicado (0 si no hubo)
    val cantidadProductos: Int = 0,
    val imagenTicket: String? = null
)
