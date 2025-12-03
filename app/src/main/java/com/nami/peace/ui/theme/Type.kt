package com.nami.peace.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Premium Typography System - SF Pro / Inter inspired
val PeaceTypography = Typography(
    // Display styles - Ultra light and spacious
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W300, // Light
        fontSize = 64.sp,
        lineHeight = 72.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W300,
        fontSize = 52.sp,
        lineHeight = 60.sp,
        letterSpacing = (-0.25).sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W400, // Regular
        fontSize = 40.sp,
        lineHeight = 48.sp,
        letterSpacing = 0.sp
    ),
    
    // Headline styles - Clean and readable
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W600, // SemiBold
        fontSize = 34.sp,
        lineHeight = 42.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W600,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W600,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    
    // Title styles - Medium weight for hierarchy
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W600,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500, // Medium
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    ),
    
    // Body styles - Comfortable reading
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W400,
        fontSize = 17.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W400,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W400,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.4.sp
    ),
    
    // Label styles - UI elements
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// Legacy Typography for compatibility
val Typography = PeaceTypography

/**
 * Creates a custom Typography with the specified font family and font padding.
 */
fun createCustomTypography(fontFamily: FontFamily, fontPadding: Dp = 0.dp): Typography {
    val lineHeightIncrease = fontPadding.value * 0.1f
    val letterSpacingIncrease = fontPadding.value * 0.025f
    
    return Typography(
        displayLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.W300,
            fontSize = 64.sp,
            lineHeight = (72.sp.value + lineHeightIncrease).sp,
            letterSpacing = (-0.5f + letterSpacingIncrease).sp
        ),
        displayMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.W300,
            fontSize = 52.sp,
            lineHeight = (60.sp.value + lineHeightIncrease).sp,
            letterSpacing = (-0.25f + letterSpacingIncrease).sp
        ),
        displaySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.W400,
            fontSize = 40.sp,
            lineHeight = (48.sp.value + lineHeightIncrease).sp,
            letterSpacing = (0f + letterSpacingIncrease).sp
        ),
        headlineLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.W600,
            fontSize = 34.sp,
            lineHeight = (42.sp.value + lineHeightIncrease).sp,
            letterSpacing = (0f + letterSpacingIncrease).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.W600,
            fontSize = 28.sp,
            lineHeight = (36.sp.value + lineHeightIncrease).sp,
            letterSpacing = (0f + letterSpacingIncrease).sp
        ),
        headlineSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.W600,
            fontSize = 24.sp,
            lineHeight = (32.sp.value + lineHeightIncrease).sp,
            letterSpacing = (0f + letterSpacingIncrease).sp
        ),
        titleLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.W600,
            fontSize = 22.sp,
            lineHeight = (28.sp.value + lineHeightIncrease).sp,
            letterSpacing = (0f + letterSpacingIncrease).sp
        ),
        titleMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.W500,
            fontSize = 18.sp,
            lineHeight = (24.sp.value + lineHeightIncrease).sp,
            letterSpacing = (0.15f + letterSpacingIncrease).sp
        ),
        titleSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.W500,
            fontSize = 16.sp,
            lineHeight = (22.sp.value + lineHeightIncrease).sp,
            letterSpacing = (0.1f + letterSpacingIncrease).sp
        ),
        bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.W400,
            fontSize = 17.sp,
            lineHeight = (26.sp.value + lineHeightIncrease).sp,
            letterSpacing = (0f + letterSpacingIncrease).sp
        ),
        bodyMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.W400,
            fontSize = 15.sp,
            lineHeight = (22.sp.value + lineHeightIncrease).sp,
            letterSpacing = (0.25f + letterSpacingIncrease).sp
        ),
        bodySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.W400,
            fontSize = 13.sp,
            lineHeight = (18.sp.value + lineHeightIncrease).sp,
            letterSpacing = (0.4f + letterSpacingIncrease).sp
        ),
        labelLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.W500,
            fontSize = 15.sp,
            lineHeight = (20.sp.value + lineHeightIncrease).sp,
            letterSpacing = (0.1f + letterSpacingIncrease).sp
        ),
        labelMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.W500,
            fontSize = 13.sp,
            lineHeight = (18.sp.value + lineHeightIncrease).sp,
            letterSpacing = (0.5f + letterSpacingIncrease).sp
        ),
        labelSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.W500,
            fontSize = 11.sp,
            lineHeight = (16.sp.value + lineHeightIncrease).sp,
            letterSpacing = (0.5f + letterSpacingIncrease).sp
        )
    )
}
