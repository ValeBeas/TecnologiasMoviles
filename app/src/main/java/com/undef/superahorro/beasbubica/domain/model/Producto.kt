package com.undef.superahorro.domain.model

/**
 * Modelo de un producto dentro de una compra.
 * precioTotal se calcula automáticamente (cantidad × precio).
 */
data class Producto(
    val id: Int = 0,
    val compraId: Int = 0,
    val idSupabase: String = "",
    val idCompraSupabase: String = "",
    val codigo: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val cantidad: Int = 1,
    val precio: Double = 0.0
) {
    val precioTotal: Double get() = cantidad * precio
}
