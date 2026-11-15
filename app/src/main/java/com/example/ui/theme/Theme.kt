package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.LocalAppLanguage

private val DarkColorScheme = darkColorScheme(
    primary = DarkEmeraldPrimary,
    onPrimary = DarkEmeraldOnPrimary,
    primaryContainer = DarkEmeraldContainer,
    onPrimaryContainer = DarkEmeraldOnContainer,
    secondary = DarkMarigoldSecondary,
    onSecondary = DarkMarigoldOnSecondary,
    secondaryContainer = DarkMarigoldContainer,
    onSecondaryContainer = DarkMarigoldOnSecondaryContainer,
    tertiary = DarkEarthTertiary,
    onTertiary = DarkEarthOnTertiary,
    tertiaryContainer = DarkEarthContainer,
    onTertiaryContainer = DarkEarthOnContainer,
    background = DarkFieldBackground,
    surface = DarkFieldSurface,
    surfaceVariant = DarkFieldSurfaceVariant,
    outlineVariant = DarkFieldOutlineVariant,
    onBackground = DarkFieldTextPrimary,
    onSurface = DarkFieldTextPrimary,
    onSurfaceVariant = DarkFieldTextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = KrishiEmerald,
    onPrimary = KrishiOnEmerald,
    primaryContainer = KrishiEmeraldContainer,
    onPrimaryContainer = KrishiOnEmeraldContainer,
    secondary = HarvestMarigold,
    onSecondary = HarvestOnMarigold,
    secondaryContainer = HarvestMarigoldContainer,
    onSecondaryContainer = HarvestOnMarigoldContainer,
    tertiary = EarthTerracotta,
    onTertiary = EarthOnTerracotta,
    tertiaryContainer = EarthTerracottaContainer,
    onTertiaryContainer = EarthOnTerracottaContainer,
    background = WarmIvoryBackground,
    surface = CrispWhiteSurface,
    surfaceVariant = WarmTonalSurface,
    outline = WarmSandOutline,
    outlineVariant = WarmSandOutlineVariant,
    onBackground = KrishiTextPrimary,
    onSurface = KrishiTextPrimary,
    onSurfaceVariant = KrishiTextSecondary,
    error = KrishiError,
    onError = KrishiOnError,
    errorContainer = KrishiErrorContainer,
    onErrorContainer = KrishiOnErrorContainer
)

@Composable
fun CropAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val currentLanguage = LocalAppLanguage.current
    val useDevanagari = currentLanguage == AppLanguage.HINDI || currentLanguage == AppLanguage.MARATHI

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = buildAppTypography(useDevanagariPrimary = useDevanagari),
        content = content
    )
}
