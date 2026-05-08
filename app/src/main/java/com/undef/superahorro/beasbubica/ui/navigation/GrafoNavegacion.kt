package com.undef.superahorro.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.undef.superahorro.ui.screens.auth.PantallaLogin
import com.undef.superahorro.ui.screens.auth.PantallaRegistro
import com.undef.superahorro.ui.screens.history.PantallaHistorial
import com.undef.superahorro.ui.screens.home.PantallaInicio
import com.undef.superahorro.ui.screens.products.PantallaMisProductos
import com.undef.superahorro.ui.screens.products.PantallaNuevoProducto
import com.undef.superahorro.ui.screens.profile.PantallaPerfil
import com.undef.superahorro.ui.screens.purchases.PantallaDetalleCompra
import com.undef.superahorro.ui.screens.purchases.PantallaListaCompras
import com.undef.superahorro.ui.screens.purchases.PantallaNuevaCompra
import com.undef.superahorro.ui.screens.settings.PantallaConfiguracion
import com.undef.superahorro.ui.screens.splash.PantallaSplash
import com.undef.superahorro.ui.screens.statistics.PantallaEstadisticas
import com.undef.superahorro.viewmodel.ViewModelNuevaCompra

/**
 * Mapa de navegación completo de la app.
 * Creo el ViewModelNuevaCompra una sola vez acá con remember y lo comparto
 * entre NuevaCompra y NuevoProducto para que los productos no se pierdan al navegar.
 */
@Composable
fun GrafoNavegacion(
    navController: NavHostController,
    modoOscuro: Boolean,
    alCambiarModoOscuro: (Boolean) -> Unit
) {
    // ViewModel compartido entre NuevaCompra y NuevoProducto
    val viewModelNuevaCompra = remember { ViewModelNuevaCompra() }

    NavHost(navController = navController, startDestination = Pantalla.Splash.ruta) {

        composable(Pantalla.Splash.ruta) {
            PantallaSplash(alNavegar = {
                navController.navigate(Pantalla.Login.ruta) {
                    popUpTo(Pantalla.Splash.ruta) { inclusive = true }
                }
            })
        }

        composable(Pantalla.Login.ruta) {
            PantallaLogin(
                alIniciarSesion = {
                    navController.navigate(Pantalla.Inicio.ruta) {
                        popUpTo(Pantalla.Login.ruta) { inclusive = true }
                    }
                },
                alIrARegistro = { navController.navigate(Pantalla.Registro.ruta) }
            )
        }

        composable(Pantalla.Registro.ruta) {
            PantallaRegistro(
                alVolverAlLogin = { navController.popBackStack() },
                alRegistrarse   = {
                    navController.navigate(Pantalla.Inicio.ruta) {
                        popUpTo(Pantalla.Login.ruta) { inclusive = true }
                    }
                }
            )
        }

        composable(Pantalla.Inicio.ruta) {
            PantallaInicio(
                navController        = navController,
                alAgregarCompra      = { navController.navigate(Pantalla.NuevaCompra.ruta) },
                alVerDetalleCompra   = { id -> navController.navigate(Pantalla.DetalleCompra.crearRuta(id)) },
                alAbrirConfiguracion = { navController.navigate(Pantalla.Configuracion.ruta) }
            )
        }

        composable(Pantalla.ListaCompras.ruta) {
            PantallaListaCompras(
                navController   = navController,
                alVerDetalle    = { id -> navController.navigate(Pantalla.DetalleCompra.crearRuta(id)) },
                alAgregarCompra = { navController.navigate(Pantalla.NuevaCompra.ruta) },
                alVolverAtras   = { navController.popBackStack() }
            )
        }

        composable(
            route     = Pantalla.DetalleCompra.ruta,
            arguments = listOf(navArgument("compraId") { type = NavType.IntType })
        ) { entrada ->
            val compraId = entrada.arguments?.getInt("compraId") ?: 0
            // Sin alAgregarProducto — ya no se puede agregar productos desde el historial
            PantallaDetalleCompra(
                compraId      = compraId,
                alVolverAtras = { navController.popBackStack() }
            )
        }

        // NuevaCompra recibe el ViewModel compartido con NuevoProducto
        composable(Pantalla.NuevaCompra.ruta) {
            PantallaNuevaCompra(
                navController = navController,
                viewModel     = viewModelNuevaCompra,
                alVolverAtras = { navController.popBackStack() }
            )
        }

        // NuevoProducto: solo accesible desde NuevaCompra (no hay ruta desde historial)
        // Al agregar, deposita el producto en el ViewModel compartido y vuelve
        composable(Pantalla.NuevoProducto.ruta) {
            PantallaNuevoProducto(
                alAgregarProducto = { producto ->
                    viewModelNuevaCompra.agregarProducto(producto)
                    navController.popBackStack()      // vuelve a NuevaCompra
                },
                alVolverAtras = { navController.popBackStack() }
            )
        }

        composable(Pantalla.Historial.ruta) {
            PantallaHistorial(
                navController = navController,
                alVerDetalle  = { id -> navController.navigate(Pantalla.DetalleCompra.crearRuta(id)) },
                alVolverAtras = { navController.popBackStack() }
            )
        }

        composable(Pantalla.Estadisticas.ruta) {
            PantallaEstadisticas(
                navController = navController,
                alVolverAtras = { navController.popBackStack() }
            )
        }

        composable(Pantalla.Perfil.ruta) {
            PantallaPerfil(
                navController  = navController,
                alVolverAtras  = { navController.popBackStack() },
                alCerrarSesion = {
                    navController.navigate(Pantalla.Login.ruta) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Pantalla.Configuracion.ruta) {
            PantallaConfiguracion(
                modoOscuro    = modoOscuro,
                alCambiarModo = alCambiarModoOscuro,
                alVolverAtras = { navController.popBackStack() }
            )
        }

        composable(Pantalla.MisProductos.ruta) {
            PantallaMisProductos(
                navController = navController,
                alVolverAtras = { navController.popBackStack() }
            )
        }
    }
}
