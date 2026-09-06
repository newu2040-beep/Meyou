package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val MeyouLightColorScheme = lightColorScheme(
    primary = MeyouViolet,
    onPrimary = Color.White,
    primaryContainer = MeyouLavenderContainer,
    onPrimaryContainer = Color(0xFF22005D),
    secondary = Color(0xFF625B71),
    onSecondary = Color.White,
    secondaryContainer = MeyouLavenderLight,
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFF7D5260),
    onTertiary = Color.White,
    tertiaryContainer = MeyouSoftPink,
    onTertiaryContainer = Color(0xFF31111D),
    background = MeyouCreamLight,
    onBackground = MeyouOnSurfaceLight,
    surface = MeyouIvorySurface,
    onSurface = MeyouOnSurfaceLight,
    surfaceVariant = MeyouIvorySurfaceVariant,
    onSurfaceVariant = MeyouOnSurfaceVariantLight,
    surfaceContainer = MeyouIvoryContainer,
    outline = MeyouOutlineLight,
    outlineVariant = MeyouOutlineVariantLight
)

val MeyouDarkColorScheme = darkColorScheme(
    primary = MeyouVioletDark,
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = MeyouLavender,
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFFEFB8C8),
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),
    background = MeyouCharcoalDark,
    onBackground = MeyouOnSurfaceDark,
    surface = MeyouCharcoalSurface,
    onSurface = MeyouOnSurfaceDark,
    surfaceVariant = MeyouCharcoalSurfaceVariant,
    onSurfaceVariant = MeyouOnSurfaceVariantDark,
    surfaceContainer = MeyouCharcoalContainer,
    outline = MeyouOutlineDark,
    outlineVariant = MeyouOutlineVariantDark
)

@Composable
fun MEYOUTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> MeyouDarkColorScheme
        else -> MeyouLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MeyouTypography,
        shapes = MeyouShapes,
        content = content
    )
}

// Backward compatibility alias for test harness
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    MEYOUTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
