package com.nami.peace.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)

/**
 * Creates a custom Typography with the specified font family and font padding.
 * 
 * Font padding is applied by:
 * - Increasing line height proportionally to the padding value
 * - Increasing letter spacing proportionally to the padding value
 * 
 * This ensures that text elements have consistent spacing when font padding is applied.
 * 
 * @param fontFamily The font family to use for all text styles
 * @param fontPadding The padding value (0-20dp) to apply to text spacing
 * @return A Typography instance with custom font and padding applied
 */
fun createCustomTypography(fontFamily: FontFamily, fontPadding: Dp = 0.dp): Typography {
    // Calculate padding multipliers
    // For every 1dp of padding, increase line height by 0.1sp and letter spacing by 0.025sp
    val lineHeightIncrease = fontPadding.value * 0.1f
    val letterSpacingIncrease = fontPadding.value * 0.025f
    
    return Typography(
        displayLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 57.sp,
            lineHeight = (64.sp.value + lineHeightIncrease).sp,
            letterSpacing = (-0.25f + letterSpacingIncrease).sp
        ),
        displayMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 45.sp,
            lineHeight = (52.sp.value + lineHeightIncrease).sp,
            letterSpacing = (0f + letterSpacingIncrease).sp
        ),
        displaySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 36.sp,
            lineHeight = (44.sp.value + lineHeightIncrease).sp,
            letterSpacing = (0f + letterSpacingIncrease).sp
        ),
        headlineLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 32.sp,
            lineHeight = (40.sp.value + lineHeightIncrease).sp,
            letterSpacing = (0f + letterSpacingIncrease).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 28.sp,
            lineHeight = (36.sp.value + lineHeightIncrease).sp,
            letterSpacing = (0f + letterSpacingIncrease).sp
        ),
        headlineSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 24.sp,
            lineHeight = (32.sp.value + lineHeightIncrease).sp,
            letterSpacing = (0f + letterSpacingIncrease).sp
        ),
        titleLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 22.sp,
            lineHeight = (28.sp.value + lineHeightIncrease).sp,
            letterSpacing = (0f + letterSpacingIncrease).sp
        ),
        titleMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = (24.sp.value + lineHeightIncrease).sp,
            letterSpacing = (0.15f + letterSpacingIncrease).sp
        ),
        titleSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = (20.sp.value + lineHeightIncrease).sp,
            letterSpacing = (0.1f + letterSpacingIncrease).sp
        ),
        bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = (24.sp.value + lineHeightIncrease).sp,
            letterSpacing = (0.5f + letterSpacingIncrease).sp
        ),
        bodyMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = (20.sp.value + lineHeightIncrease).sp,
            letterSpacing = (0.25f + letterSpacingIncrease).sp
        ),
        bodySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = (16.sp.value + lineHeightIncrease).sp,
            letterSpacing = (0.4f + letterSpacingIncrease).sp
        ),
        labelLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = (20.sp.value + lineHeightIncrease).sp,
            letterSpacing = (0.1f + letterSpacingIncrease).sp
        ),
        labelMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = (16.sp.value + lineHeightIncrease).sp,
            letterSpacing = (0.5f + letterSpacingIncrease).sp
        ),
        labelSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = (16.sp.value + lineHeightIncrease).sp,
            letterSpacing = (0.5f + letterSpacingIncrease).sp
        )
    )
}
