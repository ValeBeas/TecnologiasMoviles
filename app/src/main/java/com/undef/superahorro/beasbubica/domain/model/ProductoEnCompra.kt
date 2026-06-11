package com.undef.superahorro.domain.model

import java.text.NumberFormat
import java.util.Locale

/**
 * Modelo liviano para el formulario de nueva compra.
 * Tiene solo los datos que el usuario ingresa: nombre, cantidad, costo y código.
 */
data class ProductoEnCompra(
    val nombre: String,
    val cantidad: Int,
    val costo: Double,
    val codigoBarras: String = ""  // Opcional — el usuario puede escanearlo o dejarlo vacío
) {
    /** Subtotal del ítem = cantidad × costo. Se calcula solo, no se guarda. */
    val subtotal: Double get() = cantidad * costo

    /** Subtotal con formato de moneda argentina para mostrar en pantalla. */
    val subtotalFormateado: String
        get() = NumberFormat.getNumberInstance(Locale("es", "AR")).format(subtotal)

    /** Costo unitario con formato de moneda argentina para mostrar en pantalla. */
    val costoFormateado: String
        get() = NumberFormat.getNumberInstance(Locale("es", "AR")).format(costo)
}

/**
 * Suma el subtotal de todos los productos de la lista.
 * Es la única función que calcula totales en toda la app.
 */
fun List<ProductoEnCompra>.calcularTotal(): Double = sumOf { it.subtotal }
