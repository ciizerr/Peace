package com.nami.peace.ui.theme

import androidx.compose.runtime.compositionLocalOf

data class GlassSettings(
    val blurEnabled: Boolean = true,
    val blurStrength: Float = 15f,
    val blurTintAlpha: Float = 0.2f,
    val shadowsEnabled: Boolean = true,
    val shadowStrength: Float = 0.5f,
    val shadowStyle: String = "Subtle"
)

val LocalGlassSettings = compositionLocalOf { GlassSettings() }
