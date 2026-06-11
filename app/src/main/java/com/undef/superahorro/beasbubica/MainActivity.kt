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
import com.undef.superahorro.data.local.datastore.PreferenciasUsuario
import com.undef.superahorro.data.local.datastore.dataStore
import com.undef.superahorro.ui.navigation.GrafoNavegacion
import com.undef.superahorro.ui.theme.SuperAhorroTheme
import com.undef.superahorro.viewmodel.ViewModelAuth
import com.undef.superahorro.viewmodel.ViewModelMoneda
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

// Activity principal. Carga el modo oscuro desde DataStore al iniciar
// para que persista entre sesiones.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Leer el modo oscuro guardado antes de que se dibuje la UI (evita flash)
        val modoOscuroInicial = runBlocking {
            PreferenciasUsuario(applicationContext).flujoModoOscuro.first()
        }

        setContent {
            var modoOscuro by remember { mutableStateOf(modoOscuroInicial) }
            val preferencias = remember { PreferenciasUsuario(applicationContext) }

            // ViewModels globales que viven durante toda la sesión
            val viewModelAuth   = remember { ViewModelAuth(applicationContext) }
            val viewModelMoneda = remember { ViewModelMoneda(applicationContext) }

            SuperAhorroTheme(darkTheme = modoOscuro) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    GrafoNavegacion(
                        navController       = navController,
                        modoOscuro          = modoOscuro,
                        alCambiarModoOscuro = { nuevo ->
                            modoOscuro = nuevo
                            // Persistir el cambio en DataStore
                            kotlinx.coroutines.GlobalScope.launch {
                                preferencias.guardarModoOscuro(nuevo)
                            }
                        },
                        viewModelAuth       = viewModelAuth,
                        viewModelMoneda     = viewModelMoneda
                    )
                }
            }
        }
    }
}
