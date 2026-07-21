package com.undef.superahorro.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.undef.superahorro.R
import com.undef.superahorro.ui.theme.*
import com.undef.superahorro.viewmodel.ViewModelAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
/**
 * Pantalla de bienvenida con animación de entrada.
 * Verifica la sesión guardada y redirige al Home o al Login.
 */
fun PantallaSplash(
    viewModelAuth: ViewModelAuth,
    alIrAlHome: () -> Unit,
    alIrAlLogin: () -> Unit
) {
    val alpha = remember { Animatable(0f) }
    val escala = remember { Animatable(0.7f) }

    LaunchedEffect(Unit) {
        launch { alpha.animateTo(1f, animationSpec = tween(800)) }
        launch {
            escala.animateTo(
                1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        // Esperar la animación y verificar sesión
        delay(1500)
        viewModelAuth.verificarSesion(
            onExito = alIrAlHome,
            onSinSesion = alIrAlLogin
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(IndigoDeep, IndigoMedium))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .alpha(alpha.value)
                .scale(escala.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Outlined.ShoppingCart,
                contentDescription = null,
                tint = SurfaceWhite,
                modifier = Modifier.size(88.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.app_brand),
                style = MaterialTheme.typography.headlineLarge,
                color = SurfaceWhite,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = SurfaceWhite.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(48.dp))
            CircularProgressIndicator(
                color = SurfaceWhite.copy(alpha = 0.7f),
                strokeWidth = 2.dp,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
