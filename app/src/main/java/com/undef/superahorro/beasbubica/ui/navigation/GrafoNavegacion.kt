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
import com.undef.superahorro.ui.screens.purchases.PantallaEditarCompra
import com.undef.superahorro.ui.screens.purchases.PantallaListaCompras
import com.undef.superahorro.ui.screens.purchases.PantallaNuevaCompra
import com.undef.superahorro.ui.screens.settings.PantallaConfiguracion
import com.undef.superahorro.ui.screens.splash.PantallaSplash
import com.undef.superahorro.ui.screens.statistics.PantallaEstadisticas
import com.undef.superahorro.viewmodel.ViewModelAuth
import com.undef.superahorro.viewmodel.ViewModelMoneda
import com.undef.superahorro.viewmodel.ViewModelNuevaCompra


@Composable
/**
 * Define cómo se conectan todas las pantallas de la app.
 * ViewModelNuevaCompra y ViewModelAuth se comparten entre pantallas desde acá.
 */
fun GrafoNavegacion(
    navController: NavHostController,
    modoOscuro: Boolean,
    alCambiarModoOscuro: (Boolean) -> Unit,
    viewModelAuth: ViewModelAuth,
    viewModelMoneda: ViewModelMoneda
) {
    val viewModelNuevaCompra = remember { ViewModelNuevaCompra() }

    NavHost(navController = navController, startDestination = Pantalla.Splash.ruta) {

        composable(Pantalla.Splash.ruta) {
            PantallaSplash(
                viewModelAuth = viewModelAuth,
                alIrAlHome = {
                    navController.navigate(Pantalla.Inicio.ruta) {
                        popUpTo(Pantalla.Splash.ruta) { inclusive = true }
                    }
                },
                alIrAlLogin = {
                    navController.navigate(Pantalla.Login.ruta) {
                        popUpTo(Pantalla.Splash.ruta) { inclusive = true }
                    }
                }
            )
        }

        composable(Pantalla.Login.ruta) {
            PantallaLogin(
                viewModelAuth = viewModelAuth,
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
                viewModelAuth = viewModelAuth,
                alVolverAlLogin = { navController.popBackStack() },
                alRegistrarse = {
                    navController.navigate(Pantalla.Inicio.ruta) {
                        popUpTo(Pantalla.Login.ruta) { inclusive = true }
                    }
                }
            )
        }

        composable(Pantalla.Inicio.ruta) {
            PantallaInicio(
                navController        = navController,
                viewModelMoneda      = viewModelMoneda,
                alAgregarCompra      = { navController.navigate(Pantalla.NuevaCompra.ruta) },
                alVerDetalleCompra   = { id -> navController.navigate(Pantalla.DetalleCompra.crearRuta(id)) },
                alAbrirConfiguracion = { navController.navigate(Pantalla.Configuracion.ruta) }
            )
        }

        composable(Pantalla.ListaCompras.ruta) {
            PantallaListaCompras(
                navController   = navController,
                viewModelMoneda = viewModelMoneda,
                alVerDetalle    = { id -> navController.navigate(Pantalla.DetalleCompra.crearRuta(id)) },
                alAgregarCompra = { navController.navigate(Pantalla.NuevaCompra.ruta) },
                alVolverAtras   = { navController.popBackStack() }
            )
        }

        composable(
            route     = Pantalla.DetalleCompra.ruta,
            arguments = listOf(navArgument("compraId") { type = NavType.StringType })
        ) { entrada ->
            val compraId = entrada.arguments?.getString("compraId") ?: ""
            PantallaDetalleCompra(
                compraId        = compraId,
                viewModelMoneda = viewModelMoneda,
                alVolverAtras   = { navController.popBackStack() },
                alEditarCompra  = { id -> navController.navigate(Pantalla.EditarCompra.crearRuta(id)) }
            )
        }

        composable(Pantalla.NuevaCompra.ruta) {
            PantallaNuevaCompra(
                navController = navController,
                viewModel     = viewModelNuevaCompra,
                alVolverAtras = { navController.popBackStack() }
            )
        }

        composable(Pantalla.NuevoProducto.ruta) {
            PantallaNuevoProducto(
                alAgregarProducto = { producto ->
                    viewModelNuevaCompra.agregarProducto(producto)
                    navController.popBackStack()
                },
                alVolverAtras = { navController.popBackStack() }
            )
        }

        composable(Pantalla.Historial.ruta) {
            PantallaHistorial(
                navController   = navController,
                viewModelMoneda = viewModelMoneda,
                alVerDetalle    = { id -> navController.navigate(Pantalla.DetalleCompra.crearRuta(id)) },
                alVolverAtras   = { navController.popBackStack() }
            )
        }

        composable(Pantalla.Estadisticas.ruta) {
            PantallaEstadisticas(
                navController   = navController,
                viewModelMoneda = viewModelMoneda,
                alVolverAtras   = { navController.popBackStack() }
            )
        }

        composable(Pantalla.Perfil.ruta) {
            PantallaPerfil(
                navController  = navController,
                viewModelAuth  = viewModelAuth,
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
                modoOscuro      = modoOscuro,
                alCambiarModo   = alCambiarModoOscuro,
                viewModelMoneda = viewModelMoneda,
                alVolverAtras   = { navController.popBackStack() }
            )
        }

        composable(
            route     = Pantalla.EditarCompra.ruta,
            arguments = listOf(navArgument("compraId") { type = NavType.StringType })
        ) { entrada ->
            val compraIdEditar = entrada.arguments?.getString("compraId") ?: ""
            PantallaEditarCompra(
                compraId      = compraIdEditar,
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
