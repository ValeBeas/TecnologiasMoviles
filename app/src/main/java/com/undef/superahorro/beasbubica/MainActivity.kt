package com.undef.superahorro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.undef.superahorro.ui.navigation.GrafoNavegacion
import com.undef.superahorro.ui.theme.SuperAhorroTheme

/**
 * Única Activity de la app. Acá guardo el estado del modo oscuro y arranco el NavHost.
 * En la Entrega 2 el modo oscuro se va a guardar en DataStore para que persista.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Estado del modo oscuro — se pasa hacia abajo hasta PantallaConfiguracion
            var modoOscuro by remember { mutableStateOf(false) }

            SuperAhorroTheme(darkTheme = modoOscuro) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    GrafoNavegacion(
                        navController       = navController,
                        modoOscuro          = modoOscuro,
                        alCambiarModoOscuro = { modoOscuro = it }
                    )
                }
            }
        }
    }
}
