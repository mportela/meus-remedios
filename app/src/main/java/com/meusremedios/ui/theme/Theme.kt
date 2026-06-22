package com.meusremedios.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

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

/**
 * Tema do app. Sem cores dinâmicas para garantir contraste previsível e
 * consistente em todos os dispositivos (acessibilidade).
 */
@Composable
fun MeusRemediosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
