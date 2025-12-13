package com.nami.peace.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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

@Composable
fun PeaceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontFamily: androidx.compose.ui.text.font.FontFamily? = null,
    isBoldText: Boolean = false,
    seedColor: androidx.compose.ui.graphics.Color? = null,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        seedColor != null -> {
            // Generate scheme from seed (Simple override for primary)
            val baseScheme = if (darkTheme) DarkColorScheme else LightColorScheme
            baseScheme.copy(
                primary = seedColor,
                onPrimary = androidx.compose.ui.graphics.Color.White, // Prototyping: Assume dark/vibrant colors need white text
                primaryContainer = seedColor.copy(alpha = 0.2f), // Softer container
                onPrimaryContainer = seedColor, // Or strong version
                // Optional: Adjust secondary to match or complement
                secondary = seedColor.copy(alpha = 0.8f),
                tertiary = seedColor.copy(alpha = 0.6f)
            )
        }
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
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

    val typography = if (fontFamily != null) {
        getTypography(fontFamily, isBoldText)
    } else {
        // Apply bold even if default font
        getTypography(androidx.compose.ui.text.font.FontFamily.Default, isBoldText)  
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = Shapes,
        content = content
    )
}
