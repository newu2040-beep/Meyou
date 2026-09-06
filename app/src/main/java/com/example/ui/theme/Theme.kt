package com.example.ui.theme

import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.datastore.AppThemeScheme

data class AppDimensions(
    val isCompact: Boolean = false,
    val screenHorizontalPadding: Dp = if (isCompact) 12.dp else 20.dp,
    val cardPadding: Dp = if (isCompact) 12.dp else 18.dp,
    val itemSpacing: Dp = if (isCompact) 8.dp else 16.dp,
    val sectionSpacing: Dp = if (isCompact) 14.dp else 22.dp,
    val avatarSize: Dp = if (isCompact) 64.dp else 80.dp,
    val iconSize: Dp = if (isCompact) 20.dp else 24.dp
)

val LocalAppDimensions = staticCompositionLocalOf { AppDimensions() }

// Theme 1: Violet (Lavender Dream)
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

// Theme 2: Sage (Forest Sage)
val SageLightColorScheme = lightColorScheme(
    primary = MeyouSagePrimary,
    onPrimary = Color.White,
    primaryContainer = MeyouSageContainer,
    onPrimaryContainer = Color(0xFF002114),
    secondary = Color(0xFF4E6355),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1E8D6),
    onSecondaryContainer = Color(0xFF0B1F14),
    tertiary = Color(0xFF3C6472),
    onTertiary = Color.White,
    background = Color(0xFFFCFDF7),
    onBackground = Color(0xFF191C1A),
    surface = Color(0xFFF7FAF4),
    onSurface = Color(0xFF191C1A),
    surfaceVariant = Color(0xFFDEE5DE),
    onSurfaceVariant = Color(0xFF424943),
    surfaceContainer = Color(0xFFE8EFE8),
    outline = Color(0xFF727972),
    outlineVariant = Color(0xFFC2C9C2)
)

val SageDarkColorScheme = darkColorScheme(
    primary = MeyouSagePrimaryDark,
    onPrimary = Color(0xFF003822),
    primaryContainer = MeyouSageContainerDark,
    onPrimaryContainer = MeyouSageContainer,
    secondary = Color(0xFFB5CCBB),
    onSecondary = Color(0xFF203528),
    secondaryContainer = Color(0xFF374B3E),
    onSecondaryContainer = Color(0xFFD1E8D6),
    tertiary = Color(0xFFA4CDDD),
    onTertiary = Color(0xFF043542),
    background = Color(0xFF111412),
    onBackground = Color(0xFFE1E3DF),
    surface = Color(0xFF181C19),
    onSurface = Color(0xFFE1E3DF),
    surfaceVariant = Color(0xFF262C27),
    onSurfaceVariant = Color(0xFFC2C9C2),
    surfaceContainer = Color(0xFF2F3530),
    outline = Color(0xFF8C938C),
    outlineVariant = Color(0xFF424943)
)

// Theme 3: Terracotta (Terracotta Amber)
val TerracottaLightColorScheme = lightColorScheme(
    primary = MeyouTerracottaPrimary,
    onPrimary = Color.White,
    primaryContainer = MeyouTerracottaContainer,
    onPrimaryContainer = Color(0xFF360F00),
    secondary = Color(0xFF77574B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDBCF),
    onSecondaryContainer = Color(0xFF2C160D),
    tertiary = Color(0xFF6B5E2F),
    onTertiary = Color.White,
    background = Color(0xFFFFFBF9),
    onBackground = Color(0xFF201A18),
    surface = Color(0xFFFAF5F1),
    onSurface = Color(0xFF201A18),
    surfaceVariant = Color(0xFFF5DED6),
    onSurfaceVariant = Color(0xFF53433E),
    surfaceContainer = Color(0xFFEFE6E0),
    outline = Color(0xFF85736D),
    outlineVariant = Color(0xFFD8C2BB)
)

