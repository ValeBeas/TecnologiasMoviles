package com.undef.superahorro.data.repository

import com.undef.superahorro.domain.model.Compra
import com.undef.superahorro.domain.model.Producto
import com.undef.superahorro.domain.model.ProductoEnCompra
import com.undef.superahorro.domain.model.calcularTotal
import com.undef.superahorro.domain.repository.RepositorioCompras
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Datos de prueba para la Entrega 1. Tengo 17 compras con fechas entre 2025 y 2026
 * para que se vean datos en todos los filtros de estadísticas.
 * Los IDs van de menor a mayor: el ID más alto es la compra más reciente.
 * En la Entrega 2 esto se reemplaza por Room + Retrofit sin tocar la interfaz.
 */
class RepositorioComprasImpl : RepositorioCompras {

    /** Función corta para crear un producto mock sin tener que repetir los campos vacíos. */
    private fun pm(id: Int, compraId: Int, nombre: String, cantidad: Int, costo: Double) =
        Producto(id = id, compraId = compraId, nombre = nombre, cantidad = cantidad, precio = costo)

    // ── Productos mock ────────────────────────────────────────────────────
    // Cada lista tiene distinta cantidad de ítems y costos con o sin decimales
    private val productosMock = mapOf(

        // Compra 1 — nov 2025 — Carrefour — 2 productos
        1 to listOf(
            pm(1,  1, "Yerba Taragüi 500g",      2, 1590.50),
            pm(2,  1, "Aceite Cocinero 900ml",   1, 1105.75),
        ),

        // Compra 2 — dic 2025 — Coto — 1 producto (compra pequeña)
        2 to listOf(
            pm(3,  2, "Pan Lactal Bimbo 550g",   1,  875.00),
        ),

        // Compra 3 — ene 2026 — Jumbo — 4 productos
        3 to listOf(
            pm(4,  3, "Whisky Johnnie Walker",   1, 7999.99),
            pm(5,  3, "Queso Cremoso 400g",      2, 2150.00),
            pm(6,  3, "Galletitas Oreo 120g",    3,  520.50),
            pm(7,  3, "Coca-Cola 2.25L",         4,  895.25),
        ),

        // Compra 4 — ene 2026 — Día — 2 productos con decimales
        4 to listOf(
            pm(8,  4, "Azúcar Ledesma 1kg",      2,  567.33),
            pm(9,  4, "Harina 000 Cañuelas 1kg", 3,  489.99),
        ),

        // Compra 5 — feb 2026 — Carrefour — 3 productos
        5 to listOf(
            pm(10, 5, "Leche La Serenísima 1L",  6,  645.00),
            pm(11, 5, "Café Nescafé 170g",       1, 1720.50),
            pm(12, 5, "Manteca La Salamandra",   2,  810.75),
        ),

        // Compra 6 — feb 2026 — Coto — 5 productos (compra grande)
        6 to listOf(
            pm(13, 6, "Arroz Gallo Oro 1kg",     3,  810.00),
            pm(14, 6, "Fideos Matarazzo 500g",   5,  465.99),
            pm(15, 6, "Jabón Palmolive 3x90g",   2,  720.00),
            pm(16, 6, "Shampoo Head & Shoulders",1,  985.50),
            pm(17, 6, "Papel Higiénico Elite x4",3, 1050.00),
        ),

        // Compra 7 — mar 2026 — Jumbo — 2 productos
        7 to listOf(
            pm(18, 7, "Vino Trapiche Malbec",    2, 2950.00),
            pm(19, 7, "Cereal Special K 330g",   1, 1280.00),
        ),

        // Compra 8 — mar 2026 — Día — 6 productos (compra grande con decimales)
        8 to listOf(
            pm(20, 8, "Carne Picada Especial 1kg",2, 4750.99),
            pm(21, 8, "Tomate en lata 400g",      3,  320.50),
            pm(22, 8, "Aceite de Oliva 500ml",    1, 1899.00),
            pm(23, 8, "Sal Fina La Dosificadora", 2,  189.75),
            pm(24, 8, "Pimienta negra molida",    1,  342.00),
            pm(25, 8, "Vinagre de manzana 500ml", 2,  415.50),
        ),

        // Compra 9 — mar 2026 — Carrefour — 1 producto
        9 to listOf(
            pm(26, 9, "Detergente Magistral 750ml", 2, 625.00),
        ),

        // Compra 10 — abr 2026 — Coto — 3 productos
        10 to listOf(
            pm(27, 10, "Galletitas Oreo 120g",    4,  520.50),
            pm(28, 10, "Jugo Tang x10 sobres",    2,  485.00),
            pm(29, 10, "Agua Villavicencio 1.5L", 6,  290.75),
        ),

        // Compra 11 — abr 2026 — Jumbo — 4 productos con decimales
        11 to listOf(
            pm(30, 11, "Yerba Taragüi 500g",      3, 1590.50),
            pm(31, 11, "Leche chocolatada 1L",    4,  780.25),
            pm(32, 11, "Helado Freddo 1kg",       1, 3450.99),
            pm(33, 11, "Barras de cereal x6",     2,  695.00),
        ),

        // Compra 12 — abr 2026 — Carrefour — 2 productos
        12 to listOf(
            pm(34, 12, "Lavandina Ayudín 2L",     2,  420.50),
            pm(35, 12, "Desodorante Rexona",      1,  985.00),
        ),

        // Compra 13 — 01/may/2026 — Día — 3 productos
        13 to listOf(
            pm(36, 13, "Pan Lactal Bimbo 550g",   2,  875.00),
            pm(37, 13, "Mermelada Arcor 454g",    1,  650.75),
            pm(38, 13, "Dulce de leche Sancor 1kg",1,1280.99),
        ),

        // Compra 14 — 03/may/2026 — Coto — 7 productos (compra semanal grande)
        14 to listOf(
            pm(39, 14, "Leche La Serenísima 1L",  6,  645.00),
            pm(40, 14, "Yerba Taragüi 500g",      2, 1590.50),
            pm(41, 14, "Aceite Cocinero 900ml",   2, 1105.75),
            pm(42, 14, "Arroz Gallo Oro 1kg",     2,  810.00),
            pm(43, 14, "Fideos Matarazzo 500g",   3,  465.99),
            pm(44, 14, "Azúcar Ledesma 1kg",      1,  567.33),
            pm(45, 14, "Harina 000 Cañuelas 1kg", 2,  489.99),
        ),

        // Compra 15 — 05/may/2026 — Jumbo — 2 productos con decimales
        15 to listOf(
            pm(46, 15, "Carne Picada Especial 1kg",1, 4750.99),
            pm(47, 15, "Pollo entero 2kg",         1, 3200.50),
        ),

        // Compra 16 — 06/may/2026 — Carrefour — 5 productos
        16 to listOf(
            pm(48, 16, "Jabón en polvo Skip 3kg",  1, 2890.00),
            pm(49, 16, "Suavizante Comfort 1.8L",  1,  980.50),
            pm(50, 16, "Papel Higiénico Elite x4", 2, 1050.00),
            pm(51, 16, "Lavandina Ayudín 2L",      2,  420.50),
            pm(52, 16, "Esponja Scotch Brite x2",  1,  345.75),
        ),

        // Compra 17 — 07/may/2026 — Coto — 4 productos (compra de hoy)
        17 to listOf(
            pm(53, 17, "Galletitas Oreo 120g",     2,  520.50),
            pm(54, 17, "Coca-Cola 2.25L",          3,  895.25),
            pm(55, 17, "Agua Villavicencio 1.5L",  4,  290.75),
            pm(56, 17, "Café Nescafé 170g",        1, 1720.50),
        ),
    )

