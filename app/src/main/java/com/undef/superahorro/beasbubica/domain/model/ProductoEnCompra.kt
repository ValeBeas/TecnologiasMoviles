package com.undef.superahorro.domain.model

import java.text.NumberFormat
import java.util.Locale

/**
 * Modelo para agregar productos en Nueva Compra.
 * Solo tiene los campos que el usuario carga:
 *  Nombre
 *  Cantidad
 *  Costo
 *  Código de barras.
 */
data class ProductoEnCompra(
    val nombre: String,
    val cantidad: Int,
    val costo: Double,
    val codigoBarras: String = ""  // Opcional — el usuario puede escanearlo o dejarlo vacío
) {
    val subtotal: Double get() = cantidad * costo

    val subtotalFormateado: String
        get() = NumberFormat.getNumberInstance(Locale("es", "AR")).format(subtotal)

    val costoFormateado: String
        get() = NumberFormat.getNumberInstance(Locale("es", "AR")).format(costo)
}

/**
 * Suma el subtotal de todos los productos de la lista.
 * Es la única función que calcula totales en toda la app.
 */
fun List<ProductoEnCompra>.calcularTotal(): Double = sumOf { it.subtotal }
