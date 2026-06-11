package com.undef.superahorro.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --- Esquema de colores CLARO ---
/**
 * Tema visual de la app con soporte para modo claro y modo oscuro.
 * SuperAhorroTheme() es la función que envuelve toda la app en MainActivity.
 */
private val EsquemaClaro = lightColorScheme(
    primary            = IndigoDeep,
    onPrimary          = SurfaceWhite,
    primaryContainer   = ColorSuccessLight,
    onPrimaryContainer = IndigoDeep,
    secondary          = IndigoMedium,
    onSecondary        = SurfaceWhite,
    secondaryContainer = DividerColor,
    onSecondaryContainer = TextPrimary,
    tertiary           = CoralVibrant,
    onTertiary         = SurfaceWhite,
    tertiaryContainer  = CoralLight,
    onTertiaryContainer = ColorError,
    background         = SurfaceLight,
    onBackground       = TextPrimary,
    surface            = SurfaceWhite,
    onSurface          = TextPrimary,
    surfaceVariant     = SurfaceLight,
    onSurfaceVariant   = TextSecondary,
    outline            = BorderColor,
    outlineVariant     = DividerColor,
    error              = ColorError,
    onError            = SurfaceWhite,
    errorContainer     = ColorErrorLight,
    onErrorContainer   = ColorError,
)

// --- Esquema de colores OSCURO ---
private val EsquemaOscuro = darkColorScheme(
    primary            = IndigoLight,
    onPrimary          = DarkBackground,
    primaryContainer   = IndigoDeep,
    onPrimaryContainer = DarkTextPrimary,
    secondary          = IndigoMedium,
    onSecondary        = DarkTextPrimary,
    secondaryContainer = DarkCard,
    onSecondaryContainer = DarkTextSecond,
    tertiary           = CoralVibrant,
    onTertiary         = DarkBackground,
    tertiaryContainer  = ColorErrorLight,
    onTertiaryContainer = ColorError,
    background         = DarkBackground,
    onBackground       = DarkTextPrimary,
    surface            = DarkSurface,
    onSurface          = DarkTextPrimary,
    surfaceVariant     = DarkCard,
    onSurfaceVariant   = DarkTextSecond,
    outline            = DarkCard,
    outlineVariant     = DarkSurface,
    error              = CoralVibrant,
    onError            = DarkBackground,
    errorContainer     = ColorErrorLight,
    onErrorContainer   = ColorError,
)

// --- Tema principal de la app ---
// darkTheme: si true usa esquema oscuro, si false usa claro
@Composable
fun SuperAhorroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val esquema = if (darkTheme) EsquemaOscuro else EsquemaClaro

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val ventana = (view.context as Activity).window
            ventana.statusBarColor = esquema.primary.toArgb()
            WindowCompat.getInsetsController(ventana, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = esquema,
        typography  = AppTypography,
        shapes      = AppShapes,
        content     = content
    )
}