    /** Calcula el total de una compra sumando sus productos con calcularTotal(). */
    private fun totalDeCompra(compraId: Int): Double =
        productosMock[compraId]
            ?.map { ProductoEnCompra(it.nombre, it.cantidad, it.precio) }
            ?.calcularTotal()
            ?: 0.0

    // ── Compras mock — IDs ascendentes → más reciente = ID mayor ─────────
    // El historial las ordenará de mayor a menor ID para mostrar la más nueva primero.
    private val comprasMock = listOf(
        Compra(1,  "15/11/2025", "09:00", "Carrefour", totalDeCompra(1),  2),
        Compra(2,  "03/12/2025", "16:30", "Coto",      totalDeCompra(2),  1),
        Compra(3,  "12/01/2026", "10:15", "Jumbo",     totalDeCompra(3),  4),
        Compra(4,  "28/01/2026", "19:00", "Día",       totalDeCompra(4),  2),
        Compra(5,  "10/02/2026", "11:30", "Carrefour", totalDeCompra(5),  3),
        Compra(6,  "22/02/2026", "14:00", "Coto",      totalDeCompra(6),  5),
        Compra(7,  "05/03/2026", "08:45", "Jumbo",     totalDeCompra(7),  2),
        Compra(8,  "18/03/2026", "17:15", "Día",       totalDeCompra(8),  6),
        Compra(9,  "30/03/2026", "12:00", "Carrefour", totalDeCompra(9),  1),
        Compra(10, "08/04/2026", "10:00", "Coto",      totalDeCompra(10), 3),
        Compra(11, "15/04/2026", "18:30", "Jumbo",     totalDeCompra(11), 4),
        Compra(12, "25/04/2026", "09:30", "Carrefour", totalDeCompra(12), 2),
        Compra(13, "01/05/2026", "11:00", "Día",       totalDeCompra(13), 3),
        Compra(14, "03/05/2026", "14:30", "Coto",      totalDeCompra(14), 7),
        Compra(15, "05/05/2026", "16:00", "Jumbo",     totalDeCompra(15), 2),
        Compra(16, "06/05/2026", "10:45", "Carrefour", totalDeCompra(16), 5),
        Compra(17, "07/05/2026", "08:30", "Coto",      totalDeCompra(17), 4),
    )

    // Devuelve las compras ordenadas de mayor a menor ID (más reciente primero)
    override fun obtenerCompras(): Flow<List<Compra>> =
        flow { emit(comprasMock.sortedByDescending { it.id }) }

    override suspend fun obtenerCompraPorId(id: Int) = comprasMock.find { it.id == id }
    override suspend fun insertarCompra(compra: Compra): Long = 0L
    override suspend fun actualizarCompra(compra: Compra) {}
    override suspend fun eliminarCompra(compra: Compra) {}
    override fun obtenerProductosPorCompra(compraId: Int): Flow<List<Producto>> = flow {
        emit(productosMock[compraId] ?: emptyList())
    }
    override suspend fun insertarProducto(producto: Producto) {}
    override suspend fun actualizarProducto(producto: Producto) {}
    override suspend fun eliminarProducto(producto: Producto) {}
}
