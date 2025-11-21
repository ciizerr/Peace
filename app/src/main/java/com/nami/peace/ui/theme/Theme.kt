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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

import androidx.compose.material3.ProvideTextStyle
private val DarkColorScheme = darkColorScheme(
    primary = PeaceTeal,
    secondary = PeaceLavender,
    tertiary = PeaceOrange,
    background = NightSkyBackground,
    surface = NightSkySurface,
    onBackground = NightSkyTextPrimary,
    onSurface = NightSkyTextPrimary,
    surfaceVariant = NightSkySurface,
    onSurfaceVariant = NightSkyTextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF5C6BC0), // Slightly deeper for light mode contrast
    secondary = Color(0xFF7E57C2),
    tertiary = Color(0xFFEF5350),
    background = MorningLightBackground,
    surface = MorningLightSurface,
    onBackground = MorningLightTextPrimary,
    onSurface = MorningLightTextPrimary,
    surfaceVariant = Color(0xFFE1E5EB),
    onSurfaceVariant = MorningLightTextSecondary
)


@Composable
fun PeaceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
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
        typography = Typography,
        content = {
            ProvideTextStyle(value = Typography.bodyLarge, content = content)
        }
    )
}