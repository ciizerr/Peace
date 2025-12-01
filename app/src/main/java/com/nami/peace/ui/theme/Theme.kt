package com.nami.peace.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
 * Provides the current font padding value (0-20dp) to all composables in the tree.
 */
val LocalFontPadding = compositionLocalOf { 0.dp }

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = DarkGray,
    surface = DarkGray,
    onBackground = White,
    onSurface = White
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = OffWhite,
    surface = White,
    onBackground = Black,
    onSurface = Black
)

/**
 * Composable function that remembers and provides the current font family based on user preferences.
 * 
 * This function:
 * - Reads the selected font from UserPreferencesRepository
 * - Loads the font using FontManager
 * - Falls back to system font if the selected font is null or not found
 * - Returns a FontFamily that can be used in Typography
 *
 * @param fontManager The FontManager instance for loading fonts
 * @param preferencesRepository The UserPreferencesRepository for reading font preferences
 * @return The selected FontFamily or system default
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
 * Main theme composable for the Peace app.
 * 
 * This theme:
 * - Supports dark/light themes with dynamic colors on Android 12+
 * - Loads custom fonts from user preferences
 * - Applies font padding from user preferences
 * - Provides custom typography to all text elements
 * 
 * @param darkTheme Whether to use dark theme (defaults to system setting)
 * @param dynamicColor Whether to use dynamic colors on Android 12+ (defaults to true)
 * @param content The composable content to be themed
 */
@Composable
fun PeaceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    
    // Get FontManager and PreferencesRepository from Hilt
    val fontManager = remember {
        val appContext = context.applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(
            appContext,
            FontManagerEntryPoint::class.java
        )
        entryPoint.fontManager()
    }
    
    val preferencesRepository = remember {
        val appContext = context.applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(
            appContext,
            PreferencesRepositoryEntryPoint::class.java
        )
        entryPoint.preferencesRepository()
    }
    
    // Get custom font family from preferences
    val fontFamily = rememberFontFamily(fontManager, preferencesRepository)
    
    // Get font padding from preferences
    val fontPaddingValue by preferencesRepository.fontPadding.collectAsState(initial = 0)
    val fontPadding = fontPaddingValue.dp
    
    // Create custom typography with the selected font
    val customTypography = createCustomTypography(fontFamily, fontPadding)
    
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    // Provide font padding via CompositionLocal
    CompositionLocalProvider(LocalFontPadding provides fontPadding) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = customTypography,
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
