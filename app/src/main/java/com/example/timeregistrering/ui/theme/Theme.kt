package com.example.timeregistrering.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SamsungLightColors = lightColorScheme(
    primary = Color(0xFF1A73E8), // Samsung Blue
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8F0FE),
    onPrimaryContainer = Color(0xFF1967D2),
    secondary = Color(0xFF4285F4),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8F0FE),
    onSecondaryContainer = Color(0xFF1967D2),
    tertiary = Color(0xFF5F6368),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF1F3F4),
    onTertiaryContainer = Color(0xFF202124),
    error = Color(0xFFD93025),
    onError = Color.White,
    errorContainer = Color(0xFFFFE9E8),
    onErrorContainer = Color(0xFFB3261E),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF202124),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF202124),
    surfaceVariant = Color(0xFFF1F3F4),
    onSurfaceVariant = Color(0xFF5F6368),
    outline = Color(0xFFDADCE0)
)

private val SamsungDarkColors = darkColorScheme(
    primary = Color(0xFF8AB4F8),
    onPrimary = Color(0xFF202124),
    primaryContainer = Color(0xFF1967D2),
    onPrimaryContainer = Color(0xFFE8F0FE),
    secondary = Color(0xFF8AB4F8),
    onSecondary = Color(0xFF202124),
    secondaryContainer = Color(0xFF1967D2),
    onSecondaryContainer = Color(0xFFE8F0FE),
    tertiary = Color(0xFFBDC1C6),
    onTertiary = Color(0xFF202124),
    tertiaryContainer = Color(0xFF5F6368),
    onTertiaryContainer = Color(0xFFF1F3F4),
    error = Color(0xFFF28B82),
    onError = Color(0xFF202124),
    errorContainer = Color(0xFFB3261E),
    onErrorContainer = Color(0xFFFFE9E8),
    background = Color(0xFF202124),
    onBackground = Color(0xFFE8EAED),
    surface = Color(0xFF202124),
    onSurface = Color(0xFFE8EAED),
    surfaceVariant = Color(0xFF303134),
    onSurfaceVariant = Color(0xFFBDC1C6),
    outline = Color(0xFF5F6368)
)

@Composable
fun TimeregistreringTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> SamsungDarkColors
        else -> SamsungLightColors
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
