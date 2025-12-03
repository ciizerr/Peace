package com.nami.peace.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.nami.peace.data.repository.UserPreferencesRepository
import com.nami.peace.util.font.FontManager
import dagger.hilt.android.EntryPointAccessors

/**
 * CompositionLocal for font padding.
 */
val LocalFontPadding = compositionLocalOf { 0.dp }

// Premium Light Color Scheme - Calm Morning
private val PeaceLightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = SerenityBlue.copy(alpha = 0.12f),
    onPrimaryContainer = DeepCharcoal,
    secondary = LightSecondary,
    onSecondary = PureWhite,
    secondaryContainer = CalmTeal.copy(alpha = 0.12f),
    onSecondaryContainer = DeepCharcoal,
    tertiary = LightTertiary,
    onTertiary = DeepCharcoal,
    tertiaryContainer = MintGlow.copy(alpha = 0.12f),
    onTertiaryContainer = DeepCharcoal,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceTint = SerenityBlue.copy(alpha = 0.05f),
    error = AccentError,
    onError = PureWhite,
    errorContainer = AccentError.copy(alpha = 0.12f),
    onErrorContainer = AccentError,
    outline = Color(0xFFD1D5DB),
    outlineVariant = Color(0xFFE5E7EB),
    scrim = Color(0x80000000)
)

// Premium AMOLED Dark Color Scheme - Peaceful Night
private val PeaceDarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimary.copy(alpha = 0.15f),
    onPrimaryContainer = DarkOnSurface,
    secondary = DarkSecondary,
    onSecondary = Color(0xFF003826),
    secondaryContainer = DarkSecondary.copy(alpha = 0.15f),
    onSecondaryContainer = DarkOnSurface,
    tertiary = DarkTertiary,
    onTertiary = Color(0xFF003826),
    tertiaryContainer = DarkTertiary.copy(alpha = 0.15f),
    onTertiaryContainer = DarkOnSurface,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceTint = DarkPrimary.copy(alpha = 0.08f),
    error = AccentError,
    onError = Color.White,
    errorContainer = AccentError.copy(alpha = 0.15f),
    onErrorContainer = AccentError.copy(alpha = 0.8f),
    outline = Color(0xFF374151),
    outlineVariant = Color(0xFF1F2937),
    scrim = Color(0xCC000000)
)

/**
 * Remembers and provides the current font family based on user preferences.
 */
@Composable
fun rememberFontFamily(
    fontManager: FontManager,
    preferencesRepository: UserPreferencesRepository
): FontFamily {
    val selectedFont by preferencesRepository.selectedFont.collectAsState(initial = null)
    
    return remember(selectedFont) {
        if (selectedFont == null) {
            fontManager.getSystemFont()
        } else {
            fontManager.getFont(selectedFont!!) ?: fontManager.getSystemFont()
        }
    }
}

/**
 * Main theme composable for the Peace app with premium design.
 * 
 * Features:
 * - Premium color palette (Serenity Blue, Calm Teal, Mint Glow)
 * - AMOLED dark mode support
 * - Smooth, rounded shapes
 * - Elegant typography
 */
@Composable
fun PeaceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Use premium color schemes
    val colorScheme = if (darkTheme) PeaceDarkColorScheme else PeaceLightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Set status bar color
            window.statusBarColor = if (darkTheme) Color.Black.toArgb() else Color.White.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    // Provide default font padding
    CompositionLocalProvider(LocalFontPadding provides 0.dp) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = PeaceShapes,
            content = content
        )
    }
}

/**
 * Hilt EntryPoint for accessing FontManager from composables.
 */
@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface FontManagerEntryPoint {
    fun fontManager(): FontManager
}

/**
 * Hilt EntryPoint for accessing UserPreferencesRepository from composables.
 */
@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface PreferencesRepositoryEntryPoint {
    fun preferencesRepository(): UserPreferencesRepository
}
