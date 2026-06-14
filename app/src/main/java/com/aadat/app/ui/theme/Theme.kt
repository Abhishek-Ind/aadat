package com.aadat.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    primaryContainer = PrimaryContainer,
    background = Background,
    surface = Surface,
    surfaceVariant = SurfaceVariant,
    onPrimary = OnPrimary,
    onBackground = OnBackground,
    onSurface = OnSurface,
    error = ErrorColor,
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    primaryContainer = PrimaryContainerDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onPrimary = OnPrimaryDark,
    onBackground = OnBackgroundDark,
    onSurface = OnSurfaceDark,
    error = ErrorColor,
)

@Composable
fun AadatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
