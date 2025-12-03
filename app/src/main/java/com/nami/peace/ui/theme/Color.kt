package com.nami.peace.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================
// PREMIUM PEACE COLOR SYSTEM
// Inspired by Apple HIG + Material You + Calm
// ============================================

// Core Brand Colors - Peaceful & Premium
val SerenityBlue = Color(0xFF75B8FF) // Primary - Serenity Blue
val CalmTeal = Color(0xFF4EC9B0) // Secondary - Calm Teal
val MintGlow = Color(0xFF8CF0C8) // Accent - Mint Glow
val MistGrey = Color(0xFFF4F6F8) // Background - Mist Grey
val DeepCharcoal = Color(0xFF1C1C1E) // Text - Deep Charcoal
val PureWhite = Color(0xFFFFFFFF) // Surface
val SoftCloud = Color(0xFFFAFBFC) // Surface Variant

// Light Theme - Calm Morning
val LightPrimary = SerenityBlue
val LightSecondary = CalmTeal
val LightTertiary = MintGlow
val LightBackground = MistGrey
val LightSurface = PureWhite
val LightSurfaceVariant = SoftCloud
val LightOnPrimary = PureWhite
val LightOnBackground = DeepCharcoal
val LightOnSurface = DeepCharcoal
val LightOnSurfaceVariant = Color(0xFF6B7280) // Slate Grey

// AMOLED Dark Theme - Peaceful Night
val DarkPrimary = Color(0xFF9BCFFF) // Lighter Serenity
val DarkSecondary = Color(0xFF6EDDC4) // Lighter Teal
val DarkTertiary = Color(0xFFA8F5D8) // Lighter Mint
val DarkBackground = Color(0xFF000000) // Pure Black (AMOLED)
val DarkSurface = Color(0xFF121212) // Elevated Surface
val DarkSurfaceVariant = Color(0xFF1E1E1E) // Card Surface
val DarkOnPrimary = Color(0xFF003258) // Dark Blue
val DarkOnBackground = Color(0xFFE8EAED) // Light Grey
val DarkOnSurface = Color(0xFFE8EAED)
val DarkOnSurfaceVariant = Color(0xFFB0B8C0) // Medium Grey

// Priority Colors - Soft & Peaceful
val PriorityHigh = Color(0xFFFF9F8E) // Soft Coral (not alarming)
val PriorityMedium = Color(0xFFFFD88E) // Warm Gold
val PriorityLow = Color(0xFFB4E4FF) // Sky Blue

// Status & Accent Colors
val AccentSuccess = Color(0xFF6EDDC4) // Calm Teal
val AccentWarning = Color(0xFFFFD88E) // Warm Gold
val AccentError = Color(0xFFFF9F8E) // Soft Coral
val AccentInfo = SerenityBlue

// Gradient Colors - Calm Morning
val GradientLightStart = Color(0xFFE3F4FF) // Morning Sky Start
val GradientLightEnd = Color(0xFFFDFEFF) // Morning Sky End
val GradientDarkStart = Color(0xFF1A2332) // Night Gradient Start
val GradientDarkEnd = Color(0xFF0A0E1A) // Night Gradient End

// Glow Effects - Subtle Highlights
val GlowBlue = SerenityBlue.copy(alpha = 0.15f)
val GlowTeal = CalmTeal.copy(alpha = 0.12f)
val GlowMint = MintGlow.copy(alpha = 0.10f)

// Shadow Colors - Soft Depth
val ShadowLight = Color(0x0A000000) // 4% black
val ShadowMedium = Color(0x14000000) // 8% black
val ShadowDark = Color(0x1F000000) // 12% black

// Legacy colors (kept for compatibility)
val Purple80 = DarkPrimary
val PurpleGrey80 = DarkSecondary
val Pink80 = DarkTertiary
val Purple40 = LightPrimary
val PurpleGrey40 = LightSecondary
val Pink40 = LightTertiary
val Black = DeepCharcoal
val White = PureWhite
val OffWhite = SoftCloud
val DarkGray = DarkSurface
val LightGray = LightSurfaceVariant
val AccentRed = PriorityHigh