val TerracottaDarkColorScheme = darkColorScheme(
    primary = MeyouTerracottaPrimaryDark,
    onPrimary = Color(0xFF5C1E00),
    primaryContainer = MeyouTerracottaContainerDark,
    onPrimaryContainer = MeyouTerracottaContainer,
    secondary = Color(0xFFE7BEAF),
    onSecondary = Color(0xFF442A20),
    secondaryContainer = Color(0xFF5D4035),
    onSecondaryContainer = Color(0xFFFFDBCF),
    tertiary = Color(0xFFD7C68D),
    onTertiary = Color(0xFF3A3005),
    background = Color(0xFF171210),
    onBackground = Color(0xFFEDE0DC),
    surface = Color(0xFF201A17),
    onSurface = Color(0xFFEDE0DC),
    surfaceVariant = Color(0xFF2D2421),
    onSurfaceVariant = Color(0xFFD8C2BB),
    surfaceContainer = Color(0xFF382F2B),
    outline = Color(0xFFA08C86),
    outlineVariant = Color(0xFF53433E)
)

// Theme 4: Ocean (Ocean Indigo)
val OceanLightColorScheme = lightColorScheme(
    primary = MeyouOceanPrimary,
    onPrimary = Color.White,
    primaryContainer = MeyouOceanContainer,
    onPrimaryContainer = Color(0xFF001D32),
    secondary = Color(0xFF4F6070),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD2E5F8),
    onSecondaryContainer = Color(0xFF0B1D2B),
    tertiary = Color(0xFF64597B),
    onTertiary = Color.White,
    background = Color(0xFFF8FAFD),
    onBackground = Color(0xFF191C1E),
    surface = Color(0xFFF2F6FA),
    onSurface = Color(0xFF191C1E),
    surfaceVariant = Color(0xFFDDE3EA),
    onSurfaceVariant = Color(0xFF41474D),
    surfaceContainer = Color(0xFFE3EAF2),
    outline = Color(0xFF71787E),
    outlineVariant = Color(0xFFC1C7CE)
)

val OceanDarkColorScheme = darkColorScheme(
    primary = MeyouOceanPrimaryDark,
    onPrimary = Color(0xFF003351),
    primaryContainer = MeyouOceanContainerDark,
    onPrimaryContainer = MeyouOceanContainer,
    secondary = Color(0xFFB7C9DB),
    onSecondary = Color(0xFF213241),
    secondaryContainer = Color(0xFF374958),
    onSecondaryContainer = Color(0xFFD2E5F8),
    tertiary = Color(0xFFCEC0E8),
    onTertiary = Color(0xFF352B4B),
    background = Color(0xFF101417),
    onBackground = Color(0xFFE1E2E5),
    surface = Color(0xFF171C20),
    onSurface = Color(0xFFE1E2E5),
    surfaceVariant = Color(0xFF242B31),
    onSurfaceVariant = Color(0xFFC1C7CE),
    surfaceContainer = Color(0xFF2D353C),
    outline = Color(0xFF8B9298),
    outlineVariant = Color(0xFF41474D)
)

// Theme 5: Rose (Rose Quartz)
val RoseLightColorScheme = lightColorScheme(
    primary = MeyouRosePrimary,
    onPrimary = Color.White,
    primaryContainer = MeyouRoseContainer,
    onPrimaryContainer = Color(0xFF3B071B),
    secondary = Color(0xFF74565F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFD9E2),
    onSecondaryContainer = Color(0xFF2B151C),
    tertiary = Color(0xFF7C5635),
    onTertiary = Color.White,
    background = Color(0xFFFFF8F8),
    onBackground = Color(0xFF201A1B),
    surface = Color(0xFFFAF3F4),
    onSurface = Color(0xFF201A1B),
    surfaceVariant = Color(0xFFF2DDE1),
    onSurfaceVariant = Color(0xFF514347),
    surfaceContainer = Color(0xFFEFE2E5),
    outline = Color(0xFF837377),
    outlineVariant = Color(0xFFD5C2C6)
)

val RoseDarkColorScheme = darkColorScheme(
    primary = MeyouRosePrimaryDark,
    onPrimary = Color(0xFF541227),
    primaryContainer = MeyouRoseContainerDark,
    onPrimaryContainer = MeyouRoseContainer,
    secondary = Color(0xFFE2BDC6),
    onSecondary = Color(0xFF422931),
    secondaryContainer = Color(0xFF5A3F47),
    onSecondaryContainer = Color(0xFFFFD9E2),
    tertiary = Color(0xFFEEBD94),
    onTertiary = Color(0xFF472A0C),
    background = Color(0xFF191113),
    onBackground = Color(0xFFECE0E1),
    surface = Color(0xFF211719),
    onSurface = Color(0xFFECE0E1),
    surfaceVariant = Color(0xFF2E2225),
    onSurfaceVariant = Color(0xFFD5C2C6),
    surfaceContainer = Color(0xFF3A2C30),
    outline = Color(0xFF9E8C90),
    outlineVariant = Color(0xFF514347)
)

