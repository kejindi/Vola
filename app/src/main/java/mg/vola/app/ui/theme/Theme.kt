package com.vola.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreen,
    secondary = CoralOrange,
    tertiary = CobaltBlue,
    background = Neutral900,
    surface = Neutral800,
    onPrimary = White,
    onSecondary = White,
    onTertiary = White,
    onBackground = Neutral100,
    onSurface = Neutral100,
    error = ErrorRed,
    onError = White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    secondary = CoralOrange,
    tertiary = CobaltBlue,
    background = Neutral50,
    surface = White,
    onPrimary = White,
    onSecondary = White,
    onTertiary = White,
    onBackground = Neutral900,
    onSurface = Neutral900,
    error = ErrorRed,
    onError = White
)

@Composable
fun VolaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as androidx.activity.ComponentActivity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = VolaTypography,
        shapes = MaterialTheme.shapes.copy(
            extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
            small = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
            medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            large = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(32.dp)
        ),
        content = content
    )
}

// Dark theme check
@Composable
fun isDarkTheme(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f

// Status bar color
@Composable
fun StatusBarColor(color: Color = MaterialTheme.colorScheme.primary) {
    val view = LocalView.current
    SideEffect {
        val window = (view.context as androidx.activity.ComponentActivity).window
        window.statusBarColor = color.toArgb()
    }
}

// Navigation bar color
@Composable
fun NavigationBarColor(color: Color = MaterialTheme.colorScheme.background) {
    val view = LocalView.current
    SideEffect {
        val window = (view.context as androidx.activity.ComponentActivity).window
        window.navigationBarColor = color.toArgb()
    }
}