package com.nami.peace.ui.theme

import androidx.compose.ui.graphics.Color

// --- Peace 2.0 "Calm Engagement" Palette ---

// Night Sky (Dark Mode)
val NightSkyBackground = Color(0xFF1E2B3E)
val NightSkySurface = Color(0xFF2A3C55) // Slightly lighter, translucent feel
val NightSkyTextPrimary = Color(0xFFE6E8EB)
val NightSkyTextSecondary = Color(0xFF8CA0B3)

// Morning Light (Light Mode)
val MorningLightBackground = Color(0xFFF4F6F8) // Soft cool gray/cream
val MorningLightSurface = Color(0xFFFFFFFF)
val MorningLightTextPrimary = Color(0xFF1E2B3E) // Dark Blue-Charcoal
val MorningLightTextSecondary = Color(0xFF5C6F84)

// Accents (Shared but tweaked for contrast)
val PeaceTeal = Color(0xFF64D2C1)
val PeaceOrange = Color(0xFFFFB74D)
val PeaceLavender = Color(0xFFB39DDB)
val PeacePink = Color(0xFFF48FB1)

// Semantic Colors
val PeaceSuccess = PeaceTeal
val PeaceError = Color(0xFFE57373)

// Legacy/Compat (Mapping to new system where possible)
val Purple80 = PeaceLavender
val PurpleGrey80 = NightSkyTextSecondary
val Pink80 = PeacePink

val Purple40 = Color(0xFF7E57C2)
val PurpleGrey40 = MorningLightTextSecondary
val Pink40 = Color(0xFFD81B60)

// Card Colors
val PeaceCardBlue = Color(0xFF64B5F6)
val PeaceCardPurple = PeaceLavender
val PeaceCardGreen = PeaceTeal
val PeaceCardRed = PeacePink