// Theme 6: Monochrome (Onyx Slate)
val MonoLightColorScheme = lightColorScheme(
    primary = MeyouMonoPrimary,
    onPrimary = Color.White,
    primaryContainer = MeyouMonoContainer,
    onPrimaryContainer = Color(0xFF101418),
    secondary = Color(0xFF5B6065),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDFE3E8),
    onSecondaryContainer = Color(0xFF181C21),
    tertiary = Color(0xFF5E5E62),
    onTertiary = Color.White,
    background = Color(0xFFFBFBFB),
    onBackground = Color(0xFF191C1E),
    surface = Color(0xFFF4F5F6),
    onSurface = Color(0xFF191C1E),
    surfaceVariant = Color(0xFFDFE2E6),
    onSurfaceVariant = Color(0xFF43474B),
    surfaceContainer = Color(0xFFE8EBEE),
    outline = Color(0xFF73777C),
    outlineVariant = Color(0xFFC3C7CC)
)

val MonoDarkColorScheme = darkColorScheme(
    primary = MeyouMonoPrimaryDark,
    onPrimary = Color(0xFF14171A),
    primaryContainer = MeyouMonoContainerDark,
    onPrimaryContainer = MeyouMonoContainer,
    secondary = Color(0xFFC3C7CC),
    onSecondary = Color(0xFF2D3136),
    secondaryContainer = Color(0xFF43474C),
    onSecondaryContainer = Color(0xFFDFE3E8),
    tertiary = Color(0xFFC6C6CA),
    onTertiary = Color(0xFF2F3034),
    background = Color(0xFF111315),
    onBackground = Color(0xFFE2E2E5),
    surface = Color(0xFF191B1E),
    onSurface = Color(0xFFE2E2E5),
    surfaceVariant = Color(0xFF272A2E),
    onSurfaceVariant = Color(0xFFC3C7CC),
    surfaceContainer = Color(0xFF313539),
    outline = Color(0xFF8D9196),
    outlineVariant = Color(0xFF43474B)
)

fun getAppColorScheme(
    scheme: AppThemeScheme,
    darkTheme: Boolean,
    context: Context
): ColorScheme {
    return when (scheme) {
        AppThemeScheme.DYNAMIC -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (darkTheme) MeyouDarkColorScheme else MeyouLightColorScheme
            }
        }
        AppThemeScheme.VIOLET -> if (darkTheme) MeyouDarkColorScheme else MeyouLightColorScheme
        AppThemeScheme.SAGE -> if (darkTheme) SageDarkColorScheme else SageLightColorScheme
        AppThemeScheme.TERRACOTTA -> if (darkTheme) TerracottaDarkColorScheme else TerracottaLightColorScheme
        AppThemeScheme.OCEAN -> if (darkTheme) OceanDarkColorScheme else OceanLightColorScheme
        AppThemeScheme.ROSE -> if (darkTheme) RoseDarkColorScheme else RoseLightColorScheme
        AppThemeScheme.MONOCHROME -> if (darkTheme) MonoDarkColorScheme else MonoLightColorScheme
    }
}

@Composable
fun MEYOUTheme(
    themeScheme: AppThemeScheme = AppThemeScheme.VIOLET,
    darkTheme: Boolean = isSystemInDarkTheme(),
    isCompactMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = getAppColorScheme(themeScheme, darkTheme, context)
    val dimensions = AppDimensions(isCompact = isCompactMode)

    CompositionLocalProvider(LocalAppDimensions provides dimensions) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MeyouTypography,
            shapes = MeyouShapes,
            content = content
        )
    }
}

// Backward compatibility alias for test harness
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    MEYOUTheme(
        themeScheme = if (dynamicColor) AppThemeScheme.DYNAMIC else AppThemeScheme.VIOLET,
        darkTheme = darkTheme,
        content = content
    )
}
