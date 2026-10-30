package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppThemeColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceSecondary: Color,
    val surfaceVariant: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val inputContainer: Color,
    val primary: Color,
    val accent: Color
)

val DarkColors = AppThemeColors(
    isDark = true,
    background = Slate900,
    surface = Slate800,
    surfaceSecondary = Color(0xFF0F172A),
    surfaceVariant = Slate700,
    border = Slate700,
    textPrimary = Color.White,
    textSecondary = Slate400,
    textMuted = Slate500,
    inputContainer = Slate800,
    primary = BlueAccent,
    accent = PurpleAccent
)

val LightColors = AppThemeColors(
    isDark = false,
    background = Color(0xFFF1F5F9), // Clean light slate background
    surface = Color(0xFFFFFFFF),    // Pure white cards
    surfaceSecondary = Color(0xFFE2E8F0), // Slate 200 secondary
    surfaceVariant = Color(0xFFF8FAFC),   // Slate 50
    border = Color(0xFFCBD5E1),           // Slate 300 border
    textPrimary = Color(0xFF0F172A),      // Slate 900 text
    textSecondary = Color(0xFF475569),    // Slate 600 secondary text
    textMuted = Color(0xFF94A3B8),        // Slate 400 muted text
    inputContainer = Color(0xFFFFFFFF),   // White input container
    primary = BlueAccentDark,
    accent = PurpleAccent
)

val LocalAppThemeColors = staticCompositionLocalOf { DarkColors }

object AppTheme {
    val colors: AppThemeColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppThemeColors.current
}

private val DarkColorScheme = darkColorScheme(
    primary = BlueAccent,
    onPrimary = Color.White,
    primaryContainer = Slate800,
    onPrimaryContainer = Color.White,
    secondary = PurpleAccent,
    onSecondary = Color.White,
    background = Slate900,
    onBackground = Color.White,
    surface = Slate800,
    onSurface = Color.White,
    surfaceVariant = Slate700,
    onSurfaceVariant = Slate300,
    outline = Slate700,
    error = RedDanger,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = BlueAccentDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE2E8F0),
    onPrimaryContainer = Slate900,
    secondary = PurpleAccent,
    onSecondary = Color.White,
    background = Color(0xFFF1F5F9),
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Color(0xFFF8FAFC),
    onSurfaceVariant = Slate700,
    outline = Color(0xFFCBD5E1),
    error = RedDanger,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val appThemeColors = if (darkTheme) DarkColors else LightColors

    CompositionLocalProvider(LocalAppThemeColors provides appThemeColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
