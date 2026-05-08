package com.undef.superahorro.domain.model

/**
 * Modelo de un producto dentro de una compra ya guardada.
 * precioTotal se calcula solo multiplicando cantidad × precio.
 */
data class Producto(
    val id: Int = 0,
    val compraId: Int = 0,           // ID de la compra a la que pertenece
    val codigo: String = "",         // Código de barras EAN-13
    val nombre: String = "",
    val descripcion: String = "",
    val cantidad: Int = 1,
    val precio: Double = 0.0         // Precio unitario
) {
    // Precio total de este ítem = cantidad × precio unitario
    val precioTotal: Double get() = cantidad * precio
}
