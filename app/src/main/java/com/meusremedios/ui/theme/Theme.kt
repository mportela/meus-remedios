package com.meusremedios.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

private val LightColors = lightColorScheme(
    primary = Blue40,
    secondary = Teal40,
    background = Neutral99,
    surface = Neutral99,
    error = Error40,
)

private val DarkColors = darkColorScheme(
    primary = Blue80,
    secondary = Teal80,
    background = Neutral10,
    surface = Neutral10,
    error = Error80,
)

private val HighContrastColors = lightColorScheme(
    primary = HCPrimary,
    onPrimary = HCOnPrimary,
    background = HCBackground,
    onBackground = HCOnBackground,
    surface = HCSurface,
    onSurface = HCOnSurface,
    error = HCError,
)

/**
 * Tema do app. Sem cores dinâmicas para garantir contraste previsível e
 * consistente em todos os dispositivos (acessibilidade).
 *
 * @param fontScale escala de fonte extra (1.15f = Grande, 1.30f = Maior).
 *   Quando null, respeita a configuração do sistema sem sobrescrever.
 * @param highContrast quando true, usa paleta de alto contraste.
 */
@Composable
fun MeusRemediosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontScale: Float? = null,
    highContrast: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        highContrast -> HighContrastColors
        darkTheme -> DarkColors
        else -> LightColors
    }
    val themedContent: @Composable () -> Unit = {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
    if (fontScale != null) {
        val currentDensity = LocalDensity.current
        CompositionLocalProvider(
            LocalDensity provides Density(currentDensity.density, fontScale),
            content = themedContent,
        )
    } else {
        themedContent()
    }
}
