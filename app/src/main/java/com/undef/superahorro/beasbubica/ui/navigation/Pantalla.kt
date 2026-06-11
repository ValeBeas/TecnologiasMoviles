package com.undef.superahorro.ui.navigation

/**
 * Rutas de navegación de la app como sealed class.
 * Evita errores de tipeo al navegar entre pantallas.
 */
sealed class Pantalla(val ruta: String) {
    object Splash        : Pantalla("splash")
    object Login         : Pantalla("login")
    object Registro      : Pantalla("registro")
    object Inicio        : Pantalla("inicio")
    object ListaCompras  : Pantalla("lista_compras")
    object DetalleCompra : Pantalla("detalle_compra/{compraId}") {
        fun crearRuta(compraId: String) = "detalle_compra/$compraId"
    }
    object NuevaCompra   : Pantalla("nueva_compra")
    object NuevoProducto : Pantalla("nuevo_producto")   // sin argumento — solo desde NuevaCompra
    object Historial     : Pantalla("historial")
    object Estadisticas  : Pantalla("estadisticas")
    object Perfil        : Pantalla("perfil")
    object Configuracion : Pantalla("configuracion")
    object MisProductos  : Pantalla("mis_productos")
    object EditarCompra  : Pantalla("editar_compra/{compraId}") {
        fun crearRuta(compraId: String) = "editar_compra/$compraId"
    }
}
