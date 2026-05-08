package com.undef.superahorro.ui.navigation

/**
 * Todas las rutas de la app. Uso sealed class que el compilador me avise si me equivoco en el nombre.
 * NuevoProducto solo se puede abrir desde NuevaCompra, no desde el historial.
 */
sealed class Pantalla(val ruta: String) {
    object Splash        : Pantalla("splash")
    object Login         : Pantalla("login")
    object Registro      : Pantalla("registro")
    object Inicio        : Pantalla("inicio")
    object ListaCompras  : Pantalla("lista_compras")
    object DetalleCompra : Pantalla("detalle_compra/{compraId}") {
        fun crearRuta(compraId: Int) = "detalle_compra/$compraId"
    }
    object NuevaCompra   : Pantalla("nueva_compra")
    object NuevoProducto : Pantalla("nuevo_producto")   // sin argumento — solo desde NuevaCompra
    object Historial     : Pantalla("historial")
    object Estadisticas  : Pantalla("estadisticas")
    object Perfil        : Pantalla("perfil")
    object Configuracion : Pantalla("configuracion")
    object MisProductos  : Pantalla("mis_productos")
}